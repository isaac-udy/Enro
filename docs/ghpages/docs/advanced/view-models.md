---
title: View Models
parent: Advanced Topics
nav_order: 2
---

# View Models

A destination's `ViewModel` can read the destination's `NavigationHandle`,
execute navigation operations and register for results. Enro uses the
AndroidX `ViewModel` on every platform.

## Getting a NavigationHandle in a ViewModel

Inside a `ViewModel`, declare the handle with the
`by navigationHandle<MyKey>()` property delegate:

<!-- snippet: view-models/handle -->
```kotlin
class ProfileViewModel : ViewModel() {
    val navigation by navigationHandle<ShowProfile>()

    fun onEditClicked() {
        navigation.open(EditProfile(navigation.key.userId))
    }

    fun onBackClicked() {
        navigation.requestClose()
    }
}
```

`by navigationHandle<ShowProfile>()` returns the same `NavigationHandle`
that `navigationHandle<ShowProfile>()` returns in the destination's
Composable. The handle is available while the ViewModel's constructor runs,
including in `init { }`.

The delegate takes a `config` block. The `config` block accepts the same
configuration as `navigation.configure { }` in a Composable:

<!-- snippet: view-models/config -->
```kotlin
class EditProfileViewModel : ViewModel() {
    private val draft = MutableStateFlow("")

    val navigation by navigationHandle<EditProfile>(
        config = {
            onCloseRequested {
                if (draft.value == key.initial) close() else openConfirmationDialog()
            }
        },
    )

    private fun openConfirmationDialog() { /* ... */ }
}
```

See [Navigation Handles → Overriding the close-requested callback](../core-concepts/navigation-handles.md#overriding-the-close-requested-callback).

## Constructing a ViewModel inside a destination

In Compose, construct the ViewModel inside `createEnroViewModel { }`, inside
the AndroidX `viewModel { }` builder:

<!-- snippet: view-models/create -->
```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val viewModel = viewModel<ProfileViewModel> { createEnroViewModel { ProfileViewModel() } }
    val state by viewModel.uiState.collectAsState()
    // ...
}
```

`createEnroViewModel { }` (in `dev.enro.viewmodel`) provides the
destination's `NavigationHandle` to the ViewModel while the lambda runs.
`by navigationHandle<MyKey>()` throws in a ViewModel that is constructed
outside `createEnroViewModel { }`.

`createEnroViewModel { }` returns the instance that the lambda returns. The
lambda can call a constructor with parameters or request the instance from
a dependency injection framework:

<!-- snippet: view-models/create-with-dependencies -->
```kotlin
val viewModel = viewModel<ProfileViewModel> {
    createEnroViewModel {
        ProfileViewModel(
            repository = MyContainer.userRepository,
            analytics  = MyContainer.analytics,
        )
    }
}
```

See [Dependency Injection](dependency-injection.md) for Koin and Metro.

## Lifetime

A destination's ViewModel is created the first time the destination is
composed. The ViewModel is retained across configuration changes and while
the destination is on the backstack. The ViewModel is cleared when the
destination is removed from the backstack.

`open`, `close` and `complete` have the same effect when called on the
handle in the ViewModel or on the handle in the Composable. A result is
delivered to the `registerForNavigationResult` channel that opened the
destination.

## ViewModels and results

`registerForNavigationResult` is a `ViewModel` extension that is used as a
property delegate:

<!-- snippet: results/view-model -->
```kotlin
class HomeViewModel : ViewModel() {
    val navigation by navigationHandle<Home>()

    val pickDate by registerForNavigationResult<LocalDate>(
        onCompleted = { date ->
            // Update a StateFlow or call a use case here.
        },
    )

    fun onPickDateClicked() {
        pickDate.open(SelectDate(maxDate = today))
    }
}
```

See [Results](results.md).

## Shared state

Enro has no API for sharing state between destinations. To share state,
store the state in an object that outlives both destinations, such as a
`StateFlow` in a parent destination's ViewModel or a singleton, and observe
the state from each destination's ViewModel.

A child destination that edits one value does not need shared state. Pass
the current value in the child's `NavigationKey` and return the edited
value as a result. See the
[sharing state with results recipe][shared-recipe].

## See also

- [Navigation Handles](../core-concepts/navigation-handles.md) — the
  `NavigationHandle` API.
- [Results](results.md) — `registerForNavigationResult` in a Composable and
  in a ViewModel.
- [Basic ViewModel recipe][basic-recipe] and
  [Sharing state with results recipe][shared-recipe].

[basic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/viewmodel/BasicViewModel.kt
[shared-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/viewmodel/SharedStateWithResults.kt
