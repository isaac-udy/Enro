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

```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
)
NavigationDisplay(state = container)
```

## rememberNavigationContainer

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

```kotlin
val backstack: NavigationBackstack          // current contents, observable
fun updateBackstack(block: (NavigationBackstack) -> NavigationBackstack)
fun execute(operation: NavigationOperation) // for advanced cases
```

Reading `state.backstack` from a Composable is a normal Compose state read —
the surrounding scope recomposes when the backstack changes. You can use this
to drive a bottom bar, breadcrumbs, or any UI element that mirrors the stack.

`updateBackstack { it.drop(1) }` programmatically reduces the stack;
`updateBackstack { backstackOf(NewRoot.asInstance()) }` resets it entirely.
Most navigation goes through `NavigationHandle` operations, not direct
backstack edits — see [Navigation Handles](navigation-handles.md).

## Filters

A `NavigationContainerFilter` decides whether a given key is accepted by this
container. By default a container accepts everything (`acceptAll()`).

Filters are how you steer navigation to the *right* container in an app with
several of them. A common pattern: a "details" container in a list-detail
layout accepts only detail-type keys; navigation to detail keys gets routed
there automatically.

See the [list-detail recipe][listdetail-recipe] for a working example.

## Interceptors

A `NavigationInterceptor` observes (and optionally vetos or rewrites) every
operation before it applies to the container's backstack. Use cases:

- Confirming "unsaved changes will be lost" before allowing a close.
- Logging or analytics on navigation events.
- Redirecting one key to another based on app state.

Interceptors are built either from a `NavigationInterceptor` implementation
or from the `navigationInterceptor { }` builder. They can also be registered
globally on the `NavigationComponent` (see
[Installation](../getting-started/installation.md)).

## Nested containers

A container can be created inside a destination — the resulting container
is a *child* of that destination's context. This is how features like tabs,
list-detail layouts, and per-flow back stacks are built.

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

[listdetail-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailNavigation.kt
[tabs-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/tabs/TabNavigation.kt
[multistack-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/multiplestacks/MultipleBackStacks.kt
