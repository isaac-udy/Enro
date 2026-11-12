---
title: Dependency Injection
parent: Advanced Topics
nav_order: 11
---

# Dependency injection

Enro does not require a dependency injection framework. Any DI framework
can construct a ViewModel for an Enro destination, under one condition.

## Constructing the ViewModel inside `createEnroViewModel`

A ViewModel that uses `by navigationHandle<Key>()` must be constructed inside
`createEnroViewModel { }`. `createEnroViewModel` provides the destination's
`NavigationHandle` to the ViewModel while the ViewModel's constructor runs.

A DI framework's own ViewModel helper constructs the ViewModel outside
`createEnroViewModel { }`. `by navigationHandle<Key>()` then throws "Could
not get a NavigationHandle for ViewModel". Request the instance from the
framework inside `createEnroViewModel { }`.

The framework provides the ViewModel's dependencies. The ViewModel reads
its arguments from `navigation.key`, so the ViewModel does not need
assisted injection.

## Koin

Declare the ViewModel in a Koin module with `factory`. The AndroidX
`viewModel()` call stores the ViewModel, so Koin must create a new instance
on each request.

<!-- snippet: di/koin-view-model -->
```kotlin
class KoinUserDetailViewModel(
    repository: UserRepository,
) : ViewModel() {
    // navigation.key is the key that opened the destination. The key
    // contains the arguments of the ViewModel.
    private val navigation by navigationHandle<KoinUserDetail>()

    val title: String = repository.nameFor(navigation.key.userId)

    fun onBackClicked() = navigation.close()
}

val koinRecipeModule = module {
    single { UserRepository() }
    // `factory`, not Koin's `viewModel`: the AndroidX viewModel() call stores the ViewModel.
    factory { KoinUserDetailViewModel(repository = get()) }
}
```

`koinEnroViewModel` requests the ViewModel from Koin inside
`createEnroViewModel { }`:

<!-- snippet: di/koin-helper -->
```kotlin
@Composable
inline fun <reified T : ViewModel> koinEnroViewModel(): T {
    val koin = getKoin()
    return viewModel<T> { createEnroViewModel { koin.get<T>() } }
}
```

Call `koinEnroViewModel()` in a destination in place of `koinViewModel()`:

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
    // `single`: every feature receives the same KoinSession instance.
    single { KoinSession() }
}

// The Koin application loads the app module and the module of each feature.
private val recipeKoin = koinApplication {
    modules(appModule, ordersModule, accountModule)
}
```

A feature's Koin module provides the feature's ViewModels. The
`enro-processor` registers the feature's destinations; Koin does not.

<!-- snippet: di/koin-modular-feature -->
```kotlin
class OrdersViewModel(
    private val session: KoinSession,
    repository: OrderRepository,
) : ViewModel() {
    private val navigation by navigationHandle<KoinOrders>()

    val orders: List<String> = repository.orders()
    val userName: String get() = session.userName

    // The account feature declares the destination for KoinAccount. The
    // orders feature imports the key only.
    fun onAccountClicked() = navigation.open(KoinAccount)
}

// The Koin module of the orders feature. The app's module provides KoinSession.
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

Declare the ViewModel on the graph as an accessor with no scope. Each read
of an unscoped accessor constructs a new ViewModel.

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

Declare the app's graph with a scope. Each feature reads the graph from a
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

Each feature contributes an interface that declares the feature's
accessors, and casts the graph to that interface:

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

    // The account screen is in another feature and is opened by key.
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

For any other framework, request the ViewModel instance from the framework
inside `createEnroViewModel { }`:

<!-- snippet: di/other-frameworks -->
```kotlin
val viewModel = viewModel<MyViewModel> {
    // Request the instance from the framework here.
    createEnroViewModel { myFramework.myViewModel() }
}
```

## Destinations across modules

The `enro-processor` registers the destinations of every module. A feature
does not register destinations through DI. See
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
