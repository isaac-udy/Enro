---
title: Troubleshooting
nav_order: 80
---

# Troubleshooting

This page lists Enro error messages, the cause of each error, and the fix.

For an error that this page does not list, see
[Reporting an issue](#reporting-an-issue).

## Setup errors

### `EnroController has not been installed`

The app called an Enro API before the app installed the `EnroController`.
Call `installNavigationController(...)` on the `NavigationComponent` before
any other Enro call.

<!-- snippet: troubleshooting/install -->
```kotlin
// Android
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)   // must run before any other Enro call
    }
}
```

[Installation](getting-started/installation.md) shows the install call for
each platform.

### `No NavigationHandle found for [KeyClass]`

A Composable called `navigationHandle<KeyClass>()` outside a destination.

`navigationHandle` returns a `NavigationHandle` only in one of these places:

- a Composable function annotated with `@NavigationDestination(...)`
- the body of a `navigationDestination<K> { }` provider
- a Composable that one of those two composes

<!-- snippet: troubleshooting/handle-scope -->
```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()  // ShowProfile is the key in @NavigationDestination
    // ...
}

@Composable
fun SomeRandomComposable() {
    val navigation = navigationHandle<NavigationKey>() // SomeRandomComposable is not composed inside a destination
}
```

### `Expected key of type [KeyClass], but found [OtherKeyClass]`

A Composable called `navigationHandle<KeyClass>()` inside a destination whose
key is not a `KeyClass`. The type parameter of `navigationHandle` must be the
destination's key type or a supertype of the destination's key type.

A Composable that several destinations share can call
`navigationHandle<NavigationKey>()`. `navigation.key` then has type
`NavigationKey`.

### `No LocalNavigationHandle`

A Composable called `registerForNavigationResult` outside a destination.
Move the `registerForNavigationResult` call into a destination, or into a
Composable that a destination composes.

### `No binding found for [KeyClass]`

The app opened a `KeyClass` key and no destination is bound to `KeyClass`.
Three common causes:

1. **The destination has no `@NavigationDestination(KeyClass::class)`
   annotation.** The KSP processor generates a binding only for an annotated
   destination.
2. **The module that declares the destination has no `enro-processor` KSP
   dependency.** Every module that contains a `@NavigationDestination`
   annotation requires a KSP dependency on `dev.enro:enro-processor`.
3. **The app module does not depend on the module that declares the
   destination.** The app module's classpath must include every module that
   declares a destination.

If none of the three causes applies, run a clean build
(`./gradlew clean :app:assembleDebug`). The
[modular navigation recipe][modular-recipe] shows a multi-module setup.

## Navigation-handle errors

### `${key} is a NavigationKey.WithResult and cannot be completed without a result`

The code called `complete()` with no argument for a key that implements
`NavigationKey.WithResult<R>`. A `NavigationKey.WithResult<R>` key must be
completed with a result. On a `NavigationHandle` typed to a
`NavigationKey.WithResult<R>` key, the compiler rejects `complete()` with no
argument.

<!-- snippet: skip (shows code that does not compile) -->
```kotlin
// Compile error when the key implements WithResult<LocalDate>.
navigation.complete()

navigation.complete(LocalDate.now())
```

To close the destination without a result, call `navigation.close()`. Enro
then calls the caller's `onClosed` callback.

### `Cannot completeFrom a NavigationKey.WithResult from a NavigationKey that does not also implement NavigationKey.WithResult`

`completeFrom(otherKey)` opens `otherKey` and delivers the result of
`otherKey` to the caller of the current destination. This error means the
current destination's key has no result and `otherKey` implements
`NavigationKey.WithResult`. The compiler rejects the call unless the code
suppresses the compile error.

A destination with a `NavigationKey.WithResult<R>` key can call
`completeFrom` only with another `NavigationKey.WithResult<R>` key that has
the same `R`.

To replace the current destination with a key and forward no result, call
`closeAndReplaceWith(otherKey)`.

### `Multiple onCloseRequested callbacks have been registered for NavigationHandle with key [Key]`

Two or more `onCloseRequested` callbacks are registered for the same
destination, for example one callback in the ViewModel and one callback in
the Composable, or two `configure { }` blocks in the same Composable.
`requestClose()` throws this error when a `NavigationHandle` has more than
one active `onCloseRequested` callback.

Register the `onCloseRequested` callback in one place: the destination's
ViewModel, or the destination's top-level Composable when the destination
has no ViewModel. See
[Navigation Handles → Overriding the close-requested callback](core-concepts/navigation-handles.md#overriding-the-close-requested-callback).

### `Cannot execute NavigationOperation on TestNavigationHandle that is closed`

A test executed a navigation operation on a `TestNavigationHandle` after the
`TestNavigationHandle` was closed. A completed `TestNavigationHandle` throws
the same error with "that is completed".

To use the `TestNavigationHandle` again after a close or a completion, call
`handle.clearOperationHistory()` first.

### `SyntheticDestination for [Key] has already finished with [Outcome]`

The code called an outcome method (`open`, `close`, `closeSilently`,
`complete`, `completeFrom`) on a synthetic destination's scope after the
synthetic destination had an outcome. Enro converts the first outcome of
the synthetic block into a navigation operation. A synthetic block that
returns without an outcome closes silently. Each later outcome call throws
this error.

The usual cause is a coroutine launched inside a synthetic block:

<!-- snippet: troubleshooting/async-synthetic -->
```kotlin
@NavigationDestination(BadSynthetic::class)
val badSynthetic = syntheticDestination<BadSynthetic> {
    context.lifecycleScope.launch {
        someAsyncWork()
        complete()      // throws "already finished": the synthetic block returned before this call
    }
}
```

A synthetic block runs synchronously. A synthetic destination has no
lifecycle, no ViewModel store and no saved state. Use one of these fixes:

- **Do the async work before opening the synthetic destination.** Pass the
  result of the async work in the synthetic destination's key.
- **Forward to a destination that does the async work:**
  `syntheticDestination<...> { completeFrom(LoadingScreen) }`.
- **Replace the synthetic destination with a regular destination** that
  shows a loading state, does the async work, and then closes.

See [Synthetic Destinations](advanced/synthetic-destinations.md).

## Result errors

### `Received result for id ${id}, but no active steps had that id`

A navigation flow logs this message when the flow receives a result for a
step and the flow no longer contains a step with that id. The usual cause is
a flow block that produced different steps when the flow block ran again:
for example, an earlier result changed a branch, and the branch no longer
opens the step.

The same earlier results must always produce the same `open` calls in the
same order. A condition inside the flow block is allowed when the condition
depends only on earlier results.

## Serialization errors

### `Object of type X could not be added to NavigationKey.Metadata`

The code stored a value in `instance.metadata` and the `EnroController` has
no serializer for the value's type. Enro makes this check only when the
`EnroController` is installed in debug mode, and skips the check for a
`NavigationKey.TransientMetadataKey`. Primitive types, `String`, and lists,
sets and maps of those types need no registration. Register a serializer
for any other type on the `NavigationComponent`:

<!-- snippet: troubleshooting/serializers-module -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        serializersModule(SerializersModule {
            polymorphic(Any::class) {
                subclass(MyCustomType.serializer())
            }
        })
    }
)
```

### Backstack restoration fails after process death

The usual cause is a `NavigationKey` with a property that is not
serializable. Annotate the `NavigationKey` with `@Serializable`. Each
property of the `NavigationKey` must be a built-in serializable type, a
`@Serializable` type, or a type registered in the `SerializersModule` of the
`NavigationComponent`.

## Testing errors

### `Could not get a NavigationHandle for ViewModel of type [ViewModel]`

A test constructed a ViewModel that declares `by navigationHandle<MyKey>()`,
and the test registered no `NavigationHandle` for the ViewModel class.

Run the test inside `runEnroTest { }`, or add an `EnroTestRule` to the test
class.

<!-- snippet: troubleshooting/test-controller -->
```kotlin
@Test
fun example() = runEnroTest {
    val handle = createTestNavigationHandle(MyKey)
    // ...
}
```

Before the test constructs the ViewModel, call
`putNavigationHandleForViewModel<MyVm, MyKey>(key)`.
`putNavigationHandleForViewModel` returns the `TestNavigationHandle` that
`by navigationHandle<MyKey>()` resolves to.

See [Testing](advanced/testing.md).

### `Multiple onCloseRequested callbacks ...` in a test

The cause is the same as in app code: two `onCloseRequested` callbacks are
registered for one `NavigationHandle`. In a test, a common cause is a
fixture and a `before` block that each register an `onCloseRequested`
callback. Register the `onCloseRequested` callback once per test.

## Migration errors (Enro 2 → 3)

The [migration guide](migrating-from-v2.md) covers compile errors after an
upgrade from Enro 2. The most common compile errors:

- **`Unresolved reference: SupportsPush` / `SupportsPresent`** — Enro 3 has
  no `SupportsPush` or `SupportsPresent`. Use `NavigationKey` or
  `NavigationKey.WithResult<R>`.
- **`Unresolved reference: closeWithResult`** — call `complete(value)`.
- **`Unresolved reference: push` / `present`** — call `open`. Destination
  metadata declares dialog and overlay behaviour.
- **`Unresolved reference: NavigationApplication`** — Enro 3 has no
  `NavigationApplication`. Call `installNavigationController` from
  `Application.onCreate`.
- **`@Parcelize` on a key** — annotate the key with `@Serializable`
  (kotlinx.serialization).
- **`Unresolved reference: enroViewModels`** — use
  `viewModel { createEnroViewModel { ... } }`.

## Reporting an issue

Open an issue at
[github.com/isaac-udy/Enro/issues](https://github.com/isaac-udy/Enro/issues).
Include:

- The full stack trace.
- A minimal code sample that shows the call site.
- The Enro version (for example `3.0.0`).
- The platform and version (Android API level, iOS version, JDK version,
  or browser).

[modular-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/modular/ModularNavigation.kt
