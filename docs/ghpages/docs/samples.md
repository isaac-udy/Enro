---
title: Sample Apps
nav_order: 65
---

# Sample apps

Three larger samples each combine several Enro features. The three samples
are part of the test application in the repository, under
[`tests/application/.../samples`][samples-root]. For examples that each show
one concept, see [Recipes](recipes.md).

## Run the samples

Clone the repository, then run the test application from the repository root:

| Platform | Command |
|---|---|
| Desktop | `./gradlew :tests:application:app:desktop:run` |
| Android | `./gradlew :tests:application:app:android:installDebug`, then open the app on the device |
| Web | `./gradlew :tests:application:app:web:wasmJsBrowserDevelopmentRun` |
| iOS | Open [`EnroTestApplication.xcodeproj`][ios-project] in Xcode and run the project |

In the app, choose **Samples**, then a sample.

## Loan Application

A long, branching form: [`samples/loan`][loan].

- A ViewModel declares the flow with `registerForFlowResult`. The answers to
  earlier steps decide which later steps the flow opens.
- The host screen renders the steps in a container created with
  `rememberNavigationContainerForFlow`, below a progress bar.
- From the summary step, the user can return to an earlier step and change
  the answer.
- On desktop, a `@NavigationDestination.PlatformOverride` opens the flow in
  a separate window with `rootWindowDestination`.

See also the [Flow in a ViewModel recipe](recipes.md#state-and-results).

## Recipe Browser

A list, a detail screen, and a create flow: [`samples/recipes`][recipe-browser].

- The list and detail screens share an element with `sharedElement`.
- The create flow is a `managedFlowDestination`. One step is a dialog that
  returns a result, and the flow branches on the dialog's result.
- On desktop, the create flow opens in a separate window and the app has a
  menu bar.

## Travel Planner

Login, registration and a home screen: [`samples/travel`][travel].

- Login and logout replace the current screen with `closeAndReplaceWith`.
- After registration, `updateBackstack` removes the registration screens from
  the backstack, so Back does not return to the registration screens.
- Some screens declare transitions for that screen only.

[samples-root]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples
[ios-project]: https://github.com/isaac-udy/Enro/tree/main/tests/application/app/ios/xcode/EnroTestApplication
[loan]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples/loan
[recipe-browser]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples/recipes
[travel]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples/travel
