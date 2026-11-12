---
title: Navigation Contexts
parent: Core Concepts
nav_order: 5
---

# Navigation Contexts

A `NavigationContext` is a node in the tree of what is on screen. Enro
builds the tree as containers and destinations are displayed. Most screens
require only a [Navigation Handle](navigation-handles.md). Use a
`NavigationContext` to read other parts of the tree.

## The tree

The tree has three kinds of context, nested in this order:

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

`LocalNavigationContext.current` returns the nearest context. Inside a
destination, `LocalNavigationContext.current` returns the destination's
`DestinationContext`. Outside every container,
`LocalNavigationContext.current` returns the `RootContext`.

The parent of a `DestinationContext` is the `ContainerContext` of the
container that displays the destination. `hostViewModelStoreOwner` returns
the `ContainerContext` as a `ViewModelStoreOwner`, so the destinations in
one container can share a ViewModel:

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

A context with more than one child has at most one active child. The
active path is the chain of active children from the root. A system back
press is handled on the active path, and `open` checks the active
container first. See
[Which container receives a key](navigation-containers.md#which-container-receives-a-key).

| Member | Meaning |
|---|---|
| `isActive` | The context is the active child of the context's parent. |
| `isActiveInRoot` | The context and every ancestor of the context are active. |
| `activeChild` | The active child of the context, or `null`. |
| `requestActive()` | Makes the context's container the active child of the container's parent. |
| `requestActiveInRoot()` | Calls `requestActive()` on the context and on every ancestor of the context. |
| `activeLeaf()` | Follows `activeChild` down the tree and returns the last context. |
| `activeLeafDestination()` | Follows `activeChild` down the tree and returns the last `DestinationContext`, or `null`. |

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

Each search function searches the context the function is called on and
every context below. Call `root()` first to search the whole tree.

| Function | Returns |
|---|---|
| `findContext { predicate }` | The first context that matches the predicate. |
| `findDestinationContext<T>()` | The first `DestinationContext` with a key of type `T`. |
| `findDestinationContext(key)` | The first `DestinationContext` with a key equal to `key`. |
| `findContainerContext(containerKey)` | The `ContainerContext` of the container with `containerKey`. |
| `findActiveContext { }`, `findActiveDestinationContext<T>()`, `findActiveContainerContext(key)` | The same result as the matching `find` function, searching the active path only. |
| `requireContext { }`, `requireActiveContext { }` | The same result as `findContext` and `findActiveContext`. Each `require` function throws when no context matches. |
| `findAllContainers()` | Every container below the context. |

A context also has these functions:

| Function | Returns |
|---|---|
| `getNavigationHandle<T>()` | The `NavigationHandle` of a `DestinationContext`. |
| `getViewModel<T>()` | A ViewModel that already exists in the context, or `null`. |
| `requireViewModel<T>()` | A ViewModel that already exists in the context. `requireViewModel<T>()` throws when the context has no ViewModel of type `T`. |
| `getDebugString()` | The tree below the context, as text. |

## See also

- [Navigation Containers](navigation-containers.md): the order in which `open` checks containers.
- [Navigation Handles](navigation-handles.md): the operations a screen calls to navigate.
- [View Models](../advanced/view-models.md)

[shared-vm-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/viewmodel/SharedViewModel.kt
