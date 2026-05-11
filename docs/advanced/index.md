---
title: Advanced Topics
nav_order: 4
has_children: true
---

# Advanced Topics

Patterns that build on the core concepts.

- [Results](results.md) — `NavigationKey.WithResult<R>`,
  `complete(value)`, and `registerForNavigationResult`. With two
  sub-pages on chaining multi-step flows: [embedded
  flows](results/embedded-result-flows.md) for short callback chains,
  [managed flows](results/managed-result-flows.md) for longer or
  branching sequences.
- [View Models](view-models.md) — `by navigationHandle<MyKey>()` inside
  a ViewModel, `createEnroViewModel { }` from a Composable, and shared
  state across destinations.
- [Animations](animations.md) — `NavigationAnimations`,
  per-element animation primitives, predictive back, and shared
  elements.
- [Testing](testing.md) — `runEnroTest`, `EnroTestRule`,
  `TestNavigationHandle`, and the assertion surface.
- [Plugins](plugins.md) — observe every destination's lifecycle (open,
  active, close) for cross-cutting concerns like analytics, telemetry,
  and instance-metadata tagging.
