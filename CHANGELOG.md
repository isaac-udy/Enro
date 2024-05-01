# Changelog

## Unreleased
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