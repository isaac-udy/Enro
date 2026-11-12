---
title: Sample Apps
nav_order: 65
---

# Sample apps

Three larger samples show several Enro features working together. They are
part of the test application in the repository, under
[`tests/application/.../samples`][samples-root]. For single-concept examples,
see [Recipes](recipes.md).

## Run the samples

Clone the repository, then run the test application from its root:

| Platform | Command |
|---|---|
| Desktop | `./gradlew :tests:application:app:desktop:run` |
| Android | `./gradlew :tests:application:app:android:installDebug`, then open the app on the device |
| Web | `./gradlew :tests:application:app:web:wasmJsBrowserDevelopmentRun` |
| iOS | Open [`EnroTestApplication.xcodeproj`][ios-project] in Xcode and run it |

In the app, choose **Samples**, then a sample.

## Loan Application

A long, branching form: [`samples/loan`][loan].

- The flow is owned by a ViewModel with `registerForFlowResult`. Later steps
  depend on earlier answers.
- The host screen shows the steps in a container made with
  `rememberNavigationContainerForFlow`, under its own progress bar.
- The summary step lets the user go back and change an earlier answer.
- On desktop, a `@NavigationDestination.PlatformOverride` opens the flow in
  its own window with `rootWindowDestination`.

See also the [Flow in a ViewModel recipe](recipes.md#state-and-results).

## Recipe Browser

A list, a detail screen, and a create flow: [`samples/recipes`][recipe-browser].

- The list and detail screens share an element with `sharedElement`.
- Creating a recipe is a `managedFlowDestination`. One step is a dialog that
  returns a result, and the flow branches on it.
- On desktop, the create flow opens in a separate window and the app has a
  menu bar.

## Travel Planner

Login, registration and a home screen: [`samples/travel`][travel].

- Login and logout replace the current screen with `closeAndReplaceWith`.
- After registration, the registration screens are removed from the
  backstack with `updateBackstack`, so Back does not return to them.
- Some screens set their own transitions.

[samples-root]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples
[ios-project]: https://github.com/isaac-udy/Enro/tree/main/tests/application/app/ios/xcode/EnroTestApplication
[loan]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples/loan
[recipe-browser]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples/recipes
[travel]: https://github.com/isaac-udy/Enro/tree/main/tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples/travel
