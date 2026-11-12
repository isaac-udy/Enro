---
title: Saved State
parent: Advanced Topics
nav_order: 10
---

# Saved state

A destination can keep state in four places. They differ in what they
survive.

| Stored in | Covered by another screen | Configuration change | Process death |
|---|---|---|---|
| `remember` | lost | lost | lost |
| `rememberSaveable` | kept | kept | kept |
| a ViewModel property | kept | kept | lost |
| the ViewModel's `SavedStateHandle` | kept | kept | kept |

## Covered destinations

With the default scenes, a destination leaves the composition when another
destination is opened on top of it. While it is on the backstack, Enro keeps
its `rememberSaveable` state and its ViewModel, and restores both when the
destination is shown again. State held in plain `remember` is lost.

Use `rememberSaveable` for UI state that should still be there when the user
comes back: text in a field, a scroll position, a selected tab.

## The backstack

A container's backstack is saved the same way as `rememberSaveable` state.
After a configuration change or process death, `rememberNavigationContainer`
restores the backstack it had, and ignores the `backstack` argument.

This is why a `NavigationKey` must be `@Serializable`: the keys on the
backstack are written to saved state.

## In a ViewModel

A ViewModel lives as long as its destination is on the backstack, across
configuration changes. It does not survive process death. For state that
must, give the ViewModel a `SavedStateHandle`:

<!-- snippet: saved-state/view-model -->
```kotlin
class CountersViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // Lives as long as the ViewModel: lost on process death.
    private val _inMemory = MutableStateFlow(0)
    val inMemory: StateFlow<Int> = _inMemory.asStateFlow()

    // Written to saved state: restored after process death.
    val saved: StateFlow<Int> = savedStateHandle.getStateFlow("saved", 0)

    fun increment() {
        _inMemory.update { it + 1 }
        savedStateHandle["saved"] = saved.value + 1
    }
}
```

`createSavedStateHandle()` creates a handle scoped to the destination:

<!-- snippet: saved-state/destination -->
```kotlin
var remembered by remember { mutableIntStateOf(0) }
var saveable by rememberSaveable { mutableIntStateOf(0) }
// createSavedStateHandle() gives the ViewModel a handle scoped to this destination.
val viewModel = viewModel<CountersViewModel> {
    createEnroViewModel { CountersViewModel(createSavedStateHandle()) }
}
```

## Per platform

- **Android** saves state through the Activity, so all three columns apply.
  To test process death, turn on "Don't keep activities" in Developer
  Options, send the app to the background and bring it back.
- **Web** keeps state while the page is loaded. A full page reload starts
  from the URL alone; see [Web](../platform/web.md).

## See also

- [Saved State recipe][saved-state-recipe] — the four counters above.
- [Saveable Back Stack recipe][saveable-recipe].
- [Navigation Handles → Saved state](../core-concepts/navigation-handles.md).

[saved-state-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/saveable/SavedState.kt
[saveable-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/saveable/SaveableBackStack.kt
