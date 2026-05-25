# `enro-runtime`

The runtime engine of Enro. Defines the public API every Enro app touches
at runtime: [`NavigationKey`](src/commonMain/kotlin/dev/enro/NavigationKey.kt),
[`NavigationHandle`](src/commonMain/kotlin/dev/enro/NavigationHandle.kt),
[`NavigationContainer`](src/commonMain/kotlin/dev/enro/NavigationContainer.kt),
[`NavigationDisplay`](src/commonMain/kotlin/dev/enro/ui/NavigationDisplay.kt),
plus the operation / scene / result-channel machinery that makes them
work. If you're declaring a destination or driving navigation from
inside one, you're using this module.

Targets **Android, iOS, JVM Desktop, and WASM JS** through Compose
Multiplatform. Non-UI definitions that need to be shared with non-UI
targets (e.g. a NodeJS backend) live in [`enro-common`](../enro-common/)
instead — `enro-runtime` re-exports them.

## What's in here

- **Keys & handles** — `NavigationKey` (the screen contract),
  `NavigationKey.WithResult` (typed completion), `NavigationHandle` (the
  destination's view of the runtime).
- **Containers & operations** — `NavigationContainer` (a hosted
  backstack), `NavigationOperation` (Open / Close / Complete /
  CompleteFrom / SetBackstack and their aggregates).
- **Scenes & display** — `NavigationDisplay` (Compose renderer),
  `NavigationSceneStrategy` / `SceneDecoratorStrategy` (how a backstack
  becomes a scene tree), built-in `SinglePane` / `Dialog` / `DirectOverlay`
  scene strategies.
- **Results** — `registerForNavigationResult` (the caller's side),
  `NavigationKey.WithResult.complete(…)` (the callee's side),
  `NavigationFlow` (multi-step flows).
- **Path bindings** — `@NavigationPath` and the path-resolution APIs that
  turn URLs into keys and back, used for deep-linking and web routing.
- **Synthetic destinations** — `syntheticDestination<K> { … }` for keys
  whose "destination" is a synchronous decision, not UI (auth gates,
  external URL launchers, redirects).

## Typical usage

Apps usually depend on the meta-module `dev.enro:enro`, which pulls in
`enro-runtime` along with the KSP processor and a sensible default
configuration. Depending on `enro-runtime` directly is for advanced
setups (custom processors, slimmer artefacts).

```kotlin
dependencies {
    implementation("dev.enro:enro:3.0.0-beta01")
    ksp("dev.enro:enro-processor:3.0.0-beta01")
}
```

See the root [README](../README.md) for a working "define a key, render
a destination, navigate" example, and [enro.dev](https://enro.dev) for
the full guide.
