# Enro 🗺️
 🚧  Work in progress 🚧  

A simple navigation library for Android 

*"The novices’ eyes followed the wriggling path up from the well as it swept a great meandering arc around the hillside. Its stones were green with moss and beset with weeds. Where the path disappeared through the gate they noticed that it joined a second track of bare earth, where the grass appeared to have been trampled so often that it ceased to grow. The dusty track ran straight from the gate to the well, marred only by a fresh set of sandal-prints that went down, and then up, and ended at the feet of the young monk who had fetched their water."*
    *- [The Garden Path](http://thecodelesscode.com/case/156)*

## Features

- Navigate between Fragments or Activities seamlessly, without worrying about the implementation of your destination

- Remember where you've been, across configuration changes and process death

- Describe your navigation destinations through a simple DSL

- Easily override default behavior to enable beautiful transitions between specific destinations

- Migrate to Enro screen-by-screen while maintaining full compatibility with screens that live outside of Enro's navigation graph

- Execute navigation actions from your ViewModels (but only if you want to!)

  

## Using Enro

0. Gradle: 

```gradle
// TODO
```



1. Install Enro into your Application 

```kotlin
class YourApplication : Application(), NavigationApplication {
    override val navigationController =  // See Step 3    
    override fun onCreate() {
        super.onCreate()
        NavigationController.install(this)
    }
    // ...
}
```



2. Define some NavigationKeys
```kotlin
@Parcelize
data class MyActvityKey(val activityData: String): NavigationKey
class MyActivity : AppCompatActivity() { ... }

@Parcelize
data class MyFragmentKey(val fragmentData: String): NavigationKey
class MyFragment : Fragment() { ... }
```



3. In your application, define Navigators for your NavigationKeys
```kotlin
class YourApplication : Application(), NavigationApplication {
	override val navigationController = NavigationController(
		navigators = listOf(
			activityNavigator<MyActvityKey, MyActivity>(),
			fragmentNavigator<MyFragmentKey, MyFragment>()
		)
	)
	// ...
```



4. Interact with your Navigatiors! 
```kotlin
@Parcelize
data class MyActvityKey(val userId: String): NavigationKey
class MyActivity : AppCompatActivity() { 
	val navigation by navigationHandle<MyActvityKey>()
	// ... 
	fun updateViews() {
		myTextView.setText(navigation.key.activityData)
	}
	
	fun onButtonPressed() {
		navigation.forward(MyFragmentKey("Wow!"))
	}
}

@Parcelize
data class MyFragmentKey(val userId: String): NavigationKey
class MyFragment : Fragment() { 
	val navigation by navigationHandle<MyFragmentKey>()
	// ... 
	fun updateViews() {
		myTextView.setText(navigation.key.fragmentData)
	}
	
	fun onButtonPressed() {
		navigation.forward(MyActivityKey("Wow!"))
	}
}
```



## FAQ

#### What kind of navigation instructions does Enro support?
Enro  supports three kinds of navigation instructions: `forward`, `replace` and `replaceRoot`

`forward` moves to another destination, but leaves the current destination on the navigation stack. If the navigation stack is `A -> B -> C ->` and we execute the action `C.forward(D)` the navigation stack would become `A -> B -> C -> D ->`

`replace` moves to another destination, and replaces the current destination on the navigation stack. If the navigation stack is `A -> B -> C ->` and we execute the action `C.replace(D)` the navigation stack would become `A -> B -> D ->`

`replaceRoot` moves to another destination, and treats the new destination as the root of the entire navigation stack. If the navigation stack is `A -> B -> C ->` and we execute the action `C.replaceRoot(D)` the navigation stack would become `D ->`

#### How does Enro support Activities navigating to Fragments? 
When an Activity executes a navigation instruction that resolves to a Fragment, one of two things will happen: 
1. The Activity's navigator defines a "fragment host" that accepts the Fragment's type, in which case, the Fragment will be opened into the container view defined by that fragment host.
2. The Activity's navigation **does not** define a fragment host that acccepts the Fragment's type, in which case, the Fragment will be opened as if it was an Activity. 

#### My Activity crashes on launch, what's going on?!
It's possible for an Activity to be launched from multiple places. Most of these can be controlled by Enro, but some of them cannot. For example, an Activity that's declared in the manifest as a MAIN/LAUNCHER Activity might be launched by the Android operating system when the user opens your application for the first time. Because Enro hasn't launched the Activity, it's not going to know what the NavigationKey for that Activity is, and won't be able to read it from the Activity's intent. 

Luckily, there's an easy solution! When you declare a navigator for your Activity, provide a "defaultKey" if you expect that Activity to be launched from somewhere outside of Enro's control. 

Example: 
```kotlin
class YourApplication : Application(), NavigationApplication {
	override val navigationController = NavigationController(
		navigators = listOf(
			activityNavigator<MyActvityKey, MyActivity> {
                defaultKey(MyActivityKey("Direct from the launcher!"))
			},
			fragmentNavigator<MyFragmentKey, MyFragment>(),
			// ...
		)
	)
	// ...
```

#### Why would I want to use Enro? 
Enro was written with a few specific goals:

##### Support the navigation requirements of large multi-module Applications, while allowing flexibility to define rich transitions between specific destinations
<details>
  <summary>More info...</summary>

A multi-module application has different requirements to a single-module application. 	Individual modules will define Activities and Fragments, and other modules will want to navigate to these Activities and Fragments. By detatching the NavigationKeys from the destinations themselves, this allows NavigationKeys to be defined in a common/shared module which all other modules depend on.  Any module is then able to navigate to another by using one of the NavigationKeys, without knowing about the Activity or Fragment that it is going to. FeatureOneActivity and FeatureTwoActivity don't know about each other, but they both know that FeatureOneKey and FeatureTwoKey exist. A simple version of this solution can be created in less than 20 lines of code.  

However, truly beautiful navigation requires knowledge of both the originator and the destination. Material design's shared element transitions are an example of this. If FeatureOneActivity and FeatureTwoActivity don't know about each other, how can they collaborate on a shared element transition? Enro allows transitions between two navigation destinations to be overridden for that specific case, meaning that FeatureOneActivity and FeatureTwoActivity might know nothing about each other, but the application that uses them will be able to define a navigation override that adds shared element transitions between the two.
</details>

##### Allow navigation to be triggered at the ViewModel layer of an Application, thus allowing the details of navigation to be tested in unit tests. 


