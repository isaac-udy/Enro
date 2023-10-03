# Changelog

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