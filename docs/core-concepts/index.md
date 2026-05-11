---
title: Core Concepts
nav_order: 3
has_children: true
---

# Core Concepts

The four building blocks of Enro. Read the [basic concepts
tour](../getting-started/basic-concepts.md) first if you haven't — these
pages assume the vocabulary.

- [Navigation Keys](navigation-keys.md) — the contract for a screen: its
  inputs and (optionally) its typed result.
- [Navigation Destinations](navigation-destinations.md) — the
  implementation: a Composable function or provider, bound to a key, with
  optional metadata (dialog, overlay, scene strategy).
- [Navigation Containers](navigation-containers.md) — where a backstack
  lives, including nesting, empty behaviour, filters, and interceptors.
- [Navigation Handles](navigation-handles.md) — the control surface
  inside a screen for `open`, `close`, `complete`, `requestClose`, and
  the result API.
