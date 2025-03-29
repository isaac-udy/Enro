# Changelog

## 2.9.0 (Unreleased)

## 2.8.4
* Resolved a bug with `registerForNavigationResult` in Composables when the parent ComposeView is hosted in a RecyclerView ViewHolder, which was causing results to not be delivered

## 2.8.3
* Resolved a bug with animation changes to `BottomSheetDestination` that caused animation snapping for these destinations

## 2.8.2
* Removed deprecated DialogDestination and BottomSheetDestination interfaces, and associated functions. Please use the Composable `DialogDestination` and `BottomSheetDestination` functions instead. Example usage can be found in the test application.
* Deprecated the `OverrideNavigationAnimations` function that does not take a content lambda, in favour of the version that does take a content lambda.
* `ModalBottomSheetState.bindToNavigationHandle` no longer overrides navigation animations.
* Updated animation internals in preparation for predictive back navigation

## 2.8.1
* Fixed a bug with ComposableDestinationSavedStateOwner that was causing lists of primitives (such as List<Int>) to not get saved/restored correctly

## 2.8.0
* Updated Compose to 1.7.1
* Added support for NavigationKey.WithExtras to `NavigationResultChannel` and `NavigationFlowScope`
* Updated `enro-test` methods to provide more descriptive error messages when assert/expect methods fail, and added kdoc comments to many of the functions
* Updated Composable navigation animations to use SeekableTransitionState, as a step towards supporting predictive back navigation animations
* Fixed a bug where managed flows (`registerForFlowResult`) that launch embedded flows (`deliverResultFromPush/Present`) were not correctly handling the result of the embedded flow
* Added `FragmentSharedElements` to provide a way to define shared elements for Fragment navigation, including a compatibility layer for Composable NavigationDestinations that want to use AndroidViews as shared elements with Fragments. See `FragmentsWithSharedElements.kt` in the test application for examples of how to use `FragmentSharedElements`
* Added `acceptFromFlow` as a `NavigationContainerFilter` for use on screens that build managed flows using `registerForFlowResult`. This filter will cause the `NavigationContainer` to only accept instructions that have been created as part a managed flow, and will reject instructions that are not part of a managed flow.
* Removed `isAnimating` from `ComposableNavigationContainer`, as it was unused internally, did not appear to be useful for external use cases, and was complicating Compose animation code. If this functionality *was* important to your use case, please create a Github issue to discuss your use case.
* Removed the requirement to provide a SavedStateHandle to `registerForFlowResult`. This should not affect any existing code, but if you were passing a SavedStateHandle to `registerForFlowResult`, you can now remove this parameter.
  * NavigationHandles now have access to a SavedStateHandle internally, which removes the requirement to pass this through to `registerForFlowResult`
* Added `managedFlowDestination` as a way to create a managed flow as a standalone destination
  * `managedFlowDestination` works in the same way you'd use `registerForFlowResult` to create a managed flow, but allows you to define the flow as a standalone destination that can be pushed or presented from other destinations, without the need to define a ViewModel and regular destination for the flow.
  * `managedFlowDestination` is currently marked as an `@ExperimentalEnroApi`, and may be subject to change in future versions of Enro.
  * For an example of a `managedFlowDestination`, see `dev.enro.tests.application.managedflow.UserInformationFlow` in the test application

* ⚠️ Updated result channel identifiers in preparation for Kotlin 2.0 ⚠️
  * Kotlin 2.0 changes the way that lambdas are compiled, which has implications for `registerForNavigationResult` and how result channels are uniquely identified. Activites, Fragments, Composables and ViewModels that use `by registerForNavigationResult` directly will not be affected by this change. However, if you are creating result channels inside of other objects, such as delegates, helper objects, or extension functions, you should verify that these cases continue to work as expected. It is not expected that there will be issues, but if this does result in bugs in your application, please raise them on the Enro GitHub repository. 

* ⚠️ Updated NavigationContainer handling of NavigationInstructionFilter ⚠️
  * In versions of Enro before 2.8.0, NavigationContainers would always accept destinations that were presented (`NavigationInstruction.Present(...)`, `navigationHandle.present(...)`, etc), and would only enforce their instructionFilter for pushed instructions (`NavigationInstruction.Push(...)`, `navigationHandle.push(...)`, etc). This is no longer the default behavior, and NavigationContainers will apply their instructionFilter to all instructions. 
  * This behavior can be reverted to the previous behavior by setting `useLegacyContainerPresentBehavior` when creating a NavigationController for your application using `createNavigationController`. 
  * `useLegacyContainerPresentBehavior` will be removed in a future version of Enro, and it is recommended that you update your NavigationContainers to explicitly declare their instructionFilter for all instructions, not just pushed instructions.

## 2.7.0
* ⚠️ Updated to androidx.lifecycle 2.8.1 ⚠️
  * There are breaking changes introduced in androidx.lifecycle 2.8.0; if you use Enro 2.7.0, you must upgrade your project to androidx.lifecycle 2.8+, otherwise you are likely to encounter runtime errors 

## 2.6.0
* Added `isManuallyStarted` to the `registerForFlowResult` API, which allows for the flow to be started manually with a call to `update` rather than performing this automatically when the flow is created.
* Added `async` to `NavigationFlowScope`, which allows the execution of suspending lambdas as part of the steps in a flow.

## 2.5.0
* Added `update` to the public API for `NavigationFlow`, as this is required for some use cases where the flow needs to be updated after changes in external state which may affect the logic of the flow. This function was previously named `next`, and removed from the public API in 2.4.0.
* Moved `NavigationContext.getViewModel` and `requireViewModel` extensions to the `dev.enro.viewmodel` package.
* Added `NavigationResultScope<Result, Key>` as a receiver for all registerForNavigationResult calls, to allow for more advanced handling of results and inspection of the instruction and navigation key that was used to open the result request.

## 2.4.1
* Added `EnroBackConfiguration`, which can be set when creating a `NavigationController`. This controls how Enro handles back presses.
  * EnroBackConfiguration.Default will use the behavior that has been standard in Enro until this point
  * EnroBackConfiguration.Manual disables all back handling via Enro, and allows developers to set their own back pressed handling for individual destinations
  * EnroBackConfiguration.Predictive is experimental, but adds support for predictive back gestures and animations. This is not yet fully implemented, and is not recommended for production use. Once this is stabilised, EnroBackNavigation.Default will be renamed to EnroBackNavigation.Legacy, and EnroBackNavigation.Predictive will become the default.
* Removed `ContainerRegistrationStrategy` from the "core" `rememberNavigationContainer` methods, to stop the requirement to opt-in for `AdvancedEnroApi` when using the standard `rememberNavigationContainer` APIs. This was introduced accidentally with 2.4.0.
* Added `EmbeddedNavigationDestination` as an experimental API, which allows a `NavigationKey.SupportsPush` to be rendered as an embedded destination within another Composable.

## 2.4.0
* Updated dependency versions
* Added `instruction` property directly to `NavigationContext`, to provide easy access to the instruction
* Added extensions `getViewModel` and `requireViewModel` to `NavigationContext` to access `ViewModels` directly from a context reference
* Added extensions for `findContext` and `findActiveContext` to `NavigationContext` to allow for finding other NavigationContexts from a context reference
* Updated `NavigationContainer` to add `getChildContext` which allows finding specific Active/ActivePushed/ActivePresented/Specific contexts from a container reference
* Added `instruction` property to `NavigationContext`, and marked `NavigationContext` as `@AdvancedEnroApi`
* Updated `NavigationContext` and `NavigationHandle` to bind each other to allow for easier access to the other from either reference, and to ensure the lazy references are still available while the context is being referenced
* Updated result handling for forwarding results to fix several bugs and improve behaviour (including correctly handling forwarded results through Activities)
* Added `transient` configuration to NavigationFlow steps, which allows a step to only be re-executed if it's dependencies have changed
* Added `navigationFlowReference` as a parcealble object which can be passed to NavigationKeys, and then later used to retrieve the parent navigation flow
* Prevent more than one registerForNavigationResult from occurring within the context of a single NavigationHandle
* Remove `next` from the public API of NavigationFlow, in favour of doing this automatically on creation of the flow
* Added a new version of `OverrideNavigationAnimations`, which provides a way to override animations and receive an `AnimatedVisibilityScope` which is useful for shared element transitions.

## 2.3.0
* Updated NavigationFlow to return from `next` after `onCompleted` is called, rather than continuing to set the backstack from the flow
* Updated NavigationContainer to take a `filter` of type NavigationContainerFilter instead of an `accept: (NavigationKey) -> Boolean` lambda. This allows for more advanced filtering of NavigationKeys, and this API will likely be expanded in the future. 
  * For containers that pass an argument of `accept = { <logic> }` a quick replacement is `filter = acceptKey { <logic> }`, which will have the same behavior.
* Updated EmptyBehavior to use `requestClose` for the CloseParent behavior, and added ForceCloseParent as a method for retaining the old behavior which will close the parent of the container without going through that destination's `onRequestClose`.
* Fixed a bug with nested Composable NavigationContainers and the active container being changed while the parent Composable was not active.  

## 2.2.0
* Removed NavigationAnimationOverrideBuilder methods that did not take a `returnEntering` or `returnExiting` parameter, in favour of defaulting these parameters to `entering` and `exiting` respectively. If you do not want to override return animations, you are able to pass null for these parameters to override the defaults.
* Removed default `EmptyBehavior` parameter for `rememberNavigationContainer`; an explicit EmptyBehaviour is now required. The default was previously `EmptyBehavior.AllowEmpty`, and usages of `rememberNavigationContainer` that were relying on this default parameter should be updated to pass this explicitly.
* Fixed a bug with `EnroTestRule` incorrectly capturing back presses for DialogFragments that are not bound into Enro

## 2.1.1
* Fixed a bug with `EnroTestRule`/`runEnroTest` that would cause instrumented `androidTest` tests to fail when including both tests that use `EnroTestRule`/`runEnroTest` and tests that do not in the same test suite

## 2.1.0
* Update to Compose 1.5.x
* Moved Activity/Fragment integrations out of the core of Enro and into independent plugins (which are still installed by default)
* Fixed a bug with NavigationResult channels not using the correct result channel id in some cases

## 2.0.0
Enro 2.0.0 introduces some important changes from the 1.x.x branch: 
* Compose destinations are now stable 
* The BottomSheetDestination and DialogDestination interfaces have been deprecated
  * Replace these with using the Composables named BottomSheetDestination and DialogDestination
  * See [DialogDestination.kt](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2Fdestinations%2Fcompose%2FDialogComposable.kt)
  * See [BottomSheetComposable.kt](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2Fdestinations%2Fcompose%2FBottomSheetComposable.kt)
* Synthetic destinations can be defined as properties
  * See [SimpleMessage.kt](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2Fdestinations%2Fsynthetic%2FSimpleMessage.kt)
* Forward/Replace instructions have been deprecated
  * Usages of Forward should be replaced with a mix of Push and/or Present 
  * See https://enro.dev/docs/frequently-asked-questions.html for an explanation of Push vs. Present
  * Usages of Replace should be replaced with a `push/present` followed by a `close`
* Both Composables and Fragments now use a shared NavigationContainer type to host navigation
  * See [MainActivity.kt](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2FMainActivity.kt) or [RootFragment.kt](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2FRootFragment.kt) for an example of Fragment containers
  * See [ListDetailComposable.kt](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2Fdestinations%2Flistdetail%2Fcompose%2FListDetailComposable.kt) for an example of Composable containers
  * The `OnContainer` Navigation Instruction has been added, which allows direct backstack manipulation of NavigationContainers
  * NavigationContainers allow advanced functionality such as interceptors and animation overrides
* `deliverResultFromPush`/`deliverResultFromPresent` are new extension functions which allow a screen to delegate it's result to another screen
  * See the [embedded flow](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2Fdestinations%2Fresult%2Fflow%2Fembedded) for examples
* `activityResultDestination` is a new function which allows ActivityResultContracts to be used directly as destinations
  * See [ActivityResults.kt](example%2Fsrc%2Fmain%2Fjava%2Fdev%2Fenro%2Fexample%2Fdestinations%2Factivity%2FActivityResults.kt)