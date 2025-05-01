---
title: Frequently Asked Questions
nav_order: 99
---
# Frequently Asked Questions


<details markdown="block">
  <summary class="faq-summary">
    What's a NavigationKey?
  </summary>

A NavigationKey is a contract for a screen. It defines the inputs/parameters/arguments for a screen, and potentially the type of results returned from the screen (if any).

When you perform navigation, you ask for a particular NavigationKey to be opened, and the screen/destination that is connected to that NavigationKey will be opened.

From within a screen/destination, you have access to the NavigationKey that was used when opening it, and you can use this to read the inputs/parameters/arguments that were used.

See [NavigationKeys](./navigation-keys.md) for more information.

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I connect a NavigationKey to a screen/destination?
  </summary>

Using KAPT or KSP, annotate the screen/destination with `@NavigationDestination`, and pass in the class reference to the NavigationKey.

```kotlin

// Composables:
@Parcelize
class ExampleComposableKey : NavigationKey.SupportsPush

@Composable
@NavigationDestination(ExampleComposableKey::class)
fun ExampleComposableScreen() {}

// Fragments:
@Parcelize
class ExampleFragmentKey : NavigationKey.SupportsPresent

@NavigationDestination(ExampleFragmentKey::class)
class ExampleFragment : Fragment() {}

// Activities:
@Parcelize
class ExampleActivityKey : NavigationKey.SupportsPresent

@NavigationDestination(ExampleActivityKey::class)
class ExampleActivity : AppCompatActivity() {} // Or FragmentActivity, or ComponentActivity

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I open a screen/destination?
  </summary>

Once you've defined a NavigationKey for your screen/destination:
1. On a different screen, get a reference to a NavigationHandle
2. Use the `.push` or `.present` function on the NavigationHandle (depending on whether your NavigationKey is SupportsPush or SupportsPresent)
3. Pass in an instance of your NavigationKey

```kotlin

val navigation: NavigationHandle = TODO() // up to you!
navigation.push( ExampleNavigationKey() )

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I close a screen/destination?
  </summary>

Get the NavigationHandle for the screen and use `close` or `requestClose`. 

`close` will always cause the screen to be closed. 

`requestClose` is the same as pressing the Android back button, and is a "softer" way of asking a screen to close. It is possible to configure the behaviour for `requestClose` to perform some side effect (e.g. a confirmation).

```kotlin

val navigation: NavigationHandle = TODO() // up to you!
navigation.close()

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I open a screen if I want a result from that screen/destination?
  </summary>

Create a NavigationResultChannel, by using `registerForNavigationResult<T>()`, and then use the NavigationResultChannel to push or present the NavigationKey you want to get a result from. If you do not use the NavigationResultChannel to push or present, the result will not get delivered. If you have multiple NavigationResultChannels, the result will be delivered to the NavigationResultChannel that was used to push or present.

```kotlin

class ExampleResultKey : NavigationKey.SupportsPresent.WithResult<Boolean>

@Composable
fun ExampleComposable() {
    val exampleResult = registerForNavigationResult<Boolean> { result: Boolean ->
        // handle result
    }
    LaunchedEffect(Unit) {
        exampleResult.present(ExampleResultKey())
    }
}

class ExampleViewModel : ViewModel() {
    val exampleResult by registerForNavigationResult<Boolean> { result: Boolean -> 
        // handle result
    }
    fun startResultFlow() {
        exampleResult.present(ExampleResultKey())
    }
}

class ExampleFragment : Fragment() { 
    val exampleResult by registerForNavigationResult<Boolean> { result: Boolean ->
        // handle result
    }
    fun startResultFlow() {
        exampleResult.present(ExampleResultKey())
    }
}

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I send a result from a screen/destination?
  </summary>

Make sure that the NavigationKey for that screen/destination extends `...WithResult<T>` (e.g. `NavigationKey.SupportsPresent.WithResult<T>`).

Get a `TypedNavigationHandle` for the screen, with the correct NavigationKey type.

Call `closeWithResult` and pass in an object that matches `T` from the NavigationKey's `...WithResult<T>`. 

```kotlin

class ExampleResultKey : NavigationKey.SupportsPush.WithResult<ExampleResultType> 

val navigation: TypedNavigationHandle<ExampleResultKey> = TODO() // up to you!
navigation.closeWithResult(ExampleResultType(/*...*/))

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I get a NavigationHandle?
  </summary>

In a Composable, use `= navigationHandle<T>()`
```kotlin

@Composable
fun ExampleComposable() {
    val navigation = navigationHandle<T>()
}

```

In a ViewModel, use `by navigationHandle<T>()`, but make sure you've set up your ViewModel factory correctly, see [ViewModels](./viewmodels.md).
```kotlin

class ExampleViewModel() : ViewModel() {
    val navigation by navigationHandle<T>()
}

```

In an Activity or Fragment, use `by navigationHandle<T>()`
```kotlin

class ExampleActivity : Activity {
    val navigation by navigationHandle<ExampleNavigationKey>()
} 

```
</details>

<details markdown="block">
  <summary class="faq-summary">
    What's the difference between Push and Present?
  </summary>

When you "push" a screen/destination, you're saying that the screen should be the top element of it's container, and it should be the only thing rendered within the container.

When you "present" a screen/destination, you're saying that the screen should appear above the most recently pushed screen. Generally, these destinations are Dialogs, BottomSheets, or similar. Activities are also always considered to be presented, because they cannot be contained within a container.

For example, if you have a container with a backstack that looks like this: <br>
`push(A), push(B), push(C)`, that container will show "C", and no other screens will be visible. 

If you pushed "D", and the backstack became:<br>
`push(A), push(B), push(C), push(D)`, then "C" would animate out, and "D" would become visible. "C" would become inactive.

But if you presented "D" instead, and the backstack was:<br>
`push(A), push(B), push(C), present(D)`, then "C" would not animate out, and both "C" and "D" would be visible (assuming that D did not cover the entire screen). "C" remains active in the background.

If "D" then pushed to "E", and the backstack was:<br>
`push(A), push(B), push(C), present(D), push(E)`, then both "C" and "D" would animate out, and "E" would be visible. Once "E" was closed, both "C" and "D" would become visible again.

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I create a BottomSheet or a Dialog screen in Compose?
  </summary>

Create a Composable NavigationDestination, and then call either `DialogDestination` or `BottomSheetDestination` as the root of the Composable. These destinations should generally be presented, as they should appear above the previous screen. 

```kotlin

/**
 * This is an example of creating a DialogDestination in Compose, using the standard
 * Dialog Composable.
 */
@Parcelize
object ExampleDialog : NavigationKey.SupportsPresent

@Composable
@NavigationDestination(ExampleBottomSheet::class)
fun ExampleDialogScreen() = DialogDestination {
    val navigation = navigationHandle()
    Dialog(
        onDismissRequest = { navigation.requestClose() }
    ) {
        // Render screen contents
    }
}

/**
 * This is an example of creating a BottomSheetDestination in Compose. The BottomSheetDestination
 * lambda receives a "ModalBottomSheetState" object, which should be passed to a ModalBottomSheetLayout.
 * Arguments such as "skipHalfExpanded" can be passed in to the BottomSheetDestination function.
 */
@Parcelize
object ExampleBottomSheet : NavigationKey.SupportsPresent

@Composable
@NavigationDestination(ExampleBottomSheet::class)
fun ExampleBottomSheetScreen() = BottomSheetDestination { sheetState ->
    BottomSheetDestination { sheetState ->
        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetContent = {
                // Render screen contents
            },
            content = {}
        )
    }
}

/**
 * This is an example of creating a NavigationDestination which can be pushed OR presented. If this
 * destination is pushed, it will be rendered in a Box as a regular screen, but if it is presented,
 * it will be rendered inside of a ModalBottomSheetLayout, using BottomSheetDestination.
 */
@Parcelize
object ExampleBottomSheetOrNot : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@Composable
@NavigationDestination(ExampleBottomSheetOrNot::class)
fun ExampleBottomSheetOrNotScreen() {
    val navigation = navigationHandle()
    val isPresented = navigation.instruction.navigationDirection == NavigationDirection.Present
    
    if(isPresented) {
        BottomSheetDestination { sheetState -> 
            ModalBottomSheetLayout(
                sheetState = sheetState,
                sheetContent = {
                    // Render screen contents
                },
                content = {}
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Render screen contents
        }
    }
}

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    What's a SyntheticDestination?
  </summary>

A "SyntheticDestination" is a destination that's not a Composable/Fragment/Activity, it's a way to create a NavigationKey that can be used to perform a UI/Context-aware side-effect as if it was a navigation action.

For example, you might use a SyntheticDestination to open an Intent, make a runtime permission request, set a container's backstack, use a feature flag to open one of two different NavigationKeys, or as a placeholder for a screen that hasn't been implemented yet. 

A SyntheticDestination receives the NavigationKey, NavigationInstruction, and NavigationContext reference of the destination that was used to open the SyntheticDestination, and can use these to perform any kind of logic.

```kotlin

/**
 * This is an example of launching an implicit Intent to view a URL using a SyntheticDestination
 */
@Parcelize
object OpenEnroDocumentationDestination : NavigationKey.SupportsPresent

@NavigationDestination(OpenEnroDocumentationDestination::class)
val openEnroDocumentationDestination = syntheticDestination<OpenEnroDocumentationDestination> {
    val activity = navigationContext.activity
    val url = "https://www.enro.dev"
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    activity.startActivity(intent)
}

/**
 * ShowDocumentDestination is an example of using a SyntheticDestination to pick between two
 * different "real" destinations, based on a feature flag. We can get the NavigationHandle from 
 * the NavigationContext, and then use this to push to other NavigationKeys based on a feature flag, 
 * passing through some of the arguments from ShowDocumentDestination to the other NavigationKeys 
 */
@Parcelize
class ShowDocumentDestination(
    val documentId: String
) : NavigationKey.SupportsPush

@NavigationDestination(ShowDocumentDestination::class.java)
val showDocumentDestination = syntheticDestination<ShowDocumentDestination> {
    val navigation = navigationContext.getNavigationHandle()
    val featureFlags = getFeatureFlagsFromSomewhere()
    if (featureFlags.isNewDocumentsEnabled) {
        navigation.push(
            NewShowDocumentDestination(
                documentId = key.documentId
            )
        )
    } else {
        navigation.push(
            LegacyShowDocumentDestination(
                documentId = key.documentId
            )
        )
    }
}

/**
 * DatePickerDestination is an example of using a SyntheticDestination as a placeholder while
 * a destination hasn't been implemented yet (likely during development time). We'll show a
 * Toast to announce that the DatePickerDestination hasn't been implemented, and then we'll
 * also send a result of LocalDate.now() (because DatePickerDestination is a result destination)
 */
@Parcelize
object DatePickerDestination : NavigationKey.SupportsPresent.WithResult<LocalDate>

@NavigationDestination(DatePickerDestination::class)
val datePickerDestination = syntheticDestination<DatePickerDestination> {
    Toast.makeText(
        navigationContext.activity,
        "DatePickerDestination is not yet implemented",
        Toast.LENGTH_LONG
    ).show()

    sendResult(LocalDate.now())
}

/**
 * RequestCameraPermission is an example of using `activityResultDestination`,
 * which is a special case SyntheticDestination builder that allows interoperability
 * with ActivityResultContracts
 */
@Parcelize
class RequestCameraPermission : NavigationKey.SupportsPresent.WithResult<RequestCameraPermission.Result> {
    enum class Result {
        GRANTED,
        DENIED,
        DENIED_PERMANENTLY,
    }
}

@NavigationDestination(RequestCameraPermission::class)
val requestCameraPermission = activityResultDestination(RequestCameraPermission::class) {
    ActivityResultContracts.RequestPermission()
        .withInput(Manifest.permission.CAMERA)
        .withMappedResult { granted ->
            when {
                granted -> RequestCameraPermission.Result.GRANTED
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> RequestCameraPermission.Result.DENIED
                else -> RequestCameraPermission.Result.DENIED_PERMANENTLY
            }
        }
}

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I configure animations?
  </summary>

In the configuration for your application's `navigationController`, you can provide an `animations { }` block, which allows you to configure animations for a variety of situations. This can also be configured within a `navigationModule`, which can be installed on the `navigationController`, or can be configured on an individual `navigationContainer`.

```kotlin

val specificNavigationModule = createNavigationModule {
    animations {
        // Configure the default animations for destinations that are pushed
        direction(
            direction = NavigationDirection.Push, 
            entering = yourAnimationHere,
            exiting = yourAnimationHere,
            returnEntering = yourAnimationHere,
            returnExiting = yourAnimationHere,
        )
        
        // Configure an animations for when any destination opens the "ExampleComposableKey"
        transitionTo<ExampleComposableKey>(
            entering = yourAnimationHere,  // the entering animation for ExampleComposableKey
            exiting = yourAnimationHere, // the exiting animation for the destination that opened ExampleComposableKey
            returnEntering = yourAnimationHere,  // the entering animation for the destination that opened ExampleComposableKey, when ExampleComposableKey is closed
            returnExiting = yourAnimationHere, // the exiting animation for ExampleComposableKey, when ExampleComposableKey is closed
        )

        // Configure an animations for when FooKey opens BarKey
        transitionBetween<FooKey, BarKey>(
            entering = yourAnimationHere,  // the entering animation for BarKey
            exiting = yourAnimationHere, // the exiting animation for FooKey
            returnEntering = yourAnimationHere,  // the entering animation for FooKey when BarKey is closed
            returnExiting = yourAnimationHere, // the exiting animation for BarKey when BarKey is closed
        )

        // Advanced APIs for adding animations in more complex situations
        addOpeningTransition(/* ... */) 
        addClosingTransition(/* ... */) 
    }
}

class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        module(specificNavigationModule) // install the module defined outside of the application
        animations {
            // this block has the same functionality as the 
            // animations block in specificNavigationModule above
        }
    }
}

@Composable
fun ExampleScreen() {
    val container = rememberNavigationContainer(
        animations = {
            // this block has the same functionality as the 
            // animations block in specificNavigationModule above
        }
    )
    // ...
}

```

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I do analytics when a user views a screen?
  </summary>

Enro allows you to create `EnroPlugin` classes, and register these with the `navigationController`. These plugins can be used to perform side-effects when a screen is opened or closed, and can be used to perform analytics, logging, or any other side-effect. The `EnroLogger` plugin that is defined within the Enro library is an example of this. The key functions to be interested in are:
* `onOpened(navigationHandle: NavigationHandle)` which is called the first time a screen is opened. This should be invoked once per screen.
* `onActive(navigationHandle: NavigationHandle)` which is called whenever a screen becomes "active", which essentially means whenever that screen would receive the system back button press. This can be invoked multiple times for a screen. 
* `onClosed(navigationHandle: NavigationHandle)` which is called whenever a screen is closed. This should be invoked once per screen.

</details>
