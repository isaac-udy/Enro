[![Maven Central](https://img.shields.io/maven-central/v/dev.enro/enro.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.enro%22)
> **Note**
>
> Enro 2.x.x has now merged to the main branch, but is still in an alpha/beta release phase. The Enro 1.x.x branch is still being maintained for bug fixes and can be found [here](https://github.com/isaac-udy/Enro/tree/1.x.x)

# Enro 🗺️

A simple navigation library for Android

*"The novices’ eyes followed the wriggling path up from the well as it swept a great meandering arc around the hillside. Its stones were green with moss and beset with weeds. Where the path disappeared through the gate they noticed that it joined a second track of bare earth, where the grass appeared to have been trampled so often that it ceased to grow. The dusty track ran straight from the gate to the well, marred only by a fresh set of sandal-prints that went down, and then up, and ended at the feet of the young monk who had fetched their water." - [The Garden Path](http://thecodelesscode.com/case/156)*

## Features

- Navigate between Fragments, Activities and @Composables seamlessly

- Describe navigation destinations through annotations or a simple DSL

- Remove navigation logic from screens implemented as Fragments, Activities or @Composables

- Pass type-safe results between screens across configuration changes and process death

- Create beautiful transitions between specific destinations

## Using Enro
#### Gradle
Enro is published to [Maven Central](https://search.maven.org/). Make sure your project includes the `mavenCentral()` repository, and then include the following in your module's build.gradle:

```gradle
dependencies {
    implementation "dev.enro:enro:2.0.0-beta04"
    kapt "dev.enro:enro-processor:2.0.0-beta04"
}
```

#### 1. Define your NavigationKeys

```kotlin
@Parcelize
data class MyListKey(val listType: String) : NavigationKey.SupportsPush

@Parcelize
data class MyDetailKey(val itemId: String, val isReadOnly) : NavigationKey.SupportsPush

@Parcelize
data class MyComposeKey(val name: String) : NavigationKey.SupportsPresent

@Parcelize
data class MyResultKey(val query: String) : NavigationKey.SupportsPresent.WithResult<String>
```

#### 2. Define your NavigationDestinations
```kotlin
@NavigationDestination(MyListKey::class)
class ListFragment : Fragment()

@NavigationDestination(MyDetailKey::class)
class DetailActivity : AppCompatActivity()

@Composable
@NavigationDestination(MyComposeKey::class)
fun MyComposableScreen() { }
```

#### 3. Annotate your Application as a NavigationComponent, and implement the NavigationApplication interface
```kotlin
@NavigationComponent
class MyApplication : Application(), NavigationApplication {
    override val navigationController = navigationController()
}
```

#### 4. Navigate!
```kotlin
@NavigationDestination(MyListKey::class)
class ListFragment : ListFragment() { 
    val navigation by navigationHandle<MyListKey>()
    
    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val listType = navigation.key.listType
        view.findViewById<TextView>(R.id.list_title_text).text = "List: $listType"
    }
    
    fun onListItemSelected(selectedId: String) {
        val key = MyDetailKey(itemId = selectedId)
        navigation.push(key)
    }
}

@Composable
@NavigationDestination(MyComposeKey::class)
fun MyComposableScreen() {
    val navigation = navigationHandle<MyComposeKey>()

    Button(
        content = { Text("Hello, ${navigation.key}") },
        onClick = {
            navigation.push(MyListKey(...))
        }
    )
}

```

## Applications using Enro
<p align="center">
    <a href="https://www.splitwise.com/">
        <img width="100px" src="resources/splitwise-icon.png" />
    </a>
   &nbsp;
   &nbsp;
    <a href="https://play.google.com/store/apps/details?id=com.beyondbudget">
        <img width="100px" src="resources/beyond-budget-icon.png" />
    </a>
</p>

## FAQ
#### Minimum SDK Version

Enro supports a minimum SDK version of 21

#### How well does Enro work alongside "normal" Android Activity/Fragment navigation?  
Enro is designed to integrate well with Android's default navigation. It's easy to manually open a Fragment or Activity as if Enro itself had performed the navigation. Create a NavigationInstruction object that represents the navigation, and then add it to the arguments of a Fragment, or the Intent for an Activity, and then open the Fragment/Activity as you normally would. 

Example:
```kotlin
val instruction = NavigationInstruction.Present(
    navigationKey = MyNavigationKey(...
)
)
val intent = Intent(this, MyActivity::class).addOpenInstruction(instruction)
startActivity(intent)
```

#### How does Enro decide if a Fragment, or the Activity should receive a back button press?

Enro considers the primaryNavigationFragment to be the "active" navigation target, or the current
Activity if there is no primaryNavigationFragment. In a nested Fragment situation, the
primaryNavigationFragment of the primaryNavigationFragment of the ... is considered "active".

#### What kind of navigation instructions does Enro support?

Enro supports three navigation instructions: `push`, `present` and `replaceRoot`.

#### How does Enro support Activities navigating to Fragments?

When an Activity executes a navigation instruction that resolves to a Fragment, one of three things
will happen:

1. If the instruction is a "push", and the Activity defines a "navigationContainer" that accepts the Fragment's type the Fragment will be opened into the container view defined by that container.
2. If the instruction is a "present", or the Activity **does not** define a navigationContainer that acccepts the Fragment's type the Fragment will be opened into a either a floating, full window dialog, or a full screen Activity (depending on the situation).
3. If the instruction is a "replaceRoot" the Fragment will be opened in a full screen Activity

#### How does Enro support Activities and Fragments navigating to @Composables?

When an Activity or Fragment executes a navigation instruction that resolves to a @Composable, one of three things will happen:

1. If the instruction is a "push", and the Activity/Fragment defines a "navigationContainer" that accepts the @Composable's type the @Composable will be opened into the container view defined by that container.
2. If the instruction is a "present", or the Activity/Fragment **does not** define a navigationContainer that acccepts the @Composable's type the @Composable will be opened into a either a floating, full window dialog, or a full screen Activity (depending on the situation).
3. If the instruction is a "replaceRoot" the @Composable will be opened in a full screen Activity

#### How do I deal with passing results between screens?

Enro supports any NavigationKey/NavigationDestination providing a result. Instead of implementing the NavigationKey interface on the NavigationKey that provides the result, implement NavigationKey.<TYPE>.WithResult<T> where T is the type of the result. Once you're ready to navigate to that NavigationKey and consume a result, you'll want to call "registerForNavigationResult" in your Fragment/Activity/ViewModel. This API is very similar to the AndroidX Activity 1.2.0 ActivityResultLauncher.

Example:

```kotlin
@Parcelize
class RequestDataKey(...) : NavigationKey.WithResult<Boolean>()

@NavigationDestination(RequestDataKey::class)
class MyResultActivity : AppCompatActivity() {
    val navigation by navigationHandle<RequestSomeData>()

    fun onSendResultButtonClicked() {
        navigation.closeWithResult(false)
    }
}

@NavigationDestination(...)
class MyActivity : AppCompatActivity() {
    val requestData by registerForNavigationResult<Boolean> {
        // do something!
    }

    fun onRequestDataButtonClicked() {
        requestData.open(RequestDataKey(/*arguments*/))
    }
}
```

#### How do I do List/Detail navigation 
Enro has a built in component for this.  If you want to build something more complex than what the built-in component provides, you'll be able to use the built-in component as a reference/starting point, as it is built purely on Enro's public API

#### How do I handle multiple backstacks on each page of a BottomNavigationView? 

Each `navigationContainer` has it's own backstack. The suggested implementation is to create one `navigationContainer` for each

#### I'd like to do shared element transitions, or do something special when navigating between certain screens
Enro allows you to define "NavigationExecutors" as overrides for the default behaviour, which handle these situations. 

There will be an example project that shows how this all works in the future, but for now, here's a basic explanation: 
1. A NavigationExecutor is typed for a "From", an "Opens", and a NavigationKey type. 
2. Enro performs navigation on a "NavigationContext", which is basically either a Fragment or a FragmentActivity
3. A NavigationExecutor defines two methods
   * `open`, which takes a NavigationContext of the "From" type, a NavigationBinding for the "Opens" type, and a NavigationInstruction (i.e. the From context is attempting to open the NavigationBinding with the input NavigationInstruction)
   * `close`, which takes a NavigationContext of the "Opens" type (i.e. you're closing what you've already opened)
4. By creating a NavigationExecutor between two specific screens and registering this with the NavigationController, you're able to override the default navigation behaviour (although you're still able to call back to the DefaultActivityExecutor or DefaultFragmentExecutor if you need to)
5. See the method in NavigationControllerBuilder for `override`
6. When a NavigationContext decides what NavigationExecutor to execute an instruction on, Enro will look at the NavigationContext originating the NavigationInstruction and then walk up toward's it's root NavigationContext (i.e. a Fragment will check itself, then its parent Fragment, and then that parent Fragment's Activity), checking for an appropriate override along the way. If it finds no override, the default will be used. NavigationContexts that are the children of the current NavigationContext will not be searched, only the parents. 

Example: 
```kotlin
// This override will place the "DetailFragment" into the container R.id.detail, 
// and when it's closed, will set whatever Fragment is in the R.id.master container as the primary navigation fragment
override<ListDetailActivity, DetailFragment>(
    launch = {
        val fragment =  DetailFragment().addOpenInstruction(it.instruction)
        it.fromContext.childFragmentManager.beginTransaction()
            .replace(R.id.detail, fragment)
            .setPrimaryNavigationFragment(fragment)
            .commitNow()
    },
    close = { context ->
        context.fragment.parentFragmentManager.beginTransaction()
            .remove(context.fragment)
            .setPrimaryNavigationFragment(context.parentActivity.supportFragmentManager.findFragmentById(R.id.master))
            .commitNow()
    }
)
```

#### I'd like to add a custom animation (using an override) for a @Composable @NavigationDestination
Unlike Activities and Fragments, when you want to write an override for a @Composable @NavigationDestination (particularly to specify custom animations), you don't have a class to reference in the To or From type arguments to the `override<To, From>()` function. At first glance, it may appear that it is not possible to create an override for a @Composable @NavigationDestination.

However, when you define a @Composable @NavigationDestination, Enro generates a class, called `<YourComposableName>Destination`. This class can be used when specifying overrides for @Composable @NavigationDestinations. 

Example:
```kotlin
val navigationController = navigationController {
   /**
    * This example assumes you have a @Composable function that is also a @NavigationDestination, and that the name
    * of the @Composable function is `MyComposableScreen`. 
    * 
    * This example will set both the open and close animations for this screen to be the default "no animation" animation
    * that Enro provides. 
    */
    override<MyActivityOrFragment, MyComposableScreenDestination> {
       animation { DefaultAnimations.none }
       closeAnimation { DefaultAnimations.none }
    }
}
```

Please note, that the `<YourComposableName>Destination` is a generated class, and will not be available until you've compiled the project at least once since defining your @Composable @NavigationDestination (similar to how Dagger generates Components).

#### My Activity crashes on launch, what's going on?!
It's possible for an Activity to be launched from multiple places. Most of these can be controlled by Enro, but some of them cannot. For example, an Activity that's declared in the manifest as a MAIN/LAUNCHER Activity might be launched by the Android operating system when the user opens your application for the first time. Because Enro hasn't launched the Activity, it's not going to know what the NavigationKey for that Activity is, and won't be able to read it from the Activity's intent. 

Luckily, there's an easy solution! When you declare an Activty or Fragment, you are able to do a small amount of configuration inside the `navigationHandle` block using the `defaultKey` method. This method takes a `NavigationKey` as an argument, and if the Fragment or Activity is opened without being passed a `NavigationKey` as part of its arguments, the value passed will be treated as the `NavigationKey`. This could occur because of an Activity being launched via a MAIN/LAUNCHER intent filter, via a standard `Intent`, or via a `Fragment` being added directly to a `FragmentManager` without any `NavigationInstruction` being applied. In other words, any situation where Enro is not used to launch the Activity or Fragment. 

Example: 
```kotlin
@Parcelize
class MainKey(isDefaultKey: Boolean = false) : NavigationKey

@NavigationDestination(MainKey::class)
class MainActivity : AppCompatActivity() {
    private val navigation by navigationHandle<MainKey> {
        defaultKey(
            MainKey(isDefaultKey = true)
        )
    }
}
```

## Why would I want to use Enro? 
#### Support the navigation requirements of large multi-module Applications, while allowing flexibility to define rich transitions between specific destinations

A multi-module application has different requirements to a single-module application. Individual modules will define Activities, Fragments or @Composables, and other modules will want to navigate to these Activities/Fragments/@Composables. By detatching the NavigationKeys from the destinations themselves, this allows NavigationKeys to be defined in a common/shared module which all other modules depend on. Any module is then able to navigate to another by using one of the NavigationKeys, without knowing about the Activity or Fragment that it is going to. FeatureOneActivity and FeatureTwoActivity don't know about each other, but they both know that FeatureOneKey and FeatureTwoKey exist. A simple version of this solution can be created in less than 20 lines of code.

However, truly beautiful navigation requires knowledge of both the originator and the destination. Material design's shared element transitions are an example of this. If FeatureOneActivity and FeatureTwoActivity don't know about each other, how can they collaborate on a shared element transition? Enro allows transitions between two navigation destinations to be overridden for that specific case, meaning that FeatureOneActivity and FeatureTwoActivity might know nothing about each other, but the application that uses them will be able to define a navigation override that adds shared element transitions between the two.

#### Allow navigation to be triggered at the ViewModel layer of an Application
Enro provides a custom extension function similar to AndroidX's `by viewModels()`, called `by enroViewModels()`, which works in the exact same way. However, when you use `by enroViewModels()` to construct a ViewModel, you are able to use a `by navigationHandle<NavigationKey>()` statement within your ViewModel. This `NavigationHandle` works in the exact same way as an Activity or Fragment's `NavigationHandle`, and can be used in the exact same way. 

This means that your ViewModel can be put in charge of the flow through your Application, rather than needing to use a `LiveData<NavigationEvent>()` (or similar) in your ViewModel. When we use things like `LiveData<NavigationEvent>()` we are able to test the ViewModel's intent to navigate, but there's still the reliance on the Activity/Fragment implementing the response to the navigation event correctly. In the case of retrieving a result from another screen, this gap grows even wider, and there becomes an invisible contract between the ViewModel and Activity/Fragment: The ViewModel expects that if it sets a particular `NavigationEvent` in the `LiveData`, that the Activity/Fragment will navigate to the correct place, and then once the navigation has been successful and a result has been returned, that the Activity/Fragment will call the correct method on the ViewModel to provide the result. This invisible contract results in extra boilerplate "wiring" code, and a gap for bugs to slip through. Instead, using Enro's ViewModel integration, you allow your ViewModel to be precise and clear about it's intention, and about how to handle a result. 

## Compose Support
Here is an example of a Composable function being used as a NavigationDestination:

```kotlin
@Composable
@NavigationDestination(MyComposeKey::class)
fun MyComposableScreen() {
    val navigation = navigationHandle<MyComposeKey>()

    Button(
        content = { Text("Hello, ${navigation.key}") },
        onClick = {
            navigation.forward(MyListKey(...))
        }
    )
}
```

#### Nested Composables
Enro's Composable support is based around the idea of an "EnroContainer" Composable, which can be added to a Fragment, Activity or another Composable. The EnroContainer works much like a FrameLayout being used as a container for Fragments.

Here is an example of creating a Composable that supports nested Composable navigation in Enro:

```kotlin
@Composable
@NavigationDestination(MyComposeKey::class)
fun MyNestedComposableScreen() {
    val navigation = navigationHandle<MyComposeKey>()
    val navigationContainer = rememberNavigationContainer(
        accept = { it is NestedComposeKey }
    )

    Column {
        EnroContainer(
            container = navigationContainer
        )
        Button(
            content = { Text("Open Nested") },
            onClick = {
                navigation.forward(NestedComposeKey())
            }
        )
    }
}

@Composable
@NavigationDestination(NestedComposeKey::class)
fun NestedComposableScreen() = Text("Nested Screen!")
```

In the example above, we have defined an Enro Container Controller which will accept Navigation Keys of type "NestedComposeKey". When the user clicks on the button "Open Nested", we execute a forward instruction to a NestedComposeKey. Because there is an available container which accepts NestedComposeKey instructions, the Composable for the NestedComposeKey (NestedComposableScreen in the example above) will be placed inside the EnroContainer defined in MyNestedComposableScreen.

EnroContainerControllers can be configured to have some instructions pre-launched as their initial state, can be configured to accept some/all/no keys, and can be configured with an "EmptyBehavior" which defines what will happen when the container becomes empty due to a close action. The default close behavior is "AllowEmpty", but this can be set to "CloseParent", which will pass the close instruction up to the Container's parent, or "Action", which will allow any custom action to occur when the container becomes empty.

#### Dialog and BottomSheet support
Composable functions declared as NavigationDestinations can be used as Dialog or ModalBottomSheet type destinations. To do this, make the Composable function an extension function on either `DialogDestination` or `BottomSheetDestination`. This will cause the Composable to be launched as a dialog, escaping the current navigation context of the screen.

Here's an example:

```kotlin
@Composable
@NavigationDestination(DialogComposableKey::class)
fun DialogDestination.DialogComposableScreen() {
    configureDialog { ... }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
@NavigationDestination(BottomSheetComposableKey::class)
fun BottomSheetDestination.BottomSheetComposableScreen() {
    configureBottomSheet { ... }
}
```