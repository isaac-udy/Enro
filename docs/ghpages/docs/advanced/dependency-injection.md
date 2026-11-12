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

### Features that each provide a Koin module

The app loads one Koin module per feature:

<!-- snippet: di/koin-modular-app -->
```kotlin
val appModule = module {
    // One instance for every feature.
    single { KoinSession() }
}

// The app loads its own module and one module per feature.
private val recipeKoin = koinApplication {
    modules(appModule, ordersModule, accountModule)
}
```

A feature's module provides its ViewModels. Its destinations are registered
by the `enro-processor`, not by Koin.

<!-- snippet: di/koin-modular-feature -->
```kotlin
class OrdersViewModel(
    private val session: KoinSession,
    repository: OrderRepository,
) : ViewModel() {
    private val navigation by navigationHandle<KoinOrders>()

    val orders: List<String> = repository.orders()
    val userName: String get() = session.userName

    // The account screen is in another feature. It is opened by key.
    fun onAccountClicked() = navigation.open(KoinAccount)
}

// What this feature provides. KoinSession comes from the app's module.
val ordersModule = module {
    single { OrderRepository() }
    factory { OrdersViewModel(session = get(), repository = get()) }
}

@Composable
@NavigationDestination(KoinOrders::class)
fun KoinOrdersDestination() {
    val viewModel = koinEnroViewModel<OrdersViewModel>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Orders for ${viewModel.userName}", style = MaterialTheme.typography.titleLarge)
        viewModel.orders.forEach { Text(it) }
        Button(onClick = { viewModel.onAccountClicked() }) { Text("Account") }
    }
}
```

## Metro

Add the ViewModel to the graph as an accessor, with no scope. Each read of
the accessor constructs a new ViewModel.

<!-- snippet: di/metro-view-model -->
```kotlin
@Inject
class MetroUserDetailViewModel(
    repository: MetroUserRepository,
) : ViewModel() {
    // The key this destination was opened with: the ViewModel's arguments.
    private val navigation by navigationHandle<MetroUserDetail>()

    val title: String = repository.nameFor(navigation.key.userId)

    fun onBackClicked() = navigation.close()
}

@DependencyGraph
interface MetroRecipeGraph {
    val userRepository: MetroUserRepository

    // Not scoped: each read constructs a new ViewModel.
    val userDetailViewModel: MetroUserDetailViewModel
}
```

Read the accessor inside `createEnroViewModel { }`:

<!-- snippet: di/metro-usage -->
```kotlin
@Composable
@NavigationDestination(MetroUserDetail::class)
fun MetroUserDetailDestination() {
    val viewModel = viewModel<MetroUserDetailViewModel> {
        createEnroViewModel { recipeGraph.userDetailViewModel }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(viewModel.title, style = MaterialTheme.typography.titleLarge)
        Button(onClick = { viewModel.onBackClicked() }) { Text("Back") }
    }
}
```

### Features that contribute to one graph

Declare the app's graph with a scope. Features get the graph from a
`CompositionLocal`.

<!-- snippet: di/metro-modular-graph -->
```kotlin
// Implements every interface contributed to AppScope.
@DependencyGraph(AppScope::class)
interface MetroModularGraph

val LocalMetroGraph = staticCompositionLocalOf<Any> {
    error("No Metro graph was provided")
}

/**
 * Creates [T] from the feature's graph interface [G], inside
 * `createEnroViewModel` so that `by navigationHandle()` works in the ViewModel.
 */
@Composable
inline fun <reified G : Any, reified T : ViewModel> metroEnroViewModel(
    crossinline create: (G) -> T,
): T {
    val graph = LocalMetroGraph.current as G
    return viewModel<T> { createEnroViewModel { create(graph) } }
}
```

Each feature contributes an interface with the accessors it needs, and casts
the graph to that interface:

<!-- snippet: di/metro-modular-feature -->
```kotlin
@Inject
class OrdersViewModel(
    private val session: MetroSession,
    repository: OrderRepository,
) : ViewModel() {
    private val navigation by navigationHandle<MetroOrders>()

    val orders: List<String> = repository.orders()
    val userName: String get() = session.userName

    // The account screen is in another feature. It is opened by key.
    fun onAccountClicked() = navigation.open(MetroAccount)
}

// What this feature needs from the app's graph.
@ContributesTo(AppScope::class)
interface OrdersGraph {
    val ordersViewModel: OrdersViewModel
}

@Composable
@NavigationDestination(MetroOrders::class)
fun MetroOrdersDestination() {
    val viewModel = metroEnroViewModel<OrdersGraph, OrdersViewModel> { it.ordersViewModel }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Orders for ${viewModel.userName}", style = MaterialTheme.typography.titleLarge)
        viewModel.orders.forEach { Text(it) }
        Button(onClick = { viewModel.onAccountClicked() }) { Text("Account") }
    }
}
```

## Other frameworks

The same shape works with any framework that can hand you an instance:

<!-- snippet: di/other-frameworks -->
```kotlin
val viewModel = viewModel<MyViewModel> {
    // Ask your framework for the instance here.
    createEnroViewModel { myFramework.myViewModel() }
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
- [Koin Modular recipe][koin-modular-recipe].
- [Metro ViewModel recipe][metro-recipe].
- [Metro Modular recipe][metro-modular-recipe].

[koin-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/koin/KoinViewModel.kt
[koin-modular-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/koin/modular/KoinModularApp.kt
[metro-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/metro/src/commonMain/kotlin/dev/enro/recipes/metro/MetroViewModel.kt
[metro-modular-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/metro/src/commonMain/kotlin/dev/enro/recipes/metro/modular/MetroModularApp.kt
