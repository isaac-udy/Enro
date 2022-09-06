# Troubleshooting
## Enro Exceptions
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
This exception is thrown by the `EnroViewModelFactory`, which is used when using `by enroViewModels<ViewModel>()` to create a ViewModel that has access to a NavigationHandle. `by enroViewModels` does not actually create the ViewModel itself, but rather it sets up some state and then delegates the ViewModel creation to another `ViewModelProvider.Factory`. You can pass your own `ViewModelProvider.Factory` to `by enroViewModels` if you need to use a custom ViewModel factory, but this will default to the `defaultViewModelProviderFactory` of a Fragment or Activity if you do not provide your own `ViewModelProvider.Factory`. 

If you are getting this exception, check that the `defaultViewModelProviderFactory` can actually create the ViewModel type you are requesting. If you use a custom `ViewModelProvider.Factory`, make sure that you are providing this to `by enroViewModels`, or calling `withNavigationHandle()` if you are using `by viewModels()`. 

#### This occurs in a project that uses Hilt and `@HiltViewModel`
This exception can occur in projects that use Hilt, and you are attempting to create an `@HiltViewModel` annotated ViewModel, but are not requesting the ViewModel from inside an `@AndroidEntryPoint` annotated Activity/Fragment. If a Fragment or Activity is not marked as `@AndroidEntryPoint`, Hilt will not set the `defaultViewModelProviderFactory` to the Hilt ViewModel factory, and the `defaultViewModelProviderFactory` will be a `SavedStateViewModelFactory`, which won't be able to construct your `@HiltViewModel` factory.

#### Examples
```kotlin
@Parcelize class ExampleKey : NavigationKey

class ExampleViewModel(val customArgument: Int) : ViewModel() {
    val navigation by navigationHandle<ExampleKey>()
}
class ExampleViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ExampleViewModel(customArgument = 1337) as T
    }
}

@NavigationDestination(ExampleKey::class)
class ExampleFragment : Fragment {
    
    // This statement will fail, because the Fragment's defaultViewModelProviderFactory will be a 
    // SavedStateViewModelFactory(), which doesn't know how to construct an ExampleViewModel, because
    // ExampleViewModel requires an Int parameter called "customArgument".
    val failingViewModel by enroViewModels<ExampleViewModel>()

    // This statement will succeed, because we are passing through the special "ExampleViewModelFactory"
    // factory which does know how to create an ExampleViewModel
    val successfulViewModel by enroViewModels<ExampleViewModel> { ExampleViewModelFactory() }
}

```

### `ViewModelCouldNotGetNavigationHandle`
This exception will occur if you attempt to create a ViewModel that uses `by navigationHandle()`, but the ViewModel does not have access to a NavigationHandle during it's initialisation. This can be solved in a few different ways: 

1. Activities and Fragments:
```kotlin
@NavigationDestination(MyNavigationKey::class)
class ActivityOrFragment : Activity /*or Fragment*/ {
    /**
     * viewModelFromDefault and viewModelFromCustom below show how to use `ViewModelProvider.Factory.withNavigationHandle()`
     * to bind a NavigationHandle to a ViewModelProvider through a normal call to `by viewModels()`
     */
    val viewModelFromDefault by viewModels<MyViewModel>(
        factoryProducer = {
            defaultViewModelProviderFactory.withNavigationHandle(getNavigationHandle())
        }
    )

    val viewModelFromCustom by viewModels<MyViewModel>(
        factoryProducer = {
            MyCustomViewModelFactory(/* ... */).withNavigationHandle(getNavigationHandle())
        }
    )

    /**
     * enroViewModelFromDefault and enroViewModelFromCustom below show how to use `by enroViewModels()` to create a ViewModel that
     * requests a NavigationHandle. `by enroViewModels()` essentially takes care of the `.withNavigationHandle()` call for you.
     */
    val enroViewModelFromDefault by enroViewModels<MyViewModel>()

    val enroViewModelFromCustom by enroViewModels<MyViewModel>(
        factoryProducer = { MyCustomViewModelFactory(/* ... */) }
    )
}
```

2. Composables
```kotlin
@Composable
@NavigationDestination(MyNavigationKey::class)
fun MyEnroComposable() {
 
    /**
     * In a @Composable function that is also marked as @NavigationDestination
     * any call to `= viewModel()` will by default use a ViewModelProvider.Factory that 
     * has a the `LocalNavigationHandle.current` bound
     */
    val viewModelFromDefault = viewModel<MyViewModel>()
    
    /**
     * If you need to provide a custom ViewModelProvider.Factory, you can use the `.withNavigationHandle()` 
     * function which as shown in the Activity/Fragment examples above. In a @Composable function, 
     * passing the `navigationHandle =` argument is optional, as this will use the `LocalNavigationHandle.current` 
     * unless you explicitly provide the argument. 
     */
    val viewModelFromCustomer = viewModel<MyViewModel>(
        factory = MyCustomViewModelFactory(/* ... */).withNavigationHandle()
    )
    
}
```

#### This Exception is occurring in tests
This exception will occur in tests if you are attempting to create a ViewModel to test, but have not used `putNavigationHandleForViewModel` from the `enro-test` library.

### `MissingNavigator`
This exception can occur when you attempt to navigate to a `NavigationKey` that has not been bound to an Activity/Fragment/Composable, if you have forgotten to add the required `kapt` dependencies to make sure that Enro's code generation runs, or if code generation has not updated correctly when you have added a new destination.

1. Make sure you have the correct `kapt` dependency on `enro-processor`
2. Make sure you've annotated your Activity/Fragment/Composable/SyntheticDestination with `@NavigationDestination` pointing to the correct NavigationKey
3. Clean the project (in case the incremental annotation processor has had an issue)
4. Make sure that your app module has a dependency on all modules that contain `@NavigationDestination` classes. The app module needs to be able to "see" all the `@NavigationDestination` classes that exist, and will not pick these up through transient dependencies. 

#### This Exception is occurring in tests

### `IncorrectlyTypedNavigationHandle`

### `InvalidViewForNavigationHandle`

### `DestinationIsNotDialogDestination`

### `EnroResultIsNotInstalled`

### `ResultChannelIsNotInitialised`

### `ReceivedIncorrectlyTypedResult`

### `NavigationControllerIsNotAttached`

### `UnreachableState`
This exception exists to mark when Enro does not expect this state could be reached, due to higher-level logic (for example, if an unchecked cast is always expected to succeed due to reflection based type checking). If you are seeing this Exception, this means that Enro's assumptions are incorrect. This could mean that you have reached a state that should actually throw a more descriptive Exception, or it means that you have reached a state that should be considered valid. In either case, this means that Enro needs to be changed to accomodate for this. 

Please open an issue [here](https://github.com/isaac-udy/Enro/issues), and make sure to include your stacktrace.
