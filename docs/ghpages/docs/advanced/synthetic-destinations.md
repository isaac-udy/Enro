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
all return `Nothing` — calling one short-circuits the block.

| Method | What it does |
|---|---|
| `open(key)` | Opens `key` on the same container the synthetic was opened on. The synthetic instance itself never lands on any backstack. |
| `close()` | Ends the synthetic and registers a `Closed` result against whoever opened the synthetic. The caller's `onClosed` callback fires. |
| `closeSilently()` | Same as `close()` but the result-channel callback does **not** fire. Use when the caller doesn't need to be notified. |
| `complete()` | Ends the synthetic and registers a `Completed` result with no payload. Only available for non-result keys. |
| `complete(result)` | Same as `complete()` but with a typed payload. Only available when the synthetic's key implements `NavigationKey.WithResult<R>`. |
| `completeFrom(key)` | Opens `key` and routes *its* eventual completion back to whoever opened the synthetic. The synthetic doesn't produce the result; the forwarded key does. |

The methods throw a sentinel exception that the synthetic dispatcher
catches and converts into a `NavigationOperation`. Don't catch
`Throwable` inside a synthetic block — you'll swallow the outcome.

## Falling through

A synthetic block that returns without calling any outcome method is
treated as a **silent close**. No result-channel callback fires; no
navigation operation is dispatched against the synthetic's instance. The
block ran, did its thing, and Enro doesn't need to do anything further.

```kotlin
@NavigationDestination(OpenExternalUrl::class)
val openExternalUrl = syntheticDestination<OpenExternalUrl> {
    val activity = context.findActivity()
    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(key.url)))
    // no outcome method — falls through to silent close
}
```

This is the most common shape for pure side-effect bridges. Callers
generally don't subscribe to a result for these, so the silent close
is the right default.

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

The scope exposes `context: NavigationContext` — the context from which
the synthetic was opened. Use it when the block needs platform handles
that aren't part of Enro itself:

```kotlin
@NavigationDestination(OpenExternalUrl::class)
val openExternalUrl = syntheticDestination<OpenExternalUrl> {
    val activity = context.findActivity()  // Android extension
    activity.startActivity(Intent(ACTION_VIEW, Uri.parse(key.url)))
}
```

`context.controller` reaches the `EnroController`, which is the entry
point to the path-binding APIs, registered serializers, and so on.

## See also

- [Returning results](results.md) — the result contract that `complete`
  and `completeFrom` plug into.
- [Recipes][synthetic-recipes] — three worked examples corresponding to
  the three patterns above.
- [Migrating from v2](../migrating-from-v2.md) — `sendResult` /
  `forwardResult` from Enro 2 are now `complete` / `completeFrom`.

[synthetic-recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/src/commonMain/kotlin/dev/enro/recipes/synthetic
