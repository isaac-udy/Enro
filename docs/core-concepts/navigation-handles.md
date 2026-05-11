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

`close()` and `requestClose()` are both ways of dismissing a destination,
but they're not interchangeable.

- `close()` closes the destination directly.
- `requestClose()` invokes the destination's *onCloseRequested* callback.
  By default that callback just calls `close()`, so unless you've overridden
  it, `requestClose()` and `close()` do the same thing. Overriding the
  callback lets you put logic between the request and the actual close.

The Android back button calls `requestClose()`, not `close()`. Custom
"dismiss" UI in your destination (a back arrow, an X button, a backdrop tap
on a dialog) should call `requestClose()` too. As a rule of thumb,
**prefer `requestClose()` for anything user-initiated** — you'll lose
nothing today and gain the ability to add behaviour later without hunting
down every call site.

### Overriding the close-requested callback

In a Composable destination, configure the callback with
`navigation.configure { onCloseRequested { ... } }`. The `configure` block
is a `DisposableEffect`, so the callback is automatically cleaned up when
the destination leaves the composition.

```kotlin
@Composable
@NavigationDestination(EditProfile::class)
fun EditProfileScreen() {
    val navigation = navigationHandle<EditProfile>()
    var draft by remember { mutableStateOf(navigation.key.initial) }

    val confirmDiscard = registerForNavigationResult<Boolean>(
        onCompleted = { discard -> if (discard) navigation.close() },
    )

    navigation.configure {
        onCloseRequested {
            if (draft == navigation.key.initial) {
                close()              // nothing changed — close directly
            } else {
                confirmDiscard.open(ConfirmDiscardDialog)
            }
        }
    }

    // ...
}
```

In a `ViewModel`, pass a `config` block to the `by navigationHandle<T>`
delegate:

```kotlin
class EditProfileViewModel : ViewModel() {
    val draft = MutableStateFlow("")
    private val originalValue: String get() = navigation.key.initial

    val navigation by navigationHandle<EditProfile> {
        onCloseRequested {
            if (draft.value == originalValue) {
                close()
            } else {
                // delegate to a UI-side handler, emit an event, etc.
                viewModelScope.launch { closeRequests.emit(Unit) }
            }
        }
    }

    val closeRequests = MutableSharedFlow<Unit>()
}
```

The ViewModel form is configured once at construction time and cleaned up
when the ViewModel is cleared.

Two things to notice in the Composable example above:

1. The "no changes" branch calls `close()`, **not** `requestClose()` —
   calling `requestClose()` from inside the `onCloseRequested` callback
   would loop forever.
2. The confirmation dialog is opened through a `registerForNavigationResult`
   channel so the screen can react to the user's confirmation. See
   [Results](../advanced/results.md).

Other common reasons to override `onCloseRequested`:

- Block the back button while a save is in progress.
- Run a cleanup side-effect before closing.
- Redirect the close to a different operation
  (e.g. `completeFrom(otherKey)`).

A worked end-to-end example lives in the
[request-close confirmation recipe][requestclose-recipe].

### One callback per handle

A navigation handle can have at most one active `onCloseRequested` callback.
Registering more than one for the same handle — whether that's two
`configure { }` blocks in the same composable, two `by navigationHandle<K>`
properties on the same ViewModel, or one of each running concurrently — is
an error and will throw when `requestClose()` is invoked. Decide where the
callback belongs (typically the ViewModel if you have one, otherwise the
top-level Composable for the destination) and register it once.

### Closing vs completing

Every destination can be closed *or* completed — those aren't determined by
the key's type, they're determined by what the destination is trying to
communicate to its caller.

- `close()` — the screen is going away **without** finishing the task it
  was opened for. The user backed out, cancelled, dismissed the dialog, etc.
- `complete()` / `complete(result)` — the screen is going away **because**
  it finished the task it was opened for.

The distinction is delivered to whoever opened the destination through a
`registerForNavigationResult` channel: `onCompleted` fires for `complete`,
`onClosed` fires for `close`. See [Results](../advanced/results.md).

#### With a result

For destinations whose key implements `NavigationKey.WithResult<R>`,
`complete` requires the result value:

```kotlin
navigation.complete(LocalDate.now())
```

Calling `complete()` with no argument on a `WithResult<R>` handle is a
compile error — a result-producing key has no meaningful completion without
the result.

#### Without a result

For destinations whose key has no result, both `close()` and `complete()`
are valid. The choice is semantic:

```kotlin
@Composable
@NavigationDestination(ConfirmDelete::class)
fun ConfirmDeleteDialog() {
    val navigation = navigationHandle<ConfirmDelete>()
    AlertDialog(
        onDismissRequest = { navigation.requestClose() }, // user backed out
        confirmButton = {
            Button(onClick = { navigation.complete() }) { Text("Delete") } // confirmed
        },
        dismissButton = {
            Button(onClick = { navigation.close() }) { Text("Cancel") }    // cancelled
        },
    )
}
```

Even though `ConfirmDelete` has no result type, the *caller* still gets to
distinguish "the user confirmed" from "the user cancelled" by registering
both `onCompleted` and `onClosed` on its result channel.

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
- [Request-close confirmation recipe][requestclose-recipe] — full unsaved-changes example.

[requestclose-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/requestclose/RequestCloseConfirmation.kt
