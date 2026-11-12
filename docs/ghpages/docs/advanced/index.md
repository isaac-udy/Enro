---
title: Advanced Topics
nav_order: 4
has_children: true
---

# Advanced Topics

The Advanced Topics pages document APIs that build on the core concepts.

- [Results](results.md) — `NavigationKey.WithResult<R>`,
  `complete(value)` and `registerForNavigationResult`. The Results page
  has three sub-pages for multi-step flows: [embedded
  flows](results/embedded-result-flows.md) for short callback chains,
  [managed flows](results/managed-result-flows.md) for longer or
  branching sequences, and [Flows in a
  ViewModel](results/navigation-flows.md) for a flow that draws its own
  UI around the steps.
- [View Models](view-models.md) — `by navigationHandle<MyKey>()` inside
  a ViewModel, `createEnroViewModel { }` in a Composable, and state
  shared between destinations.
- [Animations](animations.md) — `NavigationAnimations`, per-element
  animations, predictive back and shared elements.
- [Testing](testing.md) — `runEnroTest`, `EnroTestRule`,
  `TestNavigationHandle` and the assertion functions.
- [Plugins](plugins.md) — receive a callback when any destination is
  opened, becomes active or is closed. Used for analytics, telemetry and
  instance metadata.
- [Synthetic Destinations](synthetic-destinations.md) — a
  `NavigationKey` bound to a block of code instead of a screen. Used to
  call non-Enro APIs, redirect on a runtime condition, and choose
  between several destinations that return the same result type.
- [Path Bindings](path-bindings.md) — `@NavigationPath` and
  `NavigationPathBinding` map URL paths to `NavigationKey`s for deep
  links and web URLs.
- [Interceptors](interceptors.md) — run code before an open, close or
  result, and continue, cancel or replace the operation.
- [Deep Links](deep-links.md) — open a destination from a URL, on Android
  and the web, with a backstack below the destination.
- [Saved State](saved-state.md) — how long state in `remember`,
  `rememberSaveable`, a ViewModel and `SavedStateHandle` is kept.
- [Dependency Injection](dependency-injection.md) — ViewModels created by a
  DI framework, with Koin and Metro as the examples.
- [Multi-Module Projects](multi-module.md) — feature modules that open
  screens from other feature modules without a module dependency.
- [Scenes](scenes.md) — dialogs, overlays, custom scene strategies and
  scene decorators.
