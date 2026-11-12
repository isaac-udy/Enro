---
title: Navigation Containers
parent: Core Concepts
nav_order: 3
---

# Navigation Containers

A `NavigationContainer` stores a backstack of destinations and displays
the backstack in one region of the UI. Most apps have one root container.
Layouts such as tabs, list-detail and multiple back stacks use more than
one container, and the containers can be nested.

`rememberNavigationContainer` creates a container. `NavigationDisplay`
renders the container.

<!-- snippet: navigation-containers/container -->
```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
)
NavigationDisplay(state = container)
```

## rememberNavigationContainer

<!-- snippet: skip (API outline) -->
```kotlin
@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = /* auto-generated, saveable */,
    backstack: NavigationBackstack,
    emptyBehavior: EmptyBehavior = EmptyBehavior.preventEmpty(),
    interceptor: NavigationInterceptor = NoOpNavigationInterceptor,
    filter: NavigationContainerFilter = acceptAll(),
): NavigationContainerState
```

- **`key`**: identifies the container. The default `key` contains a random
  UUID that `rememberSaveable` saves. Pass an explicit `key` to find the
  container from other code, for example with `findContainerContext(key)`.
- **`backstack`**: the initial backstack, for example
  `backstackOf(MyRootKey.asInstance())`.
- **`emptyBehavior`**: the action taken when the last destination closes.
  See [Empty behaviour](#empty-behaviour).
- **`interceptor`**: runs before each operation on the container. See
  [Interceptors](#interceptors).
- **`filter`**: decides which keys the container accepts. See
  [Filters](#filters).

`rememberNavigationContainer` returns a `NavigationContainerState`. Pass
the `NavigationContainerState` to `NavigationDisplay`.

## Building a backstack

`backstackOf` creates a `NavigationBackstack` from instances:

<!-- snippet: navigation-containers/backstack -->
```kotlin
val backstack = backstackOf(
    Home.asInstance(),
    ShowProfile("user-123").asInstance(),
)
```

`asBackstack()` converts a `List<NavigationKey.Instance<*>>` to a
`NavigationBackstack`. `emptyBackstack()` creates a backstack with no
instances. `asInstance()` is described in
[Navigation Keys](navigation-keys.md#navigationkeyinstance).

## NavigationDisplay

`NavigationDisplay` is the Composable that renders a container.

<!-- snippet: skip (API outline) -->
```kotlin
NavigationDisplay(
    state = container,
    modifier = Modifier.fillMaxSize(),
    sceneStrategy = /* defaults to dialog + directOverlay + singlePane */,
    contentAlignment = Alignment.TopStart,
    sizeTransform = null,
    animations = NavigationAnimations.Default,
)
```

`NavigationDisplay` reads the container's backstack and animates between
destinations when the backstack changes. `sceneStrategy` sets the layout
of the backstack; see
[Navigation Destinations](navigation-destinations.md#scene-strategies).
`animations` sets the enter and exit animations of destinations; see
[Animations](../advanced/animations.md).

## Empty behaviour

`EmptyBehavior` sets the action taken when the last destination in the
container closes.

| Function | Effect |
|---|---|
| `EmptyBehavior.preventEmpty()` | The container keeps the last destination. The container does not handle a back press on the last destination. |
| `EmptyBehavior.allowEmpty(onEmpty = { })` | The container becomes empty and renders no destination. Enro calls the optional `onEmpty` lambda when the container becomes empty. |
| `EmptyBehavior.closeParent()` | The container keeps the last destination, and Enro closes the destination that hosts the container. Use `closeParent()` for the detail container of a list-detail layout. |
| `EmptyBehavior.default()` | Returns `preventEmpty()`. |

## Reading and changing the backstack

`NavigationContainerState` has these members:

<!-- snippet: skip (API outline) -->
```kotlin
val backstack: NavigationBackstack
fun updateBackstack(block: (NavigationBackstack) -> NavigationBackstack)
fun execute(operation: NavigationOperation)
```

`backstack` is Compose state. A Composable that reads `state.backstack`
recomposes when the backstack changes, for example to render a bottom bar
or breadcrumbs.

`updateBackstack` passes the current backstack to the block and sets the
backstack that the block returns. A `NavigationBackstack` is a list. Call
`asBackstack()` on the result of a list operation:

- `updateBackstack { it.dropLast(1).asBackstack() }` removes the top
  instance.
- `updateBackstack { it.take(1).asBackstack() }` keeps only the root
  instance.
- `updateBackstack { backstackOf(NewRoot.asInstance()) }` replaces the
  backstack.

Inside a destination, `LocalNavigationContainer.current` returns the
container that displays the destination. The [back stack operations
recipe][backstack-recipe] closes to the root, closes to a chosen screen and
replaces the backstack.

For navigation from inside a screen, use the `NavigationHandle` operations
instead of `updateBackstack`. See [Navigation Handles](navigation-handles.md).

## Filters

A `NavigationContainerFilter` decides which keys a container accepts from
`navigation.open(key)`. The default filter is `acceptAll()`.

| Filter | Accepts |
|---|---|
| `acceptAll()` | every key |
| `acceptNone()` | no key; add instances to the container with `updateBackstack` |
| `accept { key<T>() }` | keys of type `T` |
| `accept { key { it is T && ... } }` | keys that match the predicate |
| `doNotAccept { key<T>() }` | every key except those of type `T` |
| `accept { ... }.fromChildrenOnly()` | matching keys, and only when opened from a destination inside the container |

Two containers hosted by the same destination, each accepting one type of
key:

<!-- snippet: containers/filters -->
```kotlin
val notes = rememberNavigationContainer(
    key = NavigationContainer.Key("notes"),
    backstack = backstackOf(Note(1).asInstance()),
    filter = accept { key<Note>() },
    // preventEmpty keeps the first note: this container does not handle Back on the first note.
    emptyBehavior = EmptyBehavior.preventEmpty(),
)
val tasks = rememberNavigationContainer(
    key = NavigationContainer.Key("tasks"),
    backstack = backstackOf(Task(1).asInstance()),
    filter = accept { key<Task>() },
    emptyBehavior = EmptyBehavior.preventEmpty(),
)
```

## Which container receives a key

When a destination calls `navigation.open(key)`, Enro checks containers in
this order and opens the key in the first container whose filter accepts
the key:

1. Containers hosted by the calling destination: the active container
   first, then the other containers.
2. Containers hosted by the other destinations on the caller's backstack.
3. Containers hosted by the destination that hosts the caller's container:
   the active container first, then the other containers. Step 3 includes
   the caller's container and the sibling containers.
4. Steps 2 and 3 again, one level further up each time, until the root.

In the filters example, `open(Task(2))` from a note opens the `Task` in the
`tasks` container. A key that neither the `notes` filter nor the `tasks`
filter accepts opens in the container that displays the host destination.

The [nested containers recipe][nested-recipe] shows each of these cases.

## Interceptors

A `NavigationInterceptor` runs before Enro applies an operation to the
container's backstack. The interceptor can continue the operation, cancel
the operation or run a different operation. Pass an interceptor to
`rememberNavigationContainer` to intercept one container, or register an
interceptor on the `NavigationComponent` to intercept every container.

See [Interceptors](../advanced/interceptors.md).

## Nested containers

A destination can call `rememberNavigationContainer`. The new container is
a child of the destination's context. Tabs, list-detail layouts and
separate back stacks for each flow use nested containers.

<!-- snippet: navigation-containers/nested -->
```kotlin
@Composable
@NavigationDestination(MainTabs::class)
fun MainTabsDestination() {
    var tab by remember { mutableStateOf(Tab.Home) }

    val homeContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("home"),
        backstack = backstackOf(Home.asInstance()),
    )
    val profileContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("profile"),
        backstack = backstackOf(Profile.asInstance()),
    )

    Column {
        when (tab) {
            Tab.Home -> NavigationDisplay(state = homeContainer)
            Tab.Profile -> NavigationDisplay(state = profileContainer)
        }
        TabBar(current = tab, onSelect = { tab = it })
    }
}
```

Recipes that use nested containers:

- [Tab navigation][tabs-recipe]: a tabbed layout.
- [Multiple back stacks][multistack-recipe]: a separate saved backstack for each tab.

## See also

- [Navigation Destinations](navigation-destinations.md): the destinations that a container displays.
- [Navigation Handles](navigation-handles.md): the operations that change a container's backstack.
- [Animations](../advanced/animations.md): `NavigationAnimations` and animating parts of a destination.
- Recipes: [list-detail][listdetail-recipe], [tabs][tabs-recipe], [multiple back stacks][multistack-recipe].

[backstack-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/backstack/BackStackOperations.kt
[nested-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/containers/NestedContainers.kt
[listdetail-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailNavigation.kt
[tabs-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/tabs/TabNavigation.kt
[multistack-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/multiplestacks/MultipleBackStacks.kt
