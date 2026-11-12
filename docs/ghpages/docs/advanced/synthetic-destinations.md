---
title: Synthetic Destinations
parent: Advanced Topics
nav_order: 6
---

# Synthetic Destinations

A synthetic destination binds a `NavigationKey` to a block of code instead
of a screen. Opening the key runs the block. Enro never adds the synthetic
instance to a backstack. A synthetic destination can open an external
browser, choose between several destinations, or run several actions for
one `open` call.

A caller opens a synthetic destination the same way as any other
destination:

<!-- snippet: synthetic-destinations/callers -->
```kotlin
navigation.open(Logout)        // synthetic
navigation.open(EditProfile)   // synthetic: opens one of two screens
navigation.open(SettingsScreen) // ordinary destination
```

## When to use a synthetic destination

The [recipes][synthetic-recipes] contain one example of each use:

- **Side effect** — run a non-Enro action from a navigation call. For
  example, `OpenExternalUrl(url)` launches Chrome Custom Tabs on Android,
  calls `Desktop.browse(...)` on JVM, and calls `window.open(...)` on web.
- **Conditional redirect** — check a runtime condition before opening a
  destination. `RequireLogin` reads the auth state and opens either the
  requested screen or a login screen.
- **Result decider** — a synthetic destination with a result type chooses
  one of several destinations (legacy or V2, feature flag on or off,
  narrow or wide screen). `completeFrom` delivers the result of the chosen
  destination to the original caller.

Use a synthetic destination when no screen is rendered, or when runtime
state decides which screen is rendered. Use an ordinary destination with
metadata to change how one screen is rendered.

## Declaring a synthetic

`syntheticDestination<K>` returns a `NavigationDestinationProvider<K>`.
The KSP processor binds the provider the same way as any other destination:

<!-- snippet: navigation-destinations/synthetic -->
```kotlin
@Serializable
object Logout : NavigationKey

@NavigationDestination(Logout::class)
val logout = syntheticDestination<Logout> {
    sessionRepository.clearSession()
    open(LoginScreen())
}
```

The block has a `SyntheticDestinationScope<K>` receiver. The scope
contains `key`, `instance`, `context` and the outcome methods.

## Outcome methods

An outcome method sets the operation that replaces the synthetic
destination's `Open` operation. Each outcome method returns `Nothing`, so
code after the call does not run. The **pure outcomes** replace the `Open`
operation during the container's `processOperations` pass. The
**side-effect outcome** runs after the pass finishes.

### Pure outcomes

| Method | Effect |
|---|---|
| `open(key)` | Replaces `Open(synthetic)` with `Open(key)` on the same container. The container's interceptors receive `Open(key)` in the same pass. |
| `close()` | Registers a `Closed` result for the caller that opened the synthetic destination. The caller's `onClosed` callback runs. |
| `closeSilently()` | Closes the synthetic destination. The caller's `onClosed` callback does **not** run. |
| `complete()` | Registers a `Completed` result with no value. Available when the key does not implement `NavigationKey.WithResult<R>`. |
| `complete(result)` | Registers a `Completed` result with a value. Available when the key implements `NavigationKey.WithResult<R>`. |
| `completeFrom(key)` | Opens `key`. When the `key` destination completes, the caller that opened the synthetic destination receives the result. |

### Side-effect outcome

| Method | Effect |
|---|---|
| `sideEffect { ... }` | Runs the block after the container applies the operations. The block has a [`SyntheticSideEffectScope`](#side-effect-scope) receiver. The synthetic destination is closed silently. Use `sideEffect` for platform APIs, the container reference, or work that is not one navigation operation. |

Each outcome method throws an exception that Enro catches and converts into
a `NavigationOperation`. A `catch (e: Throwable)` inside a synthetic block
catches the exception, and the outcome is lost.

## When each outcome runs

A **pure outcome** replaces the synthetic destination's `Open` operation
at the same position in the list of operations. For example, when an
initial backstack is `[ASynthetic, B]` and `ASynthetic` opens `Target`, the
backstack is `[Target, B]`.

A **side-effect outcome** runs after the container has applied every
operation in the pass and has finished executing. A `sideEffect` block
can:

- Call platform APIs, such as finding an `Activity`.
- Read or replace the container's backstack with
  `container.execute(context, NavigationOperation.SetBackstack(...))`.
- Make any other call that is not one `NavigationOperation`.

In the [recipes][synthetic-recipes], the auth gate and the profile decider
use pure outcomes. The external URL launcher uses `sideEffect { ... }` to
call a non-Enro API.

## A block that calls no outcome method

A synthetic block that returns without calling an outcome method is closed
silently. No result callback runs.

Put work that uses the container or a platform API in `sideEffect { ... }`
instead of in the body of the block. The body of the block runs while the
container is executing an operation.

## Forwarding results with `completeFrom`

A result decider is a synthetic destination whose key implements
`NavigationKey.WithResult<R>`. The block chooses one of several
destinations with the same result type, and `completeFrom` delivers the
chosen destination's result to the caller:

<!-- snippet: synthetic-destinations/decider -->
```kotlin
@Serializable
object EditProfile : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileLegacy : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileV2 : NavigationKey.WithResult<ProfileUpdate>

@NavigationDestination(EditProfile::class)
val editProfile = syntheticDestination<EditProfile> {
    if (featureFlags.useNewProfileUi) completeFrom(EditProfileV2)
    else completeFrom(EditProfileLegacy)
}
```

The caller opens `EditProfile` and receives one result, from either
screen:

<!-- snippet: synthetic-destinations/decider-caller -->
```kotlin
val editProfile = registerForNavigationResult<ProfileUpdate>(
    onCompleted = { update -> /* runs for a result from either screen */ },
)
Button(onClick = { editProfile.open(EditProfile) }) { Text("Edit") }
```

`completeFrom` copies the synthetic instance's result id into the metadata
of the forwarded instance. When the forwarded destination calls
`navigation.complete(value)`, the original caller's result channel
receives `value`.

## The "already finished" error

A synthetic block must be **synchronous**. Enro reads the outcome when the
block returns. When code calls an outcome method after the block has
returned, for example from a coroutine that the block launched, the
`SyntheticDestinationScope` throws:

```
SyntheticDestination for X has already finished with Close.
A second outcome cannot be set — this usually means an async coroutine
outlived the synthetic block and tried to complete/close it after the
dispatcher had already moved on. Do any async work before opening the
synthetic, or forward to a destination that owns the work itself.
```

To fix the error, use one of these options:

- **Do the async work before opening the synthetic destination.** Pass the
  result of the work as a property of the synthetic destination's key.
- **Forward to an ordinary destination that does the async work**, for
  example `completeFrom(WorkInProgressScreen)`.
- **Replace the synthetic destination with an ordinary destination** that
  shows a loading state, does the work, and closes.

A synthetic destination has no lifecycle, no ViewModel store and no saved
state.

## Accessing the originating context

### `SyntheticDestinationScope`

`SyntheticDestinationScope<K>` has two context properties. `context` is the
`NavigationContext` that opened the synthetic destination.
`destinationContext` is the active destination nearest to `context`, or
`null`. Read `context.controller`, the parent contexts, or the opening
destination to choose an outcome.

Do not execute navigation operations from the body of a synthetic block.
The body runs while the container is executing an operation, and a
`container.execute(...)` call at that point throws an
`IllegalStateException`. Use `sideEffect { ... }` instead.

### `SyntheticSideEffectScope`
{: #side-effect-scope }

`SyntheticSideEffectScope<K>` is the receiver of the `sideEffect` block.
The scope contains `context`, `container`, `instance` and `key`. The
`sideEffect` block runs after the container has finished executing, so the
block can call `container.execute(...)`:

<!-- snippet: synthetic-destinations/side-effect-android -->
```kotlin
@NavigationDestination(OpenExternalUrl::class)
val openExternalUrl = syntheticDestination<OpenExternalUrl> {
    sideEffect {
        // findActivity() is your own helper, not an Enro API.
        val activity = context.findActivity()
        activity.startActivity(Intent(ACTION_VIEW, Uri.parse(key.url)))
    }
}
```

<!-- snippet: synthetic-destinations/side-effect-backstack -->
```kotlin
@NavigationDestination(DeepLinkResolver::class)
val deepLinkResolver = syntheticDestination<DeepLinkResolver> {
    sideEffect {
        val resolved = context.controller.getNavigationKeyFromPath(key.url)
            ?: return@sideEffect
        container.execute(
            context = context,
            operation = NavigationOperation.SetBackstack(
                currentBackstack = container.backstack,
                targetBackstack = backstackOf(Home.asInstance(), resolved.asInstance()),
            ),
        )
    }
}
```

## Testing synthetic destinations

`testSyntheticDestination(...)` in `enro-test` runs a synthetic block
without a container or interceptors, and returns the
[`SyntheticOutcome`][synthetic-outcome] that the block chose.
`testSyntheticDestination` has two overloads.

### Testing a registered synthetic destination

`testSyntheticDestination(key)` finds the synthetic destination bound to
`key` on the installed controller. `runEnroTest` installs an empty
controller, so register the synthetic destination with
`installNavigationModule` first. `testSyntheticDestination(key)` throws an
`IllegalStateException` when `key` is not bound to a synthetic destination:

<!-- snippet: synthetic-destinations/test-registered -->
```kotlin
@Test
fun `auth gate forwards to protected screen when logged in`() = runEnroTest {
    installNavigationModule(
        createNavigationModule {
            destination<RequireProtectedFeature>(requireProtectedFeature)
        }
    )
    isLoggedIn = true  // app-state setup

    val outcome = testSyntheticDestination(RequireProtectedFeature)

    outcome.assertOpens<AuthGateProtectedFeature>()
}
```

### Testing a provider directly

`testSyntheticDestination(key, provider)` runs the block of the
`NavigationDestinationProvider` argument. The overload does not read the
installed controller's bindings:

<!-- snippet: synthetic-destinations/test-direct -->
```kotlin
val authGate = syntheticDestination<RequireProtectedFeature> {
    if (sessionRepository.isLoggedIn) open(AuthGateProtectedFeature)
    else open(AuthGateLogin)
}

@Test
fun `auth gate redirects to login when logged out`() {
    sessionRepository.signOut()

    val outcome = testSyntheticDestination(RequireProtectedFeature, authGate)

    outcome.assertOpens<AuthGateLogin>()
}
```

### Assertion helpers

| Helper | Asserts |
|---|---|
| `assertOpens<T>(predicate?)` | The outcome is `Open` with a key of type `T` that matches `predicate`, when `predicate` is passed. Returns the key as `T`. |
| `assertCompletesFrom<T>(predicate?)` | The outcome is `CompleteFrom` with a key of type `T` that matches `predicate`, when `predicate` is passed. Returns the key as `T`. |
| `assertCloses(silent?)` | The outcome is `Close`. When `silent` is passed, the `silent` flag of the outcome equals `silent`. |
| `assertCompletes(expectedResult)` | The outcome is `Complete` with a result equal to `expectedResult`. Pass `null` for a `complete()` call with no result. |
| `assertSideEffect()` | The outcome is `SideEffect`. Returns the `SyntheticOutcome.SideEffect`. |

### Executing side-effect outcomes

`testSyntheticDestination` does not run the `sideEffect` block. Call
`runWith` on the `SyntheticOutcome.SideEffect` to run the block:

<!-- snippet: synthetic-destinations/test-side-effect -->
```kotlin
@Test
fun `external URL synthetic runs the launch side effect`() {
    val outcome = testSyntheticDestination(OpenExternalUrl("https://enro.dev"), openExternalUrl)
    outcome.assertSideEffect().runWith()
    // …assert on whatever the side effect produced (mock state, captured intents, etc.)
}
```

`runWith()` with no arguments creates a fixture `NavigationContext` and a
fixture `NavigationContainer`. Pass `context` and `container` arguments
when the `sideEffect` block reads either value.

### Limits of `testSyntheticDestination`

`testSyntheticDestination` does not execute an operation on a container.
When a synthetic block calls `open(Other)`, the test receives
`SyntheticOutcome.Open(Other)` and no backstack contains `Other`. To assert
on a backstack, create a container with the container fixtures and call
`container.execute(...)`, as `SyntheticDestinationTests` in `enro-runtime`
does.

[synthetic-outcome]: https://github.com/isaac-udy/Enro/blob/main/enro-runtime/src/commonMain/kotlin/dev/enro/ui/destinations/SyntheticOutcome.kt

## See also

- [Returning results](results.md) — the result API that `complete` and
  `completeFrom` use.
- [Recipes][synthetic-recipes] — a side effect, a conditional redirect and
  a result decider.
- [Migrating from v2](../migrating-from-v2.md) — the Enro 2 `sendResult`
  and `forwardResult` map to `complete` and `completeFrom`.

[synthetic-recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/synthetic
