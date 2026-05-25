---
title: Synthetic Destinations
parent: Advanced Topics
nav_order: 6
---

# Synthetic Destinations

A synthetic destination is a `NavigationKey` whose "destination" is a block
of code rather than a piece of UI. When the key is opened, the block runs;
the synthetic instance itself never lands on any backstack. Synthetics are
the bridge between Enro's navigation API and behaviours that don't fit a
"render a screen" model — things like launching an external browser,
deciding which of several implementations should fulfil a contract, or
composing a multi-step action behind a single call site.

A synthetic looks like any other destination from the caller's side:

```kotlin
navigation.open(Logout)        // synthetic
navigation.open(EditProfile)   // synthetic — could be either of two screens
navigation.open(SettingsScreen) // ordinary destination
```

The caller doesn't need to know — or care — that some keys resolve to a
block of code instead of a Composable.

## When to reach for a synthetic

There are three patterns the recipes (see [recipes][synthetic-recipes])
cover end-to-end, and they're a fair guide for when a synthetic is the
right tool:

- **Side-effect bridge** — wrap a non-Enro action behind a navigation
  call. E.g. `OpenExternalUrl(url)` that launches Chrome Custom Tabs on
  Android, `Desktop.browse(...)` on JVM, `window.open(...)` on web.
- **Conditional redirect** — gate a destination behind a runtime check.
  `RequireLogin` checks the auth state and forwards to either the
  requested screen or a login screen, so callers don't have to know
  about the gate.
- **Result decider** — a result-bearing synthetic picks between several
  concrete implementations (legacy / V2, feature flag A / feature flag
  B, mobile-only / wide-screen-only) and forwards the chosen one's
  result back to the original caller via `completeFrom`.

If the behaviour really is "render this screen with some twist," prefer a
normal destination with the twist expressed in metadata. Reach for a
synthetic when there's no screen to render, or when the choice of which
screen depends on runtime state.

## Declaring a synthetic

`syntheticDestination<K>` builds a `NavigationDestinationProvider<K>` that
the KSP processor binds the same way as any other destination:

```kotlin
@Serializable
object Logout : NavigationKey

@NavigationDestination(Logout::class)
val logout = syntheticDestination<Logout> {
    sessionRepository.clearSession()
    open(LoginScreen())
}
```

Inside the block, you have a `SyntheticDestinationScope<K>` receiver with
the key and instance available, plus the outcome methods covered below.

## The outcome methods

Each method ends the synthetic by deciding what should happen next. They
all return `Nothing` — calling one short-circuits the block. They split
into two kinds: **pure outcomes** that are rewritten in place as the
synthetic's surrounding `processOperations` pass runs, and a
**side-effect outcome** that runs deferred after the pass settles.

### Pure outcomes — rewritten in place

| Method | What it does |
|---|---|
| `open(key)` | Opens `key` on the same container the synthetic was opened on. The dispatcher rewrites the original `Open(synthetic)` into `Open(key)` inside `processOperations`, so the new operation flows through the interceptor chain in the same pass. Ordering is preserved when synthetics appear in an initial backstack alongside normal destinations. |
| `close()` | Ends the synthetic and registers a `Closed` result against whoever opened the synthetic. The caller's `onClosed` callback fires. |
| `closeSilently()` | Same as `close()` but the result-channel callback does **not** fire. Use when the caller doesn't need to be notified. |
| `complete()` | Ends the synthetic and registers a `Completed` result with no payload. Only available for non-result keys. |
| `complete(result)` | Same as `complete()` but with a typed payload. Only available when the synthetic's key implements `NavigationKey.WithResult<R>`. |
| `completeFrom(key)` | Opens `key` and routes *its* eventual completion back to whoever opened the synthetic. The synthetic doesn't produce the result; the forwarded key does. |

### Side-effect outcome — runs deferred

| Method | What it does |
|---|---|
| `sideEffect { ... }` | The block runs in `afterExecution`, with a [`SyntheticSideEffectScope`](#side-effect-scope) receiver carrying `context`, `container`, `instance`, and `key`. The synthetic is treated as silently closed once the side effect dispatches. Use this when you need platform handles, the container reference, or any imperative work that doesn't fit a single navigation operation. |

The methods throw a sentinel exception that the synthetic dispatcher
catches and converts into a `NavigationOperation`. Don't catch
`Throwable` inside a synthetic block — you'll swallow the outcome.

## Pure outcomes vs side effects

The split matters because the two kinds run at different points in the
operation lifecycle.

A **pure outcome** is a deterministic rewrite of the synthetic's `Open`.
The dispatcher catches the outcome and returns the equivalent operation
from inside its interceptor — so the rewrite is part of the same
`processOperations` pass that handled the original `Open(synthetic)`.
For example, if an initial backstack is `[ASynthetic, B]` and the
synthetic opens `Target`, the resulting backstack is `[Target, B]` —
the synthetic's outcome takes the synthetic's slot in the queue, not the
end of the queue.

A **side-effect outcome** runs deferred, in `afterExecution`. By the time
the block runs, every other operation in the same pass has already
settled. That's the point at which you can safely:

- Touch platform handles (find an `Activity`, get a Compose
  `WindowInfo`, etc.).
- Read or rewrite the container's backstack
  (`container.execute(context, NavigationOperation.SetBackstack(...))`).
- Make any other imperative call that doesn't fit a single
  `NavigationOperation`.

The two recipes that ship in `recipes/synthetic` illustrate the split:
the auth gate and profile decider both use pure outcomes; the external
URL launcher uses `sideEffect { ... }` because it bridges to a non-Enro
API.

## Falling through

A synthetic block that returns without calling any outcome method is
treated as a **silent close**. No result-channel callback fires; no
navigation operation is dispatched against the synthetic's instance. The
block ran, did its thing, and Enro doesn't need to do anything further.

This is the default for synthetics that only do pure side effects — but
note that in the current model you should prefer `sideEffect { ... }`
for that work, so you get explicit access to `container` and `context`
deferred to the right point. Pure synthetics that genuinely do *no*
imperative work and only return a fixed outcome (like a decider that
always picks one of two destinations) tend to be one-line bodies that
short-circuit through `open()` or `completeFrom(...)`.

## Forwarding results with `completeFrom`

The result-decider pattern is the most distinctive synthetic use case.
Your public-facing key has a `WithResult<R>` contract; the synthetic
picks one of several concrete implementations at runtime and lets *that*
implementation produce the result:

```kotlin
@Serializable
object EditProfile : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileLegacy : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileV2 : NavigationKey.WithResult<ProfileUpdate>

@NavigationDestination(EditProfile::class)
val editProfile = syntheticDestination<EditProfile> {
    // Read a feature flag, A/B bucket, remote config, etc.
    if (featureFlags.useNewProfileUi) completeFrom(EditProfileV2)
    else completeFrom(EditProfileLegacy)
}
```

The caller subscribes to `EditProfile` and never needs to know which
underlying screen actually ran:

```kotlin
val editProfile = registerForNavigationResult<ProfileUpdate>(
    onCompleted = { update -> /* one branch, regardless of which UI produced it */ },
)
Button(onClick = { editProfile.open(EditProfile) }) { Text("Edit") }
```

Under the hood, `completeFrom` copies the synthetic's result-channel
metadata onto the forwarded instance, so when the forwarded key calls
`navigation.complete(value)`, the value routes back through to the
original caller's channel.

## The "already finished" error

Synthetic blocks should be **synchronous**. The dispatcher catches the
outcome thrown from the block and acts on it; any code that runs *after*
the block returns (e.g. a coroutine the block launched on
`lifecycleScope`) operates against a scope that has already concluded.

If async code calls an outcome method on the scope after the block has
returned, the scope throws:

```
SyntheticDestination for X has already finished with Close.
A second outcome cannot be set — this usually means an async coroutine
outlived the synthetic block and tried to complete/close it after the
dispatcher had already moved on. Do any async work before opening the
synthetic, or forward to a destination that owns the work itself.
```

The fix is one of:

- **Do the async work *before* the synthetic** and pass the result in as
  a parameter of the synthetic's key.
- **Forward to a real destination** that owns the async work as part of
  its lifecycle (`completeFrom(WorkInProgressScreen)`).
- **Restructure as a regular destination with a loading state** if the
  synthetic was really trying to be a "do some work then close" UI.

There is a deliberate design choice here: synthetics are intentionally
small, synchronous decision points. Letting them survive across coroutine
boundaries would require giving them their own lifecycle, view model
store, and serialised state — i.e. making them ordinary destinations.
That direction is being considered as a future "lifecycle-bearing
synthetic" mode but isn't on the current roadmap.

## Accessing the originating context

### From the pure scope — reads only

`SyntheticDestinationScope<K>` exposes `context: NavigationContext` and
the derived `destinationContext`. These are intended for **reads**:
inspecting `controller`, walking parent contexts, checking which
destination opened the synthetic. They're available so a pure outcome
can be informed by the surrounding navigation state.

You can technically reach `context.controller` and other writable handles
from inside a pure block, but doing so runs while the container's
execute mutex is held — direct `controller.execute(...)` or
`container.execute(...)` calls deadlock. That's the framework signalling
that you should be using `sideEffect { ... }` instead.

### From the side-effect scope — full access

`SyntheticSideEffectScope<K>` (the receiver of the `sideEffect` block)
exposes `context`, `container`, `instance`, and `key`. By the time the
side-effect block runs, the mutex is released and the backstack has
settled, so imperative work is safe:

```kotlin
@NavigationDestination(OpenExternalUrl::class)
val openExternalUrl = syntheticDestination<OpenExternalUrl> {
    sideEffect {
        val activity = context.findActivity()  // Android extension
        activity.startActivity(Intent(ACTION_VIEW, Uri.parse(key.url)))
    }
}

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

enro-test provides `testSyntheticDestination(...)` for unit-testing
synthetic logic without going through a real container or interceptor
pipeline. It runs the synthetic's block in a sandbox scope and returns
a [`SyntheticOutcome`][synthetic-outcome] describing what the block
decided.

Two entry points cover the two common test shapes:

### Registered path — via the installed controller

Use this when the synthetic is registered through a `NavigationModule` on
your component, as it would be in production:

```kotlin
@Test
fun `auth gate forwards to protected screen when logged in`() = runEnroTest {
    MyComponent.installNavigationController(this)
    isLoggedIn = true  // app-state setup

    val outcome = testSyntheticDestination(RequireProtectedFeature)

    outcome.assertOpens<AuthGateProtectedFeature>()
}
```

### Direct path — provider passed in

Use this for pure unit tests that don't need a controller installed. Pass
the `NavigationDestinationProvider` value directly:

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
| `assertOpens<T>(predicate?)` | Outcome is `Open` of a key of type `T`, optionally matching `predicate`. Returns the typed key. |
| `assertCompletesFrom<T>(predicate?)` | Outcome is `CompleteFrom` of a key of type `T`. Returns the typed key. |
| `assertCloses(silent?)` | Outcome is `Close`, optionally matching the `silent` flag. |
| `assertCompletes(expectedResult)` | Outcome is `Complete` with the given payload. Pass `null` for non-result synthetics. |
| `assertSideEffect()` | Outcome is `SideEffect`. Returns the side-effect so you can run it (see below). |

### Executing side-effect outcomes

A `SideEffect` outcome carries the block but doesn't auto-invoke it —
the test gets to decide whether to run it, and with what scope:

```kotlin
@Test
fun `external URL synthetic runs the launch side effect`() {
    val outcome = testSyntheticDestination(OpenExternalUrl("https://enro.dev"), openExternalUrl)
    outcome.assertSideEffect().runWith()
    // …assert on whatever the side effect produced (mock state, captured intents, etc.)
}
```

`runWith()` with no arguments uses default fixture context and container
— enough for most tests. Pass explicit `context: NavigationContext` and
`container: NavigationContainer` arguments when the side effect's
behaviour depends on either.

### What you DON'T get from the test helper

`testSyntheticDestination` runs the block in isolation — it doesn't
dispatch operations against any container's backstack. So if your
synthetic calls `open(Other)`, the test sees a `SyntheticOutcome.Open(Other)`
but `Other` doesn't actually land in any backstack. For end-to-end
backstack assertions, fall back to the container fixtures and execute
the operation through `container.execute(...)` as the
`SyntheticDestinationTests` in enro-runtime do.

[synthetic-outcome]: https://github.com/isaac-udy/Enro/blob/main/enro-runtime/src/commonMain/kotlin/dev/enro/ui/destinations/SyntheticOutcome.kt

## See also

- [Returning results](results.md) — the result contract that `complete`
  and `completeFrom` plug into.
- [Recipes][synthetic-recipes] — three worked examples corresponding to
  the three patterns above.
- [Migrating from v2](../migrating-from-v2.md) — `sendResult` /
  `forwardResult` from Enro 2 are now `complete` / `completeFrom`.

[synthetic-recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/src/commonMain/kotlin/dev/enro/recipes/synthetic
