---
title: Recipes
nav_order: 60
---

# Recipes

The recipes app contains runnable examples. Each recipe shows one concept. The
recipe source is in the [`recipes`][recipes-root] directory of the repository.

Where a recipe matches a recipe in Google's
[Navigation 3 recipes][nav3-recipes], the "Navigation 3 recipe" column names
the Navigation 3 recipe.

## Run the recipes app

Clone the repository, then run one of these commands from the repository root:

| Platform | Command |
|---|---|
| Desktop | `./gradlew :recipes:app:desktop:run` |
| Android | `./gradlew :recipes:app:android:installDebug`, then open "Enro Recipes" on the device |
| Web | `./gradlew :recipes:app:web:wasmJsBrowserDevelopmentRun` |
| iOS | Open [`EnroRecipes.xcodeproj`][ios-project] in Xcode and run the project |

## Basic

| Recipe | Shows | Navigation 3 recipe |
|---|---|---|
| [Basic Navigation][basic] | `open`, `close`, and keys with arguments | Basic |
| [Saveable Back Stack][saveable] | A backstack and `rememberSaveable` state that are restored after a configuration change | Saveable back stack |
| [Saved State][saved-state] | How long state is kept by `remember`, `rememberSaveable`, a ViewModel and `SavedStateHandle` | Saveable back stack |
| [Destination Registration][registration] | Function and provider destinations; `dialog()` and `directOverlay()` metadata | Entry provider DSL |
| [Back Stack Operations][backstack] | Back to root, back to a specific screen, replace the current screen, clear the stack | — |
| [Basic Deep Link][basic-deep-link] | `@NavigationPath`, hand-written path bindings, and resolving a URL to a key | Uri with Arguments |
| [Handling a Deep Link][handle-deep-link] | Opening a destination from a URL that the app receives from outside the app | Static Uri |
| [Advanced Deep Link][advanced-deep-link] | A synthetic backstack for a deep link | Synthetic BackStack |

## Scenes and overlays

| Recipe | Shows | Navigation 3 recipe |
|---|---|---|
| [Dialog Navigation][dialog] | Dialog destinations with `dialog()` and `directOverlay()` | Dialog |
| [Navigating From a Dialog][from-dialog] | Opening a screen on top of a dialog, replacing a dialog, stacking dialogs | — |
| [Bottom Sheet Navigation][bottomsheet] | `ModalBottomSheet` as a destination | BottomSheet |
| [List-Detail (Scene)][listdetail] | A hand-written list-detail scene strategy | List-Detail Scene |
| [Two-Pane (Scene)][twopane] | Two adjacent destinations side by side | Two pane Scene |
| [List-Detail (Containers)][listdetail-containers] | List-detail built from two containers | — |
| [Material List-Detail][material] | Material 3 Adaptive `ListDetailPaneScaffold` as a scene strategy | Material List-Detail |
| [Material Supporting Pane][material] | Material 3 Adaptive `SupportingPaneScaffold` as a scene strategy | Material Supporting Pane |
| [Nested Containers][nested] | Which container opens a key passed to `open`; `accept { }` filters | — |
| [Tab Navigation][tabs] | One container per tab | Common navigation UI |
| [Multiple Back Stacks][multiplestacks] | One backstack per tab; each backstack is kept when the user changes tab | Multiple back stacks |
| [Scene Decoration][scene-decoration] | A `SceneDecoratorStrategy` that adds a sidebar or bottom bar | Responsive Navigation Scene Decorator |
| [Shell Scene][shell-scene] | A multi-pane app shell configured by destination metadata | — |

## Animations

| Recipe | Shows | Navigation 3 recipe |
|---|---|---|
| [Animated Navigation][animations] | `NavigationAnimations` for a container | Animations |
| [Per-Destination Transitions][per-destination] | Transitions for one destination; the predictive back gesture | Animations |
| [Staggered Animations][staggered] | Animating parts of a destination on their own timing | — |
| [Shared Element Animations][shared-elements] | `sharedElement` across destinations | — |

## State and results

| Recipe | Shows | Navigation 3 recipe |
|---|---|---|
| [Basic ViewModel][viewmodel] | A NavigationHandle inside a ViewModel | Basic ViewModel |
| [Koin ViewModel][koin] | A ViewModel with dependencies from Koin and arguments from the destination's key | Koin injected ViewModel |
| [Koin Modular][koin-modular] | Features that each provide a Koin module and open each other's screens by key | Koin modular |
| [Metro ViewModel][metro] | A ViewModel with dependencies from a Metro graph and arguments from the destination's key | Metro injected ViewModel |
| [Metro Modular][metro-modular] | Features that contribute to one Metro graph and open each other's screens by key | Metro modular |
| [Shared ViewModel][shared-viewmodel] | One ViewModel used by several destinations | Shared ViewModel |
| [Sharing State with Results][shared-state] | Passing a value to a child screen and receiving the edited value as a result | — |
| [Returning Results][results] | `NavigationKey.WithResult` and `registerForNavigationResult` | Results as Events |
| [Results in a ViewModel][viewmodel-results] | Requesting and receiving a result from a ViewModel | — |
| [Managed Flow][managed-flow] | A multi-step flow as a destination | — |
| [Flow in a ViewModel][viewmodel-flow] | A multi-step flow declared in a ViewModel | — |
| [Request-Close Confirmation][request-close] | Confirming before a screen closes | — |

## Advanced

| Recipe | Shows | Navigation 3 recipe |
|---|---|---|
| [Conditional Navigation][conditional] | An auth gate with an interceptor on a container | Conditional navigation |
| [Module Interceptor][module-interceptor] | An interceptor for every container in the app | — |
| [Modular Navigation][modular] | Feature modules that open each other's screens without depending on each other | Hilt, Koin and Metro modular |
| [Opened Timestamp Plugin][plugin] | A `NavigationPlugin` | — |

## Synthetic destinations

| Recipe | Shows | Navigation 3 recipe |
|---|---|---|
| [External URL Launcher][synthetic-url] | A key that runs a side effect and shows no screen | — |
| [Auth Gate][synthetic-auth] | A key that redirects to a login screen | Conditional navigation |
| [Result Decider][synthetic-decider] | A key that forwards another destination's result | — |

## Interop

| Recipe | Shows | Navigation 3 recipe |
|---|---|---|
| [Native Interop][interop] | A platform view (`AndroidView`, `SwingPanel`) inside a destination | Fragment and View Interop |

## Platform recipes

Each platform recipe is listed only in the recipes app for that platform,
under "This Platform".

| Recipe | Platform | Shows | Navigation 3 recipe |
|---|---|---|---|
| [Fragment and Activity][fragment-activity] | Android | A Fragment and an Activity as destinations; the Activity returns a result | Fragment and View Interop |
| [UIViewController][uiviewcontroller] | iOS | A `UIViewController` as a destination: embedded, presented modally, and pushed | — |
| [Multiple Windows][windows] | Desktop | A destination that opens in a separate window and returns a result | — |

## Tests

[`RecipeSmokeTests`][smoke-tests] runs the recipes on desktop with Compose UI
tests. `RecipeSmokeTests` is also an example of a navigation test that uses
the real UI: each test installs the app's `NavigationComponent`, shows a
container, and clicks through a recipe. Run `RecipeSmokeTests` with
`./gradlew :recipes:common:desktopTest`.

[recipes-root]: https://github.com/isaac-udy/Enro/tree/main/recipes
[nav3-recipes]: https://github.com/android/nav3-recipes
[ios-project]: https://github.com/isaac-udy/Enro/tree/main/recipes/app/ios/xcode/EnroRecipes
[basic]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/basic/BasicNavigation.kt
[saveable]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/saveable/SaveableBackStack.kt
[saved-state]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/saveable/SavedState.kt
[registration]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/entryprovider/DestinationRegistration.kt
[backstack]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/backstack/BackStackOperations.kt
[basic-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/BasicDeepLink.kt
[handle-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/HandleDeepLink.kt
[advanced-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/AdvancedDeepLink.kt
[dialog]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/dialog/DialogNavigation.kt
[from-dialog]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/dialog/NavigatingFromDialog.kt
[bottomsheet]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/bottomsheet/BottomSheetNavigation.kt
[listdetail]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailNavigation.kt
[twopane]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/twopane/TwoPaneNavigation.kt
[listdetail-containers]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailWithContainersNavigation.kt
[material]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/material/MaterialAdaptiveNavigation.kt
[nested]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/containers/NestedContainers.kt
[tabs]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/tabs/TabNavigation.kt
[multiplestacks]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/multiplestacks/MultipleBackStacks.kt
[scene-decoration]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/scenedecoration/simple/SceneDecorationNavigation.kt
[shell-scene]: https://github.com/isaac-udy/Enro/tree/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/scenedecoration/complex
[animations]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/animations/AnimatedNavigation.kt
[per-destination]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/animations/PerDestinationTransitions.kt
[staggered]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/animations/StaggeredAnimations.kt
[shared-elements]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/sharedelements/SharedElementAnimations.kt
[viewmodel]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/viewmodel/BasicViewModel.kt
[koin]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/koin/KoinViewModel.kt
[koin-modular]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/koin/modular/KoinModularApp.kt
[metro]: https://github.com/isaac-udy/Enro/blob/main/recipes/metro/src/commonMain/kotlin/dev/enro/recipes/metro/MetroViewModel.kt
[metro-modular]: https://github.com/isaac-udy/Enro/blob/main/recipes/metro/src/commonMain/kotlin/dev/enro/recipes/metro/modular/MetroModularApp.kt
[shared-viewmodel]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/viewmodel/SharedViewModel.kt
[shared-state]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/viewmodel/SharedStateWithResults.kt
[results]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/results/ReturningResults.kt
[viewmodel-results]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/results/ViewModelResults.kt
[managed-flow]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/managedflow/ManagedFlow.kt
[viewmodel-flow]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/flow/FlowInViewModel.kt
[request-close]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/requestclose/RequestCloseConfirmation.kt
[conditional]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/conditional/ConditionalNavigation.kt
[module-interceptor]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/interceptor/ModuleInterceptor.kt
[modular]: https://github.com/isaac-udy/Enro/tree/main/recipes/modular
[plugin]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/plugins/OpenedTimestampPlugin.kt
[synthetic-url]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/synthetic/ExternalUrlSynthetic.kt
[synthetic-auth]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/synthetic/AuthGateSynthetic.kt
[synthetic-decider]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/synthetic/ProfileDeciderSynthetic.kt
[interop]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/interop/NativeInterop.kt
[fragment-activity]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/androidMain/kotlin/dev/enro/recipes/interop/FragmentAndActivity.kt
[uiviewcontroller]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/iosMain/kotlin/dev/enro/recipes/interop/UIViewControllerInterop.kt
[windows]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/desktopMain/kotlin/dev/enro/recipes/windows/MultipleWindows.kt
[smoke-tests]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/desktopTest/kotlin/dev/enro/recipes/RecipeSmokeTests.kt
