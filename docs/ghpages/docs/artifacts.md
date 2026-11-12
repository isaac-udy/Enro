---
title: Artifacts
nav_order: 70
---

# Artifacts

Enro is published to Maven Central under the group `dev.enro`. Every
artifact has the same version.

Most apps require three artifacts: `enro`, `enro-processor` and `enro-test`.
See [Installation](getting-started/installation.md).

## When to add each artifact

| Artifact | Add the artifact when | Configuration |
|---|---|---|
| `enro` | The module has destinations or containers. | `implementation` |
| `enro-processor` | The module uses `@NavigationDestination`, `@NavigationComponent` or `@NavigationPath`. | `ksp`, once per target |
| `enro-test` | The module has tests that navigate. | `testImplementation` |
| `enro-common` | The module only declares `NavigationKey`s, or has no UI. | `implementation` |
| `enro-compat` | The app is moving from Enro 2 and still uses the 2.x API. | `implementation` |
| `enro-html` | A Kotlin/JS app renders destinations as HTML, without Compose. | `implementation` |

## Contents of each artifact

| Artifact | Contents | Targets |
|---|---|---|
| `enro` | No code. `enro` depends on `enro-runtime`, `enro-ui`, `enro-common` and `enro-annotations`. The Android variant of `enro` also contains the lint checks. | Android, iOS, desktop, wasmJs, JS |
| `enro-common` | `NavigationKey`, `NavigationKey.Instance`, `NavigationBackstack`, metadata, and path parsing. No Compose. | Android, iOS, desktop, wasmJs, JS (browser and Node.js) |
| `enro-annotations` | `@NavigationDestination`, `@NavigationComponent`, `@NavigationPath`, `@AdvancedEnroApi`, `@ExperimentalEnroApi`. No dependencies. | Android, iOS, desktop, wasmJs, JS |
| `enro-runtime` | Handles, containers, contexts, results, flows, interceptors, plugins, synthetic destinations, and Fragment and Activity destinations. Uses the Compose runtime, not Compose UI. | Android, iOS, desktop, wasmJs, JS |
| `enro-ui` | `NavigationDisplay`, scenes, animations and shared elements. Uses Compose UI. | Android, iOS, desktop, wasmJs, JS |
| `enro-processor` | The KSP processor. Generates a binding for each destination and the code that installs a `NavigationComponent`. | Runs in the build |
| `enro-test` | `runEnroTest`, `EnroTestRule`, test navigation handles and assertions. | Android, iOS, desktop, wasmJs, JS |
| `enro-compat` | The Enro 2 API names, implemented with Enro 3. | Android, iOS, desktop, wasmJs |
| `enro-html` | Destinations rendered to the DOM, for Kotlin/JS. | JS (browser) |

## Lint checks

The Android variant of `enro` contains two lint checks. Both lint checks run
in the Android lint task and in Android Studio.

| Check | Reports |
|---|---|
| `IncorrectlyTypedNavigationHandle` | A `navigationHandle<T>()` call with a key type the destination is not bound to. |
| `MissingNavigationDestinationAnnotation` | A `navigationHandle<T>()` call in a class or function that has no `@NavigationDestination`. |

## Opt-in APIs

Two annotations mark the parts of the API that require an opt-in.

| Annotation | Meaning |
|---|---|
| `@AdvancedEnroApi` | The API is for code that extends Enro. The API may change without warning. |
| `@ExperimentalEnroApi` | The API is experimental. The API may change or be removed without warning. |

Opt in at the call site with `@OptIn(AdvancedEnroApi::class)`. Opt in for a
whole module with the `-opt-in=dev.enro.annotations.AdvancedEnroApi` compiler
argument.

## See also

- [Installation](getting-started/installation.md)
- [Migrating from Enro 2](migrating-from-v2.md) — the Enro 2 API that `enro-compat` provides.
