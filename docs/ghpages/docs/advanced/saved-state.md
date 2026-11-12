---
title: Saved State
parent: Advanced Topics
nav_order: 10
---

# Saved state

A destination can store state in four places. The table shows whether each
place keeps the state after each event.

| Stored in | Destination covered by another destination | Configuration change | Process death |
|---|---|---|---|
| `remember` | lost | lost | lost |
| `rememberSaveable` | kept | kept | kept |
| a ViewModel property | kept | kept | lost |
| the ViewModel's `SavedStateHandle` | kept | kept | kept |

## Covered destinations

With the default scenes, a destination is removed from the composition when
another destination is opened on top of the destination. While a covered
destination is on the backstack, Enro keeps the destination's
`rememberSaveable` state and ViewModel. Enro restores both when the
destination is displayed again. State stored with `remember` is lost.

Use `rememberSaveable` for UI state that must be restored when the user
returns to the destination: text in a field, a scroll position, a selected
tab.

## The backstack

`rememberNavigationContainer` saves the container's backstack with
`rememberSaveable`. After a configuration change or process death,
`rememberNavigationContainer` restores the saved backstack and ignores the
`backstack` argument.

Each `NavigationKey` on the backstack is written to saved state, so a
`NavigationKey` must be `@Serializable`.

## In a ViewModel

A ViewModel is retained while the ViewModel's destination is on the
backstack, including across configuration changes. A ViewModel is not
restored after process death. For state that must be restored after process
death, pass a `SavedStateHandle` to the ViewModel:

<!-- snippet: saved-state/view-model -->
```kotlin
class CountersViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // A ViewModel property is lost on process death.
    private val _inMemory = MutableStateFlow(0)
    val inMemory: StateFlow<Int> = _inMemory.asStateFlow()

    // A SavedStateHandle value is restored after process death.
    val saved: StateFlow<Int> = savedStateHandle.getStateFlow("saved", 0)

    fun increment() {
        _inMemory.update { it + 1 }
        savedStateHandle["saved"] = saved.value + 1
    }
}
```

`createSavedStateHandle()` creates a `SavedStateHandle` scoped to the
destination:

<!-- snippet: saved-state/destination -->
```kotlin
var remembered by remember { mutableIntStateOf(0) }
var saveable by rememberSaveable { mutableIntStateOf(0) }
// createSavedStateHandle() creates a SavedStateHandle scoped to this destination.
val viewModel = viewModel<CountersViewModel> {
    createEnroViewModel { CountersViewModel(createSavedStateHandle()) }
}
```

## Per platform

- **Android**: the Activity saves the state, so all three columns of the
  table apply. To test process death, turn on "Don't keep activities" in
  Developer Options, send the app to the background and return to the app.
- **Web**: state is kept while the page is loaded. After a full page
  reload, the app has only the URL; see [Web](../platform/web.md).

## See also

- [Saved State recipe][saved-state-recipe] — one counter for each row of the
  table.
- [Saveable Back Stack recipe][saveable-recipe].
- [Navigation Handles → Saved state](../core-concepts/navigation-handles.md).

[saved-state-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/saveable/SavedState.kt
[saveable-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/saveable/SaveableBackStack.kt
