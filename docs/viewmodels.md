# ViewModels

Enro allows ViewModels to access the `NavigationHandle` for the screen that they are being used by, which allows navigation logic to be managed from within a ViewModel. This is functionality is optional, and some people prefer to leave the navigation logic within the View layer of their applications.

## Getting a NavigationHandle inside a ViewModel
To get a NavigationHandle from inside of a ViewModel, use the `by navigationHandle<T>()` property delegate. This will return a `TypedNavigationHandle<T>`. For more information on what a NavigationHandle is, see [Navigation Handles](./navigation-handles.md).

```kotlin
@Parcelize
class ExampleNavigationKey(
    val exampleArgument: String
) : NavigationKey.SupportsPush

class ExampleViewModel : ViewModel() {

```
{:.code-not-important .code-start}
```kotlin
    private val navigation by navigationHandle<ExampleNavigationKey>()
```
{:.code-important}
```kotlin

}
```
{:.code-not-important .code-end}

With a NavigationHandle inside of your ViewModel, you are able to perform all the regular functions that are available on a NavigationHandle. 

```kotlin
@Parcelize
class ExampleNavigationKey(
    val exampleArgument: String
) : NavigationKey.SupportsPush

class ExampleViewModel : ViewModel() {

    private val navigation by navigationHandle<ExampleNavigationKey>()
```
{:.code-not-important .code-start}
```kotlin
    fun goToNextScreen() {
        val argument = navigation.key.exampleArgument
        val nextScreenKey = NextScreenKey(nextScreenArgument = argument.hashCode())
        navigation.push(nextScreenKey)
    }
```
{:.code-important}
```kotlin

}
```
{:.code-not-important .code-end}

## Handle Results
ViewModels are also able to create result channels, and manage results. To create a NavigationResultChannel, use the `by registerForNavigationResult<T>()` property delegate. This will return a NavigationResultChannel that will handle results of type `T`. For more information of NavigationResultChannels, see [Navigation Results](./navigation-results.md).

```kotlin
@Parcelize
class ExampleViewModel : ViewModel() {

    private val navigation by navigationHandle<ExampleNavigationKey>()
```
{:.code-not-important .code-start}
```kotlin
    private val stringResultChannel by registerForNavigationResult<String> { result: String ->
        // ...
    }
```
{:.code-important}
```kotlin

    fun onRequestString() {
        stringResultChannel.present(RequestStringKey())
    }
}
```
{:.code-not-important .code-end}

## Creating navigation aware ViewModels
To ensure that a NavigationHandle is available for `by navigationHandle<T>()`, the `ViewModelProvider.Factory` that is used to create the ViewModel must be made aware of the local NavigationHandle. Exactly how this works depends on whether the navigation destination is an Activity/Fragment/Composable. 

### Composables
By default, all Composable navigation destinations (i.e. Composables annotated with NavigationDestination) already have a ViewModel Factory that is aware of the NavigationHandle. This means that ViewModels created using the standard Composable `viewModel<T>()` function should be able to use `by navigationHandle<T>()`, as long as a custom ViewModelProvider.Factory is not passed as a parameter to the `viewModel<T>()` function. 

If you are passing a custom ViewModelProvider.Factory to this function, you will need to bind the NavigationHandle to the factory using `withNavigationHandle`.

```kotlin
@Composable
@NavigationDestination(ExampleNavigationKey::class)
fun ViewModelExample() {
    // No factory provided:
    val firstViewModel = viewModel<FirstViewModel>()

    // Custom factory, using `withNavigationHandle()`:
    val secondViewModel = viewModel<SecondViewModel>(
        factory = CustomViewModelFactory().withNavigationHandle()
    )
}
```

If you use the same custom ViewModelProvider.Factory for all screens in your application, you may want to globally set the ViewModel Factory for all Composable navigation destinations, and ensure that this globally set factory also provides a NavigationHandle.

To do this, you can configure the `composeEnvironment` in the `createNavigationController` block of your Application class. The `composeEnvironment` configuration allows you to provide a common rendering environment for all Composable destinations, and is useful to globally set a theme, or provide screen-specific CompositionLocals.  

Here is an example of a `composeEnvironment` block which will wrap the LocalViewModelStoreOwner with a special ViewModelStoreOwner that implements HasDefaultViewModelProviderFactory, and provides a custom ViewModelProvider.Factory. This will allow any screen to use the Composable `viewModel<T>()` function without needing to specify a custom ViewModelProvider.Factory, and which will allow these ViewModels to access a NavigationHandle.  

```kotlin
@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        composeEnvironment { content ->
            ProvideCustomViewModelFactory(content)
        }
    }
}

@Composable
fun ProvideCustomViewModelFactory(content: @Composable () -> Unit) {
    val navigation = navigationHandle()
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val wrappedViewModelStoreOwner = remember(navigation, viewModelStoreOwner) {
        WrappedViewModelStore(
            wrapped = requireNotNull(viewModelStoreOwner),
            factory = CustomViewModelFactory().withNavigationHandle(navigation)
        )
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides wrappedViewModelStoreOwner
    ) {
        content()
    }
}

class WrappedViewModelStore(
    val wrapped: ViewModelStoreOwner,
    val factory: ViewModelProvider.Factory
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    override val viewModelStore: ViewModelStore
        get() = wrapped.viewModelStore

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = factory

    override val defaultViewModelCreationExtras: CreationExtras
        get() = when (wrapped) {
            is HasDefaultViewModelProviderFactory -> wrapped.defaultViewModelCreationExtras
            else -> super.defaultViewModelCreationExtras
        }
}
```

Enro provides a `ProvideViewModelFactory` utility method which can be used to achieve the same as the code above.

```kotlin
@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        composeEnvironment { content ->
            ProvideViewModelFactory(
                factory = CustomViewModelFactory(),
                content = content
            )
        }
    }
}
```

#### Hilt
If you are using Hilt, and the Activity hosting your Composable destinations is an AndroidEntryPoint, you should not need to provide a custom factory at all, and the Composable `viewModel<T>()` function should use the Hilt ViewModelProvider.Factory. 

### Activities and Fragments
From an Activity or Fragment, you have two options for making sure that a ViewModel has a NavigationHandle available: 
1. Use `by enroViewModels<T>()` instead of `by viewModels<T>()`
```kotlin
class ExampleActivity : AppCompatActivity() {
    private val firstViewModel by enroViewModels<FirstViewModel>()

    private val secondViewModel by enroViewModels<SecondViewModel>(
        factoryProducer = { CustomViewModelFactory() }
    )
} 
```

2. Use `withNavigationHandle()` to bind a NavigationHandle to an existing ViewModelProvider.Factory
```kotlin
class ExampleActivity : AppCompatActivity() {
    
    private val navigation by navigationHandle()
    private val firstViewModel by viewModels<FirstViewModel>(
        factoryProducer = { 
            CustomViewModelFactory().withNavigationHandle(navigation)
        }
    )
}
```
