---
title: Navigation Handles
parent: Core Concepts
nav_order: 4
---

# Navigation Handles

Each destination has one `NavigationHandle`. A screen reads the key from
the `NavigationHandle` and calls `open`, `close` and `complete` on the
`NavigationHandle`.

<!-- snippet: basic-concepts/handle -->
```kotlin
val navigation = navigationHandle<SelectDate>()

navigation.open(ShowProfile("user-123"))
navigation.close()
navigation.complete(today) // closes SelectDate and returns `today` to the caller
```

## Getting a handle

### In a Composable

<!-- snippet: navigation-handles/composable -->
```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
```

`navigationHandle<KeyType>()` is a Composable function. `navigation.key` has
the type `KeyType`. `navigationHandle<KeyType>()` throws when the
destination's key is not a `KeyType`.

For code that does not depend on the key's type, use `NavigationKey` as
the type parameter:

<!-- snippet: navigation-handles/untyped -->
```kotlin
val navigation = navigationHandle<NavigationKey>()
```

### In a ViewModel

Declare the handle with the `by navigationHandle<KeyType>()` property
delegate:

<!-- snippet: navigation-handles/view-model -->
```kotlin
class ProfileViewModel : ViewModel() {
    val navigation by navigationHandle<ShowProfile>()
}
```

The delegate reads the handle when the ViewModel is constructed. The
delegate throws when the ViewModel was not created by a destination's
ViewModel factory, or when the destination's key is not a `KeyType`. See
[View Models](../advanced/view-models.md).

### Inside a `navigationDestination { }` provider

The provider's content lambda has a `NavigationDestinationScope<T>` receiver,
which has a `navigation` property:

<!-- snippet: navigation-handles/provider -->
```kotlin
@NavigationDestination(ConfirmDialog::class)
val confirmDialogDestination = navigationDestination<ConfirmDialog>(
    metadata = { dialog() }
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        confirmButton = { Button(onClick = { navigation.close() }) { Text("OK") } },
        title = { Text("Confirm") },
        text = { Text("Opened with ${navigation.key}") },
    )
}
```

## NavigationHandle members

<!-- snippet: skip (API outline) -->
```kotlin
abstract class NavigationHandle<out T : NavigationKey> : LifecycleOwner {
    val key: T
    abstract val instance: NavigationKey.Instance<T>
    abstract val savedStateHandle: SavedStateHandle
    abstract fun execute(operation: NavigationOperation)
}
```

- **`key`**: the key the screen was opened with. `key` returns
  `instance.key`.
- **`instance`**: the `NavigationKey.Instance` that contains the key, the
  `id` and the instance metadata.
- **`savedStateHandle`**: a `SavedStateHandle` scoped to the instance.
  Enro restores the `SavedStateHandle` after a configuration change and
  after process death.
- **`execute(operation)`**: executes a `NavigationOperation`. Each
  operation in the next section calls `execute`.

`NavigationHandle` implements `LifecycleOwner`. The lifecycle of the
`NavigationHandle` follows the lifecycle of the destination.

## Operations

Each operation is an extension function on `NavigationHandle` in the
`dev.enro` package. Import each operation.

### Opening another destination

<!-- snippet: navigation-handles/open -->
```kotlin
navigation.open(SelectDate(maxDate = today))
navigation.open(ShowProfile("user-123").withMetadata(IsExpanded, true))
```

`open` accepts a `NavigationKey` or a `NavigationKey.WithMetadata<*>`.
`key.withMetadata(MetadataKey, value)` returns a
`NavigationKey.WithMetadata<*>`.

To receive the result of a `NavigationKey.WithResult` key, open the key
with a `registerForNavigationResult` channel instead of `open`. See
[Results](../advanced/results.md).

### Closing the current destination

<!-- snippet: navigation-handles/close -->
```kotlin
navigation.close()
navigation.requestClose()
```

- `close()` closes the destination. `close()` does not call the
  `onCloseRequested` callback.
- `requestClose()` calls the destination's `onCloseRequested` callback.
  When the destination has registered no `onCloseRequested` callback,
  `requestClose()` closes the destination.

The system back button and the back gesture call `requestClose()`. Call
`requestClose()` from UI that dismisses the destination, such as a back
arrow, a close button or a tap outside a dialog. An `onCloseRequested`
callback then applies to every dismissal.

### Overriding the close-requested callback

In a Composable destination, register the callback with
`navigation.configure { onCloseRequested { ... } }`. `configure` runs the
block in a `DisposableEffect` and removes the callback when the
`configure` call leaves the composition.

<!-- snippet: navigation-handles/configure -->
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
                close()
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

<!-- snippet: navigation-handles/configure-view-model -->
```kotlin
class EditProfileViewModel : ViewModel() {
    val draft = MutableStateFlow("")
    private val originalValue: String get() = navigation.key.initial

    val navigation by navigationHandle<EditProfile> {
        onCloseRequested {
            if (draft.value == originalValue) {
                close()
            } else {
                // The destination's UI collects closeRequests.
                viewModelScope.launch { closeRequests.emit(Unit) }
            }
        }
    }

    val closeRequests = MutableSharedFlow<Unit>()
}
```

Enro applies the `config` block when the ViewModel is constructed and
removes the callback when the ViewModel is cleared.

In the Composable example:

1. The callback calls `close()` when the draft is unchanged. A call to
   `requestClose()` inside the `onCloseRequested` callback calls the
   callback again, without end.
2. The screen opens the confirmation dialog with a
   `registerForNavigationResult` channel and closes when the dialog
   completes with `true`. See [Results](../advanced/results.md).

Other uses of `onCloseRequested`:

- Ignore the back button while a save is in progress.
- Run cleanup before calling `close()`.
- Call a different operation, such as `completeFrom(otherKey)`.

See the [request-close confirmation recipe][requestclose-recipe].

### One callback per handle

A `NavigationHandle` can have one active `onCloseRequested` callback.
`requestClose()` throws when the `NavigationHandle` has more than one
active callback. Each of these combinations registers two callbacks: two
`configure { }` blocks in the same destination, two
`by navigationHandle<K> { }` properties in the same ViewModel, or one
`configure { }` block and one ViewModel `config` block. Register the
callback in the ViewModel when the destination has
a ViewModel, and otherwise in the destination's top-level Composable.

### Closing vs completing

A destination with any key can call `close()` or `complete()`. The call
tells the caller whether the destination finished the task.

- `close()`: the destination closes without finishing the task. Examples:
  the user pressed back, cancelled or dismissed a dialog.
- `complete()` or `complete(result)`: the destination closes because the
  task is finished.

A caller that opened the destination with a `registerForNavigationResult`
channel receives `onCompleted` for `complete` and `onClosed` for `close`.
See [Results](../advanced/results.md).

#### With a result

When the key implements `NavigationKey.WithResult<R>`, `complete` requires
the result value:

<!-- snippet: navigation-handles/complete-with-result -->
```kotlin
navigation.complete(today)
```

A call to `complete()` with no argument on a `NavigationHandle` for a
`WithResult<R>` key is a compile error.

#### Without a result

When the key has no result, the destination can call `close()` or
`complete()`:

<!-- snippet: navigation-handles/complete-without-result -->
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

`ConfirmDelete` has no result type. The caller distinguishes a confirmed
delete from a cancelled delete by registering `onCompleted` and `onClosed`
on the result channel.

### Composite operations

| Operation | Effect |
|---|---|
| `navigation.closeAndReplaceWith(otherKey)` | Closes the destination and opens `otherKey` as one operation. |
| `navigation.completeFrom(otherKey)` | Opens `otherKey` and leaves the destination on the backstack. When the `otherKey` destination completes, the caller of the destination receives the result. |
| `navigation.closeAndCompleteFrom(otherKey)` | Closes the destination and opens `otherKey` as one operation. When the `otherKey` destination completes, the caller of the closed destination receives the result. |

On a `NavigationHandle` for a `NavigationKey.WithResult<R>` key,
`completeFrom` and `closeAndCompleteFrom` accept only a
`NavigationKey.WithResult<R>` key with the same result type `R`. Any other
key is a compile error.

## Reading and writing per-destination state

The `savedStateHandle` of a `NavigationHandle` is scoped to one destination
instance. Enro restores the `savedStateHandle` after process death. The
`savedStateHandle` has the same API as the `SavedStateHandle` of an Android
ViewModel.

For Composable state, use `rememberSaveable`. The container's
`NavigationSavedStateHolder` scopes `rememberSaveable` state to each
destination instance.

## Handle lifetime

A `NavigationHandle` is bound to one `Instance` on the backstack. When a
backstack contains two `Instance`s of the same key, each `Instance` has a
separate `NavigationHandle` and a separate `savedStateHandle`.

A `NavigationHandle` stays valid while another destination is open above
the handle's destination. With the default scenes, the covered destination
leaves the composition and Enro keeps the covered destination's saved
state. When the user navigates back, Enro composes the destination again
with the same `NavigationHandle` and the saved state.

## See also

- [Navigation Keys](navigation-keys.md): the key type that a `NavigationHandle` is typed with.
- [Navigation Destinations](navigation-destinations.md): declaring the destination that a `NavigationHandle` belongs to.
- [Results](../advanced/results.md): `complete` and `registerForNavigationResult`.
- [View Models](../advanced/view-models.md): `by navigationHandle<T>()` inside a ViewModel.
- [Request-close confirmation recipe][requestclose-recipe]: an unsaved-changes confirmation.

[requestclose-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/requestclose/RequestCloseConfirmation.kt
