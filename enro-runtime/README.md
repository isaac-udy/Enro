# `enro-runtime`

`enro-runtime` contains the Enro navigation runtime:
[`NavigationHandle`](src/commonMain/kotlin/dev/enro/NavigationHandle.kt),
[`NavigationContainer`](src/commonMain/kotlin/dev/enro/NavigationContainer.kt),
navigation operations, scene strategies, result channels and path
bindings. A module that declares a destination or executes navigation
operations depends on `enro-runtime`.

`enro-runtime` targets **Android, iOS, JVM Desktop, WASM JS and
Kotlin/JS**.
[`NavigationDisplay`](../enro-ui/src/commonMain/kotlin/dev/enro/ui/NavigationDisplay.kt),
the Compose UI renderer, is in `enro-ui`.
[`NavigationKey`](../enro-common/src/commonMain/kotlin/dev/enro/NavigationKey.kt)
and the other definitions shared with non-UI targets, such as a NodeJS
backend, are in [`enro-common`](../enro-common/). `enro-runtime` declares
`enro-common` as an `api` dependency.

## Contents

- **Handles** — `NavigationHandle` executes operations for one
  destination and provides the destination's `NavigationKey`.
- **Containers and operations** — `NavigationContainer` (a backstack)
  and `NavigationOperation` (`Open`, `Close`, `Complete`, `CompleteFrom`,
  `SetBackstack`, `SideEffect`, `AggregateOperation`).
- **Scenes** — `NavigationSceneStrategy` and `SceneDecoratorStrategy`
  convert a backstack into scenes. `SinglePaneSceneStrategy` and
  `DirectOverlaySceneStrategy` are in `enro-runtime`.
  `DialogSceneStrategy` is in `enro-ui`.
- **Results** — `registerForNavigationResult` (the caller),
  `navigation.complete(…)` on a `NavigationKey.WithResult` key (the
  destination), and `NavigationFlow` (multi-step flows).
- **Path bindings** — the APIs that convert a URL to a `NavigationKey`
  and a `NavigationKey` to a URL, used for deep links and web routing.
- **Synthetic destinations** — `syntheticDestination<K> { … }` for a key
  whose destination runs code and renders no UI, such as an
  authentication check, an external URL launcher or a redirect.

## Usage

Most apps depend on `dev.enro:enro`, which includes `enro-runtime`,
`enro-ui`, `enro-common` and `enro-annotations`. The KSP processor is a
separate `ksp(...)` dependency. Depend on `enro-runtime` directly in a
module that does not use Compose UI, such as a module that uses
`enro-html`.

```kotlin
dependencies {
    implementation("dev.enro:enro:3.0.0")
    ksp("dev.enro:enro-processor:3.0.0")
}
```

The root [README](../README.md) has an example that declares a key,
renders a destination and navigates. [enro.dev](https://enro.dev) has
the full documentation.
