# `enro-annotations`

`enro-annotations` contains the annotations that
[`enro-processor`](../enro-processor/) reads to generate destination
bindings and path bindings, and the opt-in marker annotations
(`@AdvancedEnroApi`, `@ExperimentalEnroApi`) used in the Enro public API.

`enro-annotations` does not depend on the Enro runtime. A module can
depend on `enro-annotations` alone in these cases:

- A KMP module with non-UI targets that declares `NavigationKey` types
  and renders no UI. Such a module also depends on
  [`enro-common`](../enro-common/).
- Build tooling that reads Enro annotations and must not depend on
  Compose or the Android runtime.

## Contents

- `@NavigationDestination(keyType)` — binds a Composable, Fragment or
  Activity to a `NavigationKey` type as the destination for that type.
- `@NavigationComponent` — marks the object for which the processor
  generates `installNavigationController(…)`.
- `@NavigationPath(pattern)` — declares a URL pattern for a
  `NavigationKey`. Deep links and web routing resolve URLs with the
  pattern.
- `@AdvancedEnroApi`, `@ExperimentalEnroApi` — opt-in markers for APIs
  outside the stable contract. An API with either marker can change or
  be removed in any 3.x release.
- `@GeneratedNavigationBinding`, `@GeneratedNavigationComponent` —
  written by the processor on generated classes. Do not use these
  annotations in app code.

## Usage

`dev.enro:enro` and `enro-runtime` include `enro-annotations` as a
transitive dependency. Depend on `enro-annotations` directly only in a
module that requires the annotation types without the runtime, such as a
`:common` KMP module shared with non-Compose targets.

```kotlin
dependencies {
    implementation("dev.enro:enro-annotations:3.0.0")
}
```
