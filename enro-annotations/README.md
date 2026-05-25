# `enro-annotations`

The annotations consumed by [`enro-processor`](../enro-processor/) to
generate destination registrations and path bindings, plus the opt-in
marker annotations (`@AdvancedEnroApi`, `@ExperimentalEnroApi`) used
throughout the Enro public API.

This module exists as a thin, dependency-free artefact so consumers can
reference Enro annotations without pulling in the full runtime — useful
for:

- KMP modules with non-UI targets that share `NavigationKey` definitions
  but never render UI (paired with [`enro-common`](../enro-common/)).
- Build-time tooling that needs to read Enro annotations without taking
  a Compose / Android runtime dependency.

## What's in here

- `@NavigationDestination(keyType)` — marks a Composable / Fragment /
  Activity as the destination for a given `NavigationKey` type.
- `@NavigationComponent` — marks the class whose generated companion
  becomes your component's `installNavigationController(…)` entry point.
- `@NavigationPath(pattern)` — declares a URL pattern for a
  `NavigationKey`, enabling deep-link / web-routing resolution.
- `@AdvancedEnroApi`, `@ExperimentalEnroApi` — opt-in markers for
  surfaces that aren't part of the stable API contract.
- `@GeneratedNavigationBinding`, `@GeneratedNavigationComponent` —
  emitted by the processor; not for hand-use.

## Typical usage

Pulled in transitively when you depend on `dev.enro:enro` or
`enro-runtime`. Depend on `enro-annotations` directly only when you
want the annotation types without the rest of the runtime (e.g. in a
`:common` KMP module shared with non-Compose targets).

```kotlin
dependencies {
    implementation("dev.enro:enro-annotations:3.0.0-alpha10")
}
```
