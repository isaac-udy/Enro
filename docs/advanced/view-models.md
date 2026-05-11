---
title: View Models
parent: Advanced Topics
nav_order: 2
---

# View Models

Enro integrates with standard Android `ViewModel`s (and their KMP-friendly
counterparts via Compose Multiplatform). A ViewModel can read its own
destination's `NavigationHandle`, drive navigation from business logic, and
register for results — all without depending on its destination's
Composable.

## Getting a NavigationHandle in a ViewModel

Inside a `ViewModel`, use the `by navigationHandle<MyKey>()` property
delegate:

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

The handle is the same type the destination's Composable would get from
`navigationHandle<ShowProfile>()`. It's available immediately at
construction time — usable from `init { }` and any method.

The delegate accepts a `config` block, same as the Composable
`configure { }`:

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

In Compose, use the standard `viewModel { }` builder with the
`createEnroViewModel { }` helper:

```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val viewModel = viewModel<ProfileViewModel> { createEnroViewModel { ProfileViewModel() } }
    val state by viewModel.uiState.collectAsState()
    // ...
}
```

`createEnroViewModel { }` (in `dev.enro.viewmodel`) is what makes
`by navigationHandle<MyKey>()` work inside the ViewModel — it supplies the
current `NavigationHandle` to the ViewModel as it's being constructed.

You don't need any special factory. `createEnroViewModel { }` returns the
same instance the lambda produces, so you can also use it with constructor
parameters, dependency-injection containers, or whatever your project
already uses:

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

If you're using Hilt, Koin, or another DI library, build the ViewModel the
way your container does and wrap the construction call in
`createEnroViewModel { }`.

## Lifetime

A ViewModel constructed for a destination shares the destination's
backstack lifetime. It's created the first time the destination is
composed, retained across configuration changes, and cleared when the
destination is permanently removed from the backstack (i.e. after a close
or a backstack reset).

The handle held in the ViewModel is *the same handle* used inside the
destination's Composable. You can drive `open` / `close` / `complete` from
either place; results dispatched to a `registerForNavigationResult` channel
go to whichever instance registered them.

## ViewModels and results

`registerForNavigationResult` is available as a `ViewModel` extension and
uses a property delegate:

```kotlin
class HomeViewModel : ViewModel() {
    val navigation by navigationHandle<Home>()

    val pickDate by registerForNavigationResult<LocalDate>(
        onCompleted = { date -> /* update StateFlow, call use case, etc. */ },
    )

    fun onPickDateClicked() {
        pickDate.open(SelectDate(maxDate = LocalDate.now()))
    }
}
```

See [Results](results.md) for details.

## Shared state

Sharing state between destinations is normal ViewModel territory — Enro
doesn't add anything special. A common pattern is to lift the state into a
container that survives both screens (e.g. a hoisted `StateFlow` in a
parent destination, or a process-wide singleton) and have each destination's
ViewModel observe it.

For a small inline example, see the [shared ViewModel recipe][shared-recipe].

## See also

- [Navigation Handles](../core-concepts/navigation-handles.md) — the handle
  API both ViewModels and Composables use.
- [Results](results.md) — `registerForNavigationResult` in both forms.
- [Basic ViewModel recipe][basic-recipe] and
  [Shared ViewModel recipe][shared-recipe].

[basic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/viewmodel/BasicViewModel.kt
[shared-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/viewmodel/SharedViewModel.kt
