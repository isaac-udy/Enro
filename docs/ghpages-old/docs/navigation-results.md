# Navigation Results
The ability for a NavigationKey to define a result type is an important feature of Enro. It allows you to define a rich contract for a screen, where the contract represents not just an input type, but also an output type. This allows screens within an application to be more independent of one another and helps screens be as re-usable as possible. 

Making use of this feature is very simple, and is done by defining a NavigationKey that implements `NavigationKey.SupportsPush.WithResult` or `NavigationKey.SupportsPresent.WithResult`. For more information on defining NavigationKeys, please see the [NavigationKeys documentation](./navigation-keys.md).

## Defining a `NavigationKey.WithResult`
Any NavigationKey can define a result type by implementing the `...WithResult<T>` interface, where `T` is the type of the result. For example, a NavigationKey that returns a `LocalDate` might look like this:

```kotlin

@Parcelize
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.SupportsPresent.WithResult<LocalDate>

```

For more information on defining NavigationKeys, please see the [NavigationKeys documentation](./navigation-keys.md).

## Receiving results
Receiving a result from a NavigationKey works in a similar way to Android's `registerForActivityResult` functionality. To receive a result, you'll need to create a `NavigationResultChannel` using the `registerForNavigationResult` function. This function takes a lambda that will be invoked when a result is received. The lambda will be invoked with the result value.

### Defining a `NavigationResultChannel`
A `NavigationResultChannel` can be defined in any Activity, Fragment, Composable or Enro-supported ViewModel. The syntax for defining a `NavigationResultChannel` is slightly different depending on the type of screen you're in, but always uses a function called `registerForNavigationResult`.

`registerForNavigationResult` takes the following arguments:
1. (Required) A generic type argument, which represents the type of the result that will be received.
2. (Required) An `onResult` lambda, which will be invoked when a result is received, which receives a single argument of the generic type provided to `registerForNavigationResult`.
3. (Optional) An `onClosed` lambda, which will be invoked when if a screen opened using the result channel is closed without a result being sent.

When you have created a result channel using `registerForNavigationResult<T>`, the result channel can be used to `push` or `present` any NavigationKey that implements `NavigationKey.SupportsPush.WithResult<T>` or `NavigationKey.SupportsPresent.WithResult<T>`. The result channel will then receive the result from the destination, and invoke the `onResult` lambda that was provided when creating the result channel. The `T` type of the `...WithResult<T>` NavigationKey must match the `T` used when creating the result channel. 

If there are multiple result channels in the same screen, registered for the same `T`, they can all be used to receive results, and the result channel that is used to `push` or `present` will be the one that receives the result (this works safely across configuration change and process death). 

#### Activities/Fragments/ViewModels
In an Activity, Fragment or ViewModel, the `registerForNavigationResult` function should be used as follows:

#### Composables
In a Composable, the `registerForNavigationResult` function should be used as follows:

```kotlin
@Composable
fun ExampleComposable() {
    val exampleResultChannel = registerForNavigationResult<ExampleResult> { result: ExampleResult ->
        // handle result
    }
    
    Button(
        onClick = { exampleResultChannel.present(ExampleResultNavigationKey()) }
    ) {
        Text("Launch Example Result")
    }
}
```

```kotlin
class MyActivityFragmentOrViewModel : ... {
    private val exampleResultChannel by registerForNavigationResult<ExampleResult> { result: ExampleResult ->
        // handle result
    }
    
    fun launchExampleResult() {
        exampleResultChannel.present(ExampleResultNavigationKey())
    }
}

```

## Sending results
From a screen that is bound to a `...WithResult<T>` NavigationKey, you can send a result by calling the `closeWithResult` function on the NavigationHandle. This function takes a single argument, which is the result value. The type of the result value must match the type of the `...WithResult<T>` NavigationKey. The `closeWithResult<T>` function is only available for `TypedNavigationHandle<K>`, where `K` is a NavigationKey `...WithResult<T>`, which have been created through the `navigationHandle<K>()` function. For more information on NavigationHandles, please see the [NavigationHandles documentation](./navigation-handles.md).

Essentially, when you want to send a result, make sure you use the typed version of the `navigationHandle` functions, and pass in the type of the NavigationKey `...WithResult<T>` that you want to send a result for, and then the `closeWithResult<T>` will be available.

It is also possible to delegate a result to another screen, rather than return the result yourself. For more information on delegating results, please see the [Delegating results](#delegating-results) section.

#### Composables
In a Composable, the `closeWithResult` function should be used as follows:

```kotlin
@Parcelize
class ExampleResultKey: NavigationKey.SupportsPush.WithResult<ExampleResult>

@Composable
fun ExampleComposable() {
    val navigationHandle = navigationHandle<ExampleResultKey>()
    // private val navigationHandle = navigationHandle() <- this won't work, as it returns an untyped NavigationHandle

    Button(
        onClick = { navigationHandle.closeWithResult(ExampleResult(...)) }
    ) {
        Text("Close with result")
    }
}
```

#### Activities/Fragments/ViewModels
In an Activity, Fragment or ViewModel, the `closeWithResult` function should be used as follows:

```kotlin
@Parcelize
class ExampleResultKey: NavigationKey.SupportsPush.WithResult<ExampleResult>

class ExampleFragmentActivityOrViewModel : ... {
    private val navigationHandle by navigationHandle<ExampleResultKey>()
    // private val navigationHandle by navigationHandle() <- this won't work, as it returns an untyped NavigationHandle
    
    fun sendResult() {
        navigationHandle.closeWithResult(ExampleResult(...))
    }
}
```

## Advanced
### Delegating results
Enro provides support for delegating results to another screen. This functionality can be used to build small, multi-step flows that are made up of multiple screens. For example, a flow that requires the user to select a date, and then select a time, could be built using two screens, where the first screen delegates to the second screen. These kinds of flows are often referred to as "embedded navigation flows", as the logic for the flow is embedded within each screen within the flow. When the screen which is delegated to returns a result, the screen that delegated to it will be closed at the same time, and the result will be delivered to the original screen that requested the result.

For example: 
1. Starting on "Screen A" where the backstack is `... -> A`
2. Screen A requests a result from Screen B, the backstack becomes `... -> A -> B`
3. Screen B delegates its result to Screen C, the backstack becomes `... -> A -> B -> C`
4. There are several possible outcomes:
  a. Screen C returns a result, which would cause the result to be delivered to Screen A, and the backstack would become `... -> A`
  b. Screen C closes without a result, which would cause no result to be delivered, but Screen C to close, and the backstack would become `... A -> B`  
  c. Screen C continues the by delegating to Screen D, the backstack would become `... -> A -> B -> C -> D`, and the result behaviour would be the same when Screen D is closed; the result would be delivered to Screen A, and the backstack would become `... -> A`
     

To delegate a result, you'll need to use the `deliverResultFromPush` or `deliverResultFromPresent` functions on the `NavigationHandle`. This function takes a single argument, which is the `NavigationKey` that you want to delegate to. The `NavigationKey` that you delegate to must be a `...WithResult<T>` NavigationKey, where `T` is the same type as the `...WithResult<T>` NavigationKey that you are delegating from.
