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
- [Synthetic Destinations](synthetic-destinations.md) — `NavigationKey`s
  whose "destination" is a block of code instead of a screen. Bridges
  to non-Enro side effects, conditional redirects, and runtime "decider"
  patterns that pick between several implementations of a single
  result-bearing contract.
- [Path Bindings](path-bindings.md) — `@NavigationPath` and
  `NavigationPathBinding`: map URL paths to `NavigationKey`s for deep
  links and web URLs.
- [Interceptors](interceptors.md) — run code before an open, close or
  result, and continue, cancel or replace the operation.
- [Deep Links](deep-links.md) — open a destination from a URL, on Android
  and the web, with a backstack underneath it.
- [Saved State](saved-state.md) — what `remember`, `rememberSaveable`, a
  ViewModel and `SavedStateHandle` each survive.
- [Dependency Injection](dependency-injection.md) — ViewModels created by a
  DI framework, with Koin and Metro as the examples.
- [Multi-Module Projects](multi-module.md) — feature modules that open each
  other's screens without depending on each other.
- [Scenes](scenes.md) — dialogs, overlays, custom scene strategies and
  scene decorators.

The Results page has a third sub-page, [Flows in a
ViewModel](results/navigation-flows.md), for a multi-step flow that needs
its own UI around the steps.
