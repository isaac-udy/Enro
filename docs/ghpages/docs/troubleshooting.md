---
title: Troubleshooting
nav_order: 80
---

# Troubleshooting

Most issues with Enro at runtime fall into one of a few categories — the
controller isn't installed, a destination isn't bound to its key, a handle
is accessed from the wrong scope, or a value can't be serialized. This
page walks through the common symptoms.

If you don't find your issue here, please open an issue at
[github.com/isaac-udy/Enro/issues](https://github.com/isaac-udy/Enro/issues)
with a stack trace and a brief description of the call site.

## Setup errors

### `EnroController has not been installed`

The controller wasn't installed before something tried to use it. Make
sure you call `installNavigationController(...)` on your `NavigationComponent`
before any composable that reads a navigation handle is composed.

```kotlin
// Android
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)   // <- this must run first
    }
}
```

See [Installation](getting-started/installation.md) for the equivalent on
each platform.

### `No NavigationHandle found for [KeyClass]`

You called `navigationHandle<MyKey>()` from a composable that isn't a
navigation destination, or whose key is a different type.

A handle is only available inside a destination — that is, a composable
function annotated with `@NavigationDestination(...)` (or the body of a
`navigationDestination<K> { }` provider), or any composable composed
**inside** one. The typed parameter must match the destination's actual
key type.

```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()  // ✅ matches the annotation
    // ...
}

@Composable
fun SomeRandomComposable() {
    val navigation = navigationHandle<NavigationKey>() // ❌ not inside a destination
}
```

If your composable is genuinely shared between destinations and doesn't
need to know its key type, use the untyped form
`navigationHandle<NavigationKey>()` and accept that `navigation.key` will
be the base interface.

### `No LocalNavigationHandle`

Same family as the previous error — composable reading the navigation
handle from outside any destination. Wrap it inside a destination, or
hoist the navigation calls up to a destination-level composable and pass
the operations down as lambdas.

### Missing navigation binding for `[KeyClass]`

Your key is being opened but no destination is registered for it. Three
common causes:

1. **Forgot `@NavigationDestination(KeyClass::class)` on the destination.**
   The annotation is what drives KSP code generation.
2. **The module that declares the destination doesn't apply the
   `enro-processor` KSP plugin.** Every module that contains
   `@NavigationDestination` annotations needs its own KSP dependency on
   `dev.enro:enro-processor`.
3. **The app module doesn't depend on the destination module.** KSP-generated
   bindings only travel through direct dependencies — make sure your app
   module's classpath includes every feature module that contains
   destinations.

If all three look correct, try a clean build (`./gradlew clean
:app:assembleDebug`) — KSP incrementality can occasionally cache an old
result. See the [modular navigation
recipe][modular-recipe] for the canonical multi-module setup.

## Navigation-handle errors

### `${key} is a NavigationKey.WithResult and cannot be completed without a result`

You called `navigation.complete()` (no arguments) on a destination whose
key implements `NavigationKey.WithResult<R>`. A result-producing key
must produce a result.

```kotlin
// ❌ won't compile if the key implements WithResult<LocalDate>
navigation.complete()

// ✅ pass the result
navigation.complete(LocalDate.now())
```

If the destination genuinely has no result to deliver and you want it
gone, use `navigation.close()` — the caller's `onClosed` callback will
fire.

### `Cannot completeFrom a NavigationKey.WithResult from a NavigationKey that does not also implement NavigationKey.WithResult`

`completeFrom(otherKey)` says "delegate this destination's completion to
`otherKey`". For that to make sense, both keys must agree on the result
type: a `WithResult<R>` destination can only `completeFrom` another
`WithResult<R>` with the matching `R`.

If the redirect target genuinely doesn't have a result, you probably
want `closeAndReplaceWith(otherKey)` instead.

### Multiple `onCloseRequested` callbacks registered for the same NavigationHandle

You registered the close-requested callback in more than one place for
the same destination — typically one in the ViewModel and one in the
Composable, or two `configure { }` blocks in the same composable. Only
one callback may be active at a time.

Pick one home for the callback (the ViewModel if you have one, otherwise
the top-level Composable). See
[Navigation Handles → Overriding the close-requested callback](core-concepts/navigation-handles.md#overriding-the-close-requested-callback).

### `Cannot execute NavigationOperation on TestNavigationHandle that is closed`

The handle you're using in a test was closed (or completed) and then you
tried to drive more navigation through it. Either the test is exercising
a flow that goes past the close, or the assertion is at the wrong point.

If you intentionally want to continue using the handle after a close,
call `handle.clearOperationHistory()` between scenarios.

### `SyntheticDestination for [Key] has already finished with [Outcome]`

An outcome method (`open`, `close`, `closeSilently`, `complete`,
`completeFrom`) was called on a synthetic's scope after the synthetic
had already concluded. The dispatcher catches the first outcome the
block emits and converts it to a navigation operation; subsequent calls
have nothing to do.

The usual cause is launching a coroutine inside a synthetic block:

```kotlin
@NavigationDestination(BadSynthetic::class)
val badSynthetic = syntheticDestination<BadSynthetic> {
    context.lifecycleOwner.lifecycleScope.launch {
        someAsyncWork()
        complete()      // ← block has long since returned; throws "already finished"
    }
}
```

Synthetics are intentionally synchronous decision points — they don't
have their own lifecycle, view-model store, or persisted state. The fix
is one of:

- **Do the async work before opening the synthetic** and pass the
  result into the synthetic's key as a parameter.
- **Forward to a real destination** that owns the work as part of its
  own lifecycle: `syntheticDestination<...> { completeFrom(LoadingScreen) }`.
- **Restructure as a regular destination with a loading state** if the
  synthetic was really trying to be a "do some work then close" UI.

See [Synthetic Destinations](advanced/synthetic-destinations.md) for the
full design rationale.

## Result errors

### `Received result for id ${id}, but no active steps had that id`

Specific to managed flows. The flow received a step result, but the
flow scope no longer contains a step matching that id — usually because
the flow's body changed shape between the time the step was opened and
the time the result came back (an upstream value caused a branch to no
longer include this step, for example).

Make sure each `open(key)` inside the flow is reached deterministically
from the upstream results. Don't write conditions that produce a
**different ordering** of `open` calls on re-evaluation; conditions are
fine, but the same upstream results should always lead to the same flow
shape.

## Serialization errors

### `Object of type X could not be added to NavigationKey.Metadata`

You're storing a value in `instance.metadata` whose serializer isn't
registered. Built-in Kotlin types are fine; custom types either need to
be `@Serializable` or have a serializer registered on the
`NavigationComponent`:

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        serializersModule(SerializersModule {
            contextual(MyCustomType.serializer())
        })
    }
)
```

### Backstack restoration fails after process death

Usually means one of your `NavigationKey`s has a non-serializable
property. Mark the key as `@Serializable` and make sure every property
on it is also serializable (either built-in, `@Serializable` itself, or
registered in your `SerializersModule`). Run a simple "kill from
Recents" test in development to flush these out early.

## Testing errors

### `No NavigationHandle found ...` in a test

You're constructing a destination or ViewModel without setting up a
test controller. Wrap the test in `runEnroTest { }`, or add an
`EnroTestRule` to the test class.

```kotlin
@Test
fun example() = runEnroTest {
    val handle = createTestNavigationHandle(MyKey)
    // ...
}
```

For ViewModels with `by navigationHandle<MyKey>()`, also call
`putNavigationHandleForViewModel<MyVm, MyKey>(key)` so the ViewModel can
resolve its handle.

See [Testing](advanced/testing.md).

### `Multiple onCloseRequested callbacks ...` in a test

Same root cause as in app code, but easier to hit accidentally if a
fixture installs the callback and the test's `before` block does too.
Make sure each scenario only sets up the callback once.

## Migration errors (Enro 2 → 3)

If you're getting compile errors after upgrading from Enro 2, see the
[migration guide](migrating-from-v2.md). The most common ones:

- **`Unresolved reference: SupportsPush` / `SupportsPresent`** — the v2
  markers are gone. Use bare `NavigationKey` or
  `NavigationKey.WithResult<R>`.
- **`Unresolved reference: closeWithResult`** — renamed to `complete(value)`.
- **`Unresolved reference: push` / `present`** — both replaced by `open`.
  Dialog/overlay behaviour moves into destination metadata.
- **`Unresolved reference: NavigationApplication`** — gone. Install the
  controller directly from `Application.onCreate`.
- **`@Parcelize` flagged but not migrated** — keys are now `@Serializable`
  (kotlinx.serialization).
- **`enroViewModels` no longer resolves** — use `viewModel { createEnroViewModel { ... } }`.

## Reporting an issue

If your problem isn't covered here, the most helpful issue report
includes:

- The full stack trace.
- A minimal code snippet showing the call site.
- The Enro version you're on (`3.0.0-alphaXX`).
- The platform (Android API level, iOS version, JDK version, browser).

[modular-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/modular/ModularNavigation.kt
