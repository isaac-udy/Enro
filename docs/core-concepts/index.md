---
title: Core Concepts
nav_order: 3
has_children: true
---

# Core Concepts

These pages describe the five core types of Enro. The pages use the terms
defined in [Basic concepts](../getting-started/basic-concepts.md).

- [Navigation Keys](navigation-keys.md): a `NavigationKey` declares the
  inputs of a screen and, optionally, the type of the screen's result.
- [Navigation Destinations](navigation-destinations.md): a destination is
  a Composable function or a provider that is bound to a key. A provider
  can declare metadata, such as `dialog()` or `directOverlay()`.
- [Navigation Containers](navigation-containers.md): a container stores a
  backstack. The page covers nested containers, empty behaviour, filters
  and interceptors.
- [Navigation Handles](navigation-handles.md): a screen calls `open`,
  `close`, `complete` and `requestClose` on the screen's
  `NavigationHandle`.
- [Navigation Contexts](navigation-contexts.md): the tree of containers
  and destinations on screen, and the functions that search the tree.
