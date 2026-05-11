---
title: Navigation Handles
parent: Core Concepts
nav_order: 4
---

# Navigation Handles

A `NavigationHandle` is the control surface inside a screen — the variable
you call `open`, `close`, and `complete` on to drive navigation. Every
destination has one. You read the current key off it; you execute operations
through it.

```kotlin
val navigation = navigationHandle<ShowProfile>()

navigation.open(SelectDate(maxDate = LocalDate.now())) // open another screen
navigation.close()                                     // close this screen
navigation.complete(result)                            // close with a result (WithResult keys only)
```

## Getting a handle

### In a Composable

```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
```

`navigationHandle<KeyType>()` is a Composable function. The type parameter
makes `navigation.key` typed.

If you don't care about the key's type (because you're writing reusable
logic that doesn't depend on it), use the untyped form:

```kotlin
val navigation = navigationHandle<NavigationKey>()
```

### In a ViewModel

Use the property delegate:

```kotlin
class ProfileViewModel : ViewModel() {
    val navigation by navigationHandle<ShowProfile>()
}
```

The handle is resolved when the ViewModel is constructed by Enro's helper
factory — see [View Models](../advanced/view-models.md).

### Inside a `navigationDestination { }` provider

The provider's content lambda has a `NavigationDestinationScope<T>` receiver,
which exposes `navigation` and `key` directly:

```kotlin
@NavigationDestination(ConfirmDialog::class)
val confirmDialogDestination = navigationDestination<ConfirmDialog>(
    metadata = { dialog() }
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        confirmButton = { Button(onClick = { navigation.close() }) { Text("OK") } },
        title = { Text("Confirm") },
        text = { Text("Item: ${key /* same as navigation.key */}") },
    )
}
```

## What's on a handle

```kotlin
abstract class NavigationHandle<out T : NavigationKey> : LifecycleOwner {
    val key: T                                      // shorthand for instance.key
    abstract val instance: NavigationKey.Instance<T>
    abstract val savedStateHandle: SavedStateHandle
    abstract fun execute(operation: NavigationOperation)
}
```

- **`key`** — the key the screen was opened with, typed.
- **`instance`** — the wrapping `NavigationKey.Instance` (id + metadata).
- **`savedStateHandle`** — a per-destination `SavedStateHandle`, scoped to
  this navigation entry. Persists across configuration changes and process
  death.
- **`execute(operation)`** — the underlying primitive every operation below
  funnels through. You rarely call this directly.

`NavigationHandle` also implements `LifecycleOwner`, so you can launch
coroutines tied to the destination's lifecycle.

## Operations

All operations live in the `dev.enro` package as extension functions on
`NavigationHandle`. Import them individually.

### Opening another destination

```kotlin
navigation.open(SelectDate(maxDate = LocalDate.now()))
navigation.open(ShowProfile("user-123").withMetadata(IsExpanded, true))
```

`open` accepts either a `NavigationKey` or a `NavigationKey.WithMetadata<*>`
(produced by `key.withMetadata(MetadataKey, value)`).

For result-producing keys, prefer a result channel
(`registerForNavigationResult`) over `open` — see
[Results](../advanced/results.md).

### Closing the current destination

```kotlin
navigation.close()             // close this destination
navigation.requestClose()      // ask this destination to close
```

`close()` removes the destination unconditionally.
`requestClose()` is a *softer* form — it dispatches the request through any
interceptors configured on this destination, which may veto or transform it
(for example, to show a "discard unsaved changes?" dialog first). Use
`requestClose()` for user-initiated dismissals (back button, X button); use
`close()` from internal flows that don't want interception.

### Completing with a result

For destinations whose key implements `NavigationKey.WithResult<R>`:

```kotlin
navigation.complete(LocalDate.now())     // closes this destination and returns the value
```

Calling `complete()` with no argument on a `WithResult<R>` handle is a
compile error — a result-producing key must produce a result.

For destinations *without* a result, `complete()` with no argument closes
the destination cleanly. The distinction (`close()` vs `complete()`) matters
when the destination was opened through a `registerForNavigationResult`
channel — see [Results](../advanced/results.md).

### Composite operations

A few one-call shortcuts for common patterns:

| Operation | Effect |
|---|---|
| `navigation.closeAndReplaceWith(otherKey)` | Closes this destination and opens `otherKey` in a single atomic operation. |
| `navigation.completeFrom(anotherKey)` | Completes this destination by *delegating* to another key — opens `anotherKey`, and when *that* destination completes, this one also completes with the same result. Useful for screen "redirection". |
| `navigation.closeAndCompleteFrom(otherKey)` | Combines the above — close this destination, then route completion through `otherKey`. |

Type constraints apply: a `NavigationKey.WithResult<R>` handle can only
`completeFrom` another `NavigationKey.WithResult<R>` with the matching
result type. The compiler enforces it.

## Reading and writing per-destination state

The `savedStateHandle` on a `NavigationHandle` is scoped to that destination
and survives process death. Use it the same way you'd use a
`SavedStateHandle` on a regular Android ViewModel.

For Composable state that should survive process death, prefer
`rememberSaveable` — the navigation container's `NavigationSavedStateHolder`
takes care of scoping it correctly per destination.

## A note on lifetime

A handle is bound to one *instance* on the backstack. If your screen appears
twice in the backstack (different `Instance`s of the same key), there are
two handles in flight — one for each instance, each with its own
`savedStateHandle`.

If you've pushed your destination and then pushed something on top of it,
your handle is still valid; the destination is composed but not visible.
When the user navigates back, the same handle resumes.

## See also

- [Navigation Keys](navigation-keys.md) — what a handle is parameterised by.
- [Navigation Destinations](navigation-destinations.md) — where handles come from.
- [Results](../advanced/results.md) — `complete` and `registerForNavigationResult` in depth.
- [View Models](../advanced/view-models.md) — `by navigationHandle<T>()` inside a ViewModel.
