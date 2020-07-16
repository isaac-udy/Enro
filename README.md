[![Download](https://api.bintray.com/packages/isaac-udy/Enro/enro/images/download.svg) ](https://bintray.com/isaac-udy/Enro/enro/_latestVersion)

# Enro 🗺️
A simple navigation library for Android 

*"The novices’ eyes followed the wriggling path up from the well as it swept a great meandering arc around the hillside. Its stones were green with moss and beset with weeds. Where the path disappeared through the gate they noticed that it joined a second track of bare earth, where the grass appeared to have been trampled so often that it ceased to grow. The dusty track ran straight from the gate to the well, marred only by a fresh set of sandal-prints that went down, and then up, and ended at the feet of the young monk who had fetched their water." - [The Garden Path](http://thecodelesscode.com/case/156)*

## Features

- Navigate between Fragments or Activities seamlessly

- Describe navigation destinations through annotations or a simple DSL

- Create beautiful transitions between specific destinations

- Remove navigation logic from Fragment or Activity implementations

## Using Enro
#### Gradle
Enro is published to [JCenter](https://bintray.com/beta/#/isaac-udy/Enro/enro-core?tab=overview). Make sure your project includes the jcenter repository, and then include the following in your module's build.gradle: 
```gradle
dependencies {
    implementation "nav.enro:enro:1.0.6"
    kapt "nav.enro:enro-processor:1.0.6"
}
```

#### 1. Define your NavigationKeys
```kotlin
@Parcelize
data class MyListKey(val listType: String): NavigationKey

@Parcelize
data class MyDetailKey(val itemId: String, val isReadOnly): NavigationKey
```

#### 2. Define your NavigationDestinations
```kotlin 
@NavigationDestination(MyListKey::class)
class ListFragment : Fragment()

@NavigationDestination(MyDetailKey::class)
class DetailActivity : AppCompatActivity() 
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
        navigation.forward(key)
    }
}
```

## FAQ
#### How well does Enro work alongside "normal" Android Activity/Fragment navigation?  
Enro is designed to integrate well with Android's default navigation. It's easy to manually open a Fragment or Activity as if Enro itself had performed the navigation. Create a NavigationInstruction object that represents the navigation, and then add it to the arguments of a Fragment, or the Intent for an Activity, and then open the Fragment/Activity as you normally would. 

Example:
```kotlin
val instruction = NavigationInstruction.Open(
    navigationDirection = NavigationDirection.Open,
    navigationKey = MyNavigationKey(...)
)
val intent = Intent(this, MyActivity::class).addOpenInstruction(instruction)
startActivity(intent)
```

#### How does Enro decide if a Fragment, or the Activity should receive a back button press?
Enro considers the primaryNavigationFragment to be the "active" navigation target, or the current Activity if there is no primaryNavigationFragment. In a nested Fragment situation, the primaryNavigationFragment of the primaryNavigationFragment of the ... is considered "active".

#### What kind of navigation instructions does Enro support?
Enro  supports three navigation instructions: `forward`, `replace` and `replaceRoot`.  

If the current navigation stack is `A -> B -> C ->` then:  
`forward(D)` = `A -> B -> C -> D ->`  
`replace(D)` = `A -> B -> D ->`  
`replaceRoot(D)` = `D ->`  

Enro supports multiple arguments to these instructions.  
`forward(X, Y, Z)` = `A -> B -> C -> X -> Y -> Z ->`  
`replace(X, Y, Z)` = `A -> B -> X -> Y -> Z ->`  
`replaceRoot(X, Y, Z)` = `X -> Y -> Z ->`  

#### How does Enro support Activities navigating to Fragments? 
When an Activity executes a navigation instruction that resolves to a Fragment, one of two things will happen: 
1. The Activity's navigator defines a "container" that accepts the Fragment's type, in which case, the Fragment will be opened into the container view defined by that container.
2. The Activity's navigation **does not** define a fragment host that acccepts the Fragment's type, in which case, the Fragment will be opened into a new, full screen Activity. 

#### How do I deal with Activity results? 
Enro supports any NavigationKey/NavigationDestination providing a result. Instead of implementing the NavigationKey interface on the NavigationKey that provides the result, implement ResultNavigationKey<T> where T is the type of the result. Once you're ready to navigate to that NavigationKey and consume a result, you'll want to call "registerForNavigationResult" in your Fragment/Activity/ViewModel. This API is very similar to the AndroidX Activity 1.2.0 ActivityResultLauncher. 

Example:
```kotlin
@Parcelize
class RequestDataKey(...) : ResultNavigationKey<Boolean>()

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

#### How do I do Master/Detail navigation 
Enro has a built in component for this.  If you want to build something more complex than what the built-in component provides, you'll be able to use the built-in component as a reference/starting point, as it is built purely on Enro's public API

#### How do I handle multiple backstacks on each page of a BottomNavigationView? 
Enro has a built in component for this. If you want to build something more complex than what the built-in component provides, you'll be able to use the built-in component as a reference/starting point, as it is built purely on Enro's public API

#### I'd like to do shared element transitions, or do something special when navigating between certain screens
Enro allows you to define "NavigationExecutors" as overrides for the default behaviour, which handle these situations. 

There will be an example project that shows how this all works in the future, but for now, here's a basic explanation: 
1. A NavigationExecutor is typed for a "From", an "Opens", and a NavigationKey type. 
2. Enro performs navigation on a "NavigationContext", which is basically either a Fragment or a FragmentActivity
3. A NavigationExecutor defines two methods
    * `open`, which takes a NavigationContext of the "From" type, a Navigator for the "Opens" type, and a NavigationInstruction (i.e. the From context is attempting to open the Navigator with the input NavigationInstruction)
    * `close`, which takes a NavigationContext of the "Opens" type (i.e. you're closing what you've already opened)
4. By creating a NavigationExecutor between two specific screens and registering this with the NavigationController, you're able to override the default navigation behaviour (although you're still able to call back to the DefaultActivityExecutor or DefaultFragmentExecutor if you need to)
5. See the methods in NavigationControllerBuilder for activityToActivityOverride, activityToFragmentOverride and fragmentToFragmentOverride
6. When a NavigationContext decides what NavigationExecutor to execute an instruction on, Enro will look at the NavigationContext originating the NavigationInstruction and then walk up toward's it's root NavigationContext (i.e. a Fragment will check itself, then its parent Fragment, and then that parent Fragment's Activity), checking for an appropriate override along the way. If it finds no override, the default will be used. NavigationContexts that are the children of the current NavigationContext will not be searched, only the parents. 

Example: 
```kotlin
// This override will place the "DetailFragment" into the container R.id.detail, 
// and when it's closed, will set whatever Fragment is in the R.id.master container as the primary navigation fragment
activityToFragmentOverride<MasterDetailActivity, DetailFragment>(
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

#### My Activity crashes on launch, what's going on?!
It's possible for an Activity to be launched from multiple places. Most of these can be controlled by Enro, but some of them cannot. For example, an Activity that's declared in the manifest as a MAIN/LAUNCHER Activity might be launched by the Android operating system when the user opens your application for the first time. Because Enro hasn't launched the Activity, it's not going to know what the NavigationKey for that Activity is, and won't be able to read it from the Activity's intent. 

Luckily, there's an easy solution! When you declare an Activty or Fragment as a NavigationDestination, and the NavigationKey for that Activity or Fragment has a no-args constructor available, you can add "allowDefault = true" to the NavigationDestination arguments, and the no-args constructor will be used to construct a NavigationKey for that Activity/Fragment if there isn't one in the intent extras/fragment arguments.

Example: 
```kotlin
@Parcelize
class MainKey(someOptionalArgument: Boolean = false) : NavigationKey

@NavigationDestination(MainKey::class, allowDefault = true)
class MainActivity : AppCompatActivity() {}
```

## Why would I want to use Enro? 
#### Support the navigation requirements of large multi-module Applications, while allowing flexibility to define rich transitions between specific destinations

A multi-module application has different requirements to a single-module application. Individual modules will define Activities and Fragments, and other modules will want to navigate to these Activities and Fragments. By detatching the NavigationKeys from the destinations themselves, this allows NavigationKeys to be defined in a common/shared module which all other modules depend on.  Any module is then able to navigate to another by using one of the NavigationKeys, without knowing about the Activity or Fragment that it is going to. FeatureOneActivity and FeatureTwoActivity don't know about each other, but they both know that FeatureOneKey and FeatureTwoKey exist. A simple version of this solution can be created in less than 20 lines of code.  

However, truly beautiful navigation requires knowledge of both the originator and the destination. Material design's shared element transitions are an example of this. If FeatureOneActivity and FeatureTwoActivity don't know about each other, how can they collaborate on a shared element transition? Enro allows transitions between two navigation destinations to be overridden for that specific case, meaning that FeatureOneActivity and FeatureTwoActivity might know nothing about each other, but the application that uses them will be able to define a navigation override that adds shared element transitions between the two.

#### Allow navigation to be triggered at the ViewModel layer of an Application


