---
title: Navigation Containers
parent: Core Concepts
nav_order: 3
---

# Navigation Containers

A `NavigationContainer` is a region of your UI that hosts a backstack of
destinations. Most apps have one root container; advanced layouts (tabs,
list-detail, multiple back stacks) use several, often nested.

The container is created with `rememberNavigationContainer` and rendered
with `NavigationDisplay`. Together they're the smallest amount of navigation
infrastructure your app needs.

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

- **`key`** — identifies this container within its parent. The default is a
  stable saveable UUID and is fine for most cases. Provide an explicit key
  when you need to address the container from elsewhere (for example, when
  programmatically pushing a destination into a specific container).
- **`backstack`** — the initial contents. Most apps pass
  `backstackOf(MyRootKey.asInstance())`.
- **`emptyBehavior`** — what happens when the backstack becomes empty.
- **`interceptor`** — observe or veto operations before they apply.
- **`filter`** — restrict which keys this container will accept.

The function returns a `NavigationContainerState` you pass to
`NavigationDisplay`.

## Building a backstack

`backstackOf` is the canonical way to build one:

<!-- snippet: navigation-containers/backstack -->
```kotlin
val backstack = backstackOf(
    Home.asInstance(),
    ShowProfile("user-123").asInstance(),
)
```

If you already have a `List<NavigationKey.Instance<*>>`, call `.asBackstack()`.
There's also `emptyBackstack()` if you want to start empty (and combine that
with an `emptyBehavior` other than the default — see below).

`MyKey.asInstance()` wraps a key into an `Instance` with a fresh id; see
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

It watches the container's backstack and animates between destinations as it
changes. `sceneStrategy` controls how the backstack is laid out
(see [Navigation Destinations](navigation-destinations.md#scene-strategies));
`animations` controls how destinations animate in and out
(see [Animations](../advanced/animations.md)).

## Empty behaviour

`EmptyBehavior` decides what happens when the user closes the last
destination in the container.

| Helper | Effect |
|---|---|
| `EmptyBehavior.preventEmpty()` | Refuses the close — the bottom-most destination stays put. |
| `EmptyBehavior.allowEmpty(onEmpty = { })` | Allows the container to become empty when its stack drains. The container stays mounted but renders nothing. The optional `onEmpty` lambda fires when this happens. |
| `EmptyBehavior.closeParent()` | When the last destination is closed, closes the *parent* destination too. This is what a typical detail-stack inside a list-detail layout wants. |
| `EmptyBehavior.default()` | An alias for the current default behaviour. Today that's `preventEmpty()`; future versions of Enro may change the default, so use this if you want to track whatever Enro considers most appropriate. |

For custom behaviour, build an `EmptyBehavior` from a lambda (see
`EmptyBehavior.kt` in the source).

## Reading and changing the backstack

`NavigationContainerState` exposes a few useful members:

<!-- snippet: skip (API outline) -->
```kotlin
val backstack: NavigationBackstack          // current contents, observable
fun updateBackstack(block: (NavigationBackstack) -> NavigationBackstack)
fun execute(operation: NavigationOperation) // for advanced cases
```

Reading `state.backstack` from a Composable is a normal Compose state read —
the surrounding scope recomposes when the backstack changes. You can use this
to drive a bottom bar, breadcrumbs, or any UI element that mirrors the stack.

The block receives the current backstack and returns the new one. A
`NavigationBackstack` is a list, so list operations work on it; call
`asBackstack()` on the result:

- `updateBackstack { it.dropLast(1).asBackstack() }` removes the top entry.
- `updateBackstack { it.take(1).asBackstack() }` goes back to the root.
- `updateBackstack { backstackOf(NewRoot.asInstance()) }` replaces the whole
  stack.

Inside a destination, `LocalNavigationContainer.current` is the container
the destination is displayed in. The [back stack operations
recipe][backstack-recipe] shows back to root, back to a specific screen, and
clearing the stack.

Most navigation goes through `NavigationHandle` operations, not direct
backstack edits — see [Navigation Handles](navigation-handles.md).

## Filters

A `NavigationContainerFilter` decides which keys a container accepts from
`navigation.open(key)`. By default a container accepts every key
(`acceptAll()`).

| Filter | Accepts |
|---|---|
| `acceptAll()` | every key |
| `acceptNone()` | no key; the backstack only changes when you update it directly |
| `accept { key<T>() }` | keys of type `T` |
| `accept { key { it is T && ... } }` | keys that match the predicate |
| `doNotAccept { key<T>() }` | every key except those of type `T` |
| `accept { ... }.fromChildrenOnly()` | matching keys, and only when opened from a destination inside this container |

Two containers hosted by the same destination, each accepting one type of
key:

<!-- snippet: containers/filters -->
```kotlin
val notes = rememberNavigationContainer(
    key = NavigationContainer.Key("notes"),
    backstack = backstackOf(Note(1).asInstance()),
    filter = accept { key<Note>() },
    // Keep the first note: Back on it is not handled by this container.
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

When a destination calls `navigation.open(key)`, Enro looks for a container
that accepts the key. It checks containers in this order and uses the first
one that accepts:

1. Containers hosted by the calling destination: the active one first, then
   the others.
2. Containers hosted by the other destinations on the caller's backstack.
3. Containers hosted by the destination that hosts the caller's container:
   the active one first, then the others. This includes the caller's own
   container and its siblings.
4. Steps 2 and 3 again, one level further up each time, until the root.

So a key is opened as close to the caller as a filter allows. In the example
above, opening a `Task` from a note finds the tasks container, and a key that
neither accepts goes up to the container that hosts both.

The [nested containers recipe][nested-recipe] shows each of these cases.

## Interceptors

A `NavigationInterceptor` runs before an operation is applied to the
container's backstack, and can let it continue, cancel it, or run a
different operation. Pass one to `rememberNavigationContainer` for one
container, or register it on the `NavigationComponent` for all of them.

See [Interceptors](../advanced/interceptors.md).

## Nested containers

A container can be created inside a destination — the resulting container
is a *child* of that destination's context. This is how features like tabs,
list-detail layouts, and per-flow back stacks are built.

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

Two recipes go deeper on this pattern:

- [Tab navigation][tabs-recipe] — a simple tabbed layout.
- [Multiple back stacks][multistack-recipe] — independent saveable backstacks per tab, à la Material's `BottomNavigationView`.

## See also

- [Navigation Destinations](navigation-destinations.md) — what fills a container.
- [Navigation Handles](navigation-handles.md) — how operations on a handle reach the container.
- [Animations](../advanced/animations.md) — `NavigationAnimations` and per-element animation.
- Recipes: [list-detail][listdetail-recipe], [tabs][tabs-recipe], [multiple back stacks][multistack-recipe].

[backstack-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/backstack/BackStackOperations.kt
[nested-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/containers/NestedContainers.kt
[listdetail-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailNavigation.kt
[tabs-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/tabs/TabNavigation.kt
[multistack-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/multiplestacks/MultipleBackStacks.kt
