---
title: Navigation Contexts
parent: Core Concepts
nav_order: 5
---

# Navigation Contexts

A `NavigationContext` is a node in the tree of what is on screen. Enro
builds the tree as containers and destinations are displayed. Most screens
only need a [Navigation Handle](navigation-handles.md). Use a context to
look at other parts of the tree.

## The tree

There are three kinds of context. They nest in this order:

| Context | Represents | Parent | Children |
|---|---|---|---|
| `RootContext` | A window, Activity or view controller | none | `ContainerContext` |
| `ContainerContext` | A navigation container | `RootContext` or `DestinationContext` | `DestinationContext` |
| `DestinationContext<T>` | A destination in a container | `ContainerContext` | `ContainerContext` |

```
RootContext
└── ContainerContext            (the root container)
    ├── DestinationContext      (Home, covered)
    └── DestinationContext      (Tabs, on screen)
        ├── ContainerContext    (first tab's container, active)
        │   └── DestinationContext
        └── ContainerContext    (second tab's container)
            └── DestinationContext
```

A `DestinationContext` has the destination's `key` and `instance`. A
`ContainerContext` has the `container`.

Every context is a `LifecycleOwner` and a `ViewModelStoreOwner`.

## Get the current context

`LocalNavigationContext.current` returns the nearest context. In a
destination that is the destination's `DestinationContext`. Outside any
container it is the `RootContext`.

The parent of a destination's context is the container the destination is
in. This function returns that container's context as a
`ViewModelStoreOwner`, so that the destinations in one container can share a
ViewModel:

<!-- snippet: contexts/parent-owner -->
```kotlin
@Composable
private fun hostViewModelStoreOwner(): ViewModelStoreOwner {
    val context = LocalNavigationContext.current as DestinationContext<*>
    return context.parent
}
```

See the [Shared ViewModel recipe][shared-vm-recipe].

## Active contexts

A context with more than one child has at most one active child. The active
path from the root is where a system back press goes, and where `open` looks
first for a container. See
[Which container receives a key](navigation-containers.md#which-container-receives-a-key).

| Member | Meaning |
|---|---|
| `isActive` | This context is its parent's active child. |
| `isActiveInRoot` | This context and every context above it are active. |
| `activeChild` | The active child, or `null`. |
| `requestActive()` | Makes this context's container the active child of its parent. |
| `requestActiveInRoot()` | Calls `requestActive()` on this context and every context above it. |
| `activeLeaf()` | Follows `activeChild` down the tree and returns the last context. |
| `activeLeafDestination()` | The same, and returns the last destination, or `null`. |

Call `requestActive()` to make a container active without navigating, for
example when the user selects a tab:

<!-- snippet: contexts/request-active -->
```kotlin
fun setActive(container: NavigationContainerState) {
    activeContainerState.value = container
    container.context.requestActive()
}
```

## Find another context

These functions search a context and everything below it. Call `root()`
first to search the whole tree.

| Function | Returns |
|---|---|
| `findContext { predicate }` | The first context that matches. |
| `findDestinationContext<T>()` | The first destination with a key of type `T`. |
| `findDestinationContext(key)` | The first destination with that key. |
| `findContainerContext(containerKey)` | The container with that key. |
| `findActiveContext { }`, `findActiveDestinationContext<T>()`, `findActiveContainerContext(key)` | The same, on the active path only. |
| `requireContext { }`, `requireActiveContext { }` | The same, and throw if nothing matches. |
| `findAllContainers()` | Every container below the context. |

From a context you can get:

| Function | Returns |
|---|---|
| `getNavigationHandle<T>()` | The `NavigationHandle` of a destination context. |
| `getViewModel<T>()` | A ViewModel that already exists in the context, or `null`. |
| `requireViewModel<T>()` | The same, and throws if there is none. |
| `getDebugString()` | The tree below the context, as text. |

## See also

- [Navigation Containers](navigation-containers.md) — how `open` picks a container.
- [Navigation Handles](navigation-handles.md) — what a screen uses to navigate.
- [View Models](../advanced/view-models.md)

[shared-vm-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/viewmodel/SharedViewModel.kt
