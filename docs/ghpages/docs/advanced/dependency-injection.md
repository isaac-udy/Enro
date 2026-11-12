---
title: Dependency Injection
parent: Advanced Topics
nav_order: 11
---

# Dependency injection

Enro does not need a dependency injection framework, and works with any of
them. There is one rule to follow.

## The rule

A ViewModel that uses `by navigationHandle<Key>()` must be constructed inside
`createEnroViewModel { }`. That call makes the destination's
`NavigationHandle` available while the ViewModel's constructor runs.

A DI framework's own ViewModel helper constructs the ViewModel somewhere
else, so the ViewModel fails with "Could not get a NavigationHandle for
ViewModel" when it reads its handle. Ask the framework for the instance
inside `createEnroViewModel { }` instead.

Dependencies come from the framework. Arguments come from the
`NavigationKey`, through the handle, so the ViewModel needs no assisted
injection.

## Koin

Declare the ViewModel in a Koin module with `factory`. The AndroidX
`viewModel()` call owns the instance's lifetime, so Koin should make a new
one each time it is asked.

<!-- snippet: di/koin-view-model -->
```kotlin
class KoinUserDetailViewModel(
    repository: UserRepository,
) : ViewModel() {
    // The key this destination was opened with: the ViewModel's arguments.
    private val navigation by navigationHandle<KoinUserDetail>()

    val title: String = repository.nameFor(navigation.key.userId)

    fun onBackClicked() = navigation.close()
}

val koinRecipeModule = module {
    single { UserRepository() }
    // factory, not viewModel: the AndroidX viewModel() call stores the instance.
    factory { KoinUserDetailViewModel(repository = get()) }
}
```

This helper asks Koin for the ViewModel inside `createEnroViewModel { }`:

<!-- snippet: di/koin-helper -->
```kotlin
@Composable
inline fun <reified T : ViewModel> koinEnroViewModel(): T {
    val koin = getKoin()
    return viewModel<T> { createEnroViewModel { koin.get<T>() } }
}
```

Use it in a destination where you would have used `koinViewModel()`:

<!-- snippet: di/koin-usage -->
```kotlin
@Composable
@NavigationDestination(KoinUserDetail::class)
fun KoinUserDetailDestination() {
    val viewModel = koinEnroViewModel<KoinUserDetailViewModel>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(viewModel.title, style = MaterialTheme.typography.titleLarge)
        Button(onClick = { viewModel.onBackClicked() }) { Text("Back") }
    }
}
```

## Other frameworks

The same shape works with any framework that can hand you an instance:

```kotlin
val viewModel = viewModel<MyViewModel> {
    createEnroViewModel { /* get a MyViewModel from your framework */ }
}
```

## Destinations across modules

Enro's annotation processor finds destinations in every module, so features
do not need DI to register their screens. See
[Multi-Module Projects](multi-module.md).

## See also

- [View Models](view-models.md) — `createEnroViewModel` and
  `by navigationHandle()`.
- [Koin ViewModel recipe][koin-recipe].

[koin-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/koin/KoinViewModel.kt
