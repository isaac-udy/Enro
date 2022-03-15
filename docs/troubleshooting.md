# Troubleshooting
## Enro Exceptions
### `InvalidLifecycleState`

### `NoAttachedNavigationHandle`
There are two primary ways to get a NavigationHandle in Enro. The first one is through the `by navigationHandle<Type>()` property delegate, which is used to create and configure a NavigationHandle that is aware of it's type. The second is through `getNavigationHandle()`, which returns an untyped NavigationHandle. This exception is thrown by `getNavigationHandle()`, as well as internally in Enro when a NavigationHandle is needed to perform some action, but the type of the NavigationHandle is unimportant (for example, when using `by registerForNavigationResult`).

This exception is thrown when Enro attempts to read a NavigationHandle from a NavigationContext (an Activity, Fragment or Composable) which does not have a NavigationHandle already attached. In normal operation, the NavigationController installed in your application's Application class will be watching Activity/Fragment/Composable lifecycle events, and attaching a NavigationHandle to these as required. If you are seeing this exception, that means that this hasn't happened. 

The most likely cause of this exception is that you have not configured your Application to use Enro correctly. It is important to make sure that your Application class is correctly implements `NavigationApplication` (see [here](https://github.com/isaac-udy/Enro#3-annotate-your-application-as-a-navigationcomponent-and-implement-the-navigationapplication-interface)). Make sure that you *instantiate the Application's NavigationController when the Application is instantiated*. If you lazily initialise the Application's NavigationController, you can create a situation where the NavigationController hasn't been attached to the Application's lifecycle in time to watch the lifecycle events of your screens and create the NavigationHandles. 

#### This Exception is occurring in tests
This exception can also occur in tests, if you have not correctly set your test up to use Enro. This can occur if you are either missing an `EnroTestRule` in your class, or you are creating an Activity/Fragment/Composable before the `EnroTestRule` has been configured.

For example, it is common to use an `ActivityTestRule` or `ActivityScenarioRule` in tests, which will create an Activity for your tests to run against. Adding an `EnroTestRule` to a test that uses either of these test rules requires setting an `order` on the `@Rule` annotation, to make sure that **the `EnroTestRule` is initialised before the Activity/Fragment/Composable is launched**. 

Examples: 
1. (Incorrect) In this example, neither of the `@Rule`s have an order, so it is likely that the `EnroTestRule` will be initialised **after** the Activity has been launched, causing a `NoAttachedNavigationHandle` exception. 
```kotlin
class ExampleTest {
    @get:Rule
    val activity = ActivityTestRule(ExampleActivity::class.java)
    
    @get:Rule
    val enroRule = EnroTestRule()
}
```

2. (Correct) In this example, both of the `@Rule`s have an order, ensuring that the EnroTestRule will be active when the ActivityTestRule launches the Activity. 
```kotlin
class ExampleTest {
    @get:Rule(order = 0)
    val enroRule = EnroTestRule()

    @get:Rule(order = 1)
    val activity = ActivityTestRule(ExampleActivity::class.java)
}
```

3. (Correct) In this example, the `EnroTestRule` does not have an order, but this does not matter, as an Activity is only launched during the `exampleTest` test method, meaning that the `EnroTestRule` will be initialised before the Activity is created. 
```kotlin
class ExampleTest {
    @get:Rule
    val enroRule = EnroTestRule()
    
    @Test
    fun exampleTest() {
        val scenario = ActivityScenario.launch(ExampleActivity::class.java)
    }
}
```

### `CouldNotCreateEnroViewModel`

### `MissingNavigator`

### `IncorrectlyTypedNavigationHandle`

### `InvalidViewForNavigationHandle`

### `DestinationIsNotDialogDestination`

### `EnroResultIsNotInstalled`

### `ReceivedIncorrectlyTypedResult`

### `NavigationControllerIsNotAttached`

### `UnreachableState`
This exception exists to mark when Enro does not expect this state could be reached, due to higher-level logic (for example, if an unchecked cast is always expected to succeed due to reflection based type checking). If you are seeing this Exception, this means that Enro's assumptions are incorrect. This could mean that you have reached a state that should actually throw a more descriptive Exception, or it means that you have reached a state that should be considered valid. In either case, this means that Enro needs to be changed to accomodate for this. 

Please open an issue [here](https://github.com/isaac-udy/Enro/issues), and make sure to include your stacktrace.