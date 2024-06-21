# Testing

## ViewModel Testing
When using Enro in ViewModels, you can test the navigation logic of your application without needing to run an emulator or device. This is one of the primary reasons why Enro supports using NavigationHandles inside ViewModels.

The following documentation will provide information on how to test ViewModels that use `by navigationHandle`, `by registerForNavigationResult`, or `by registerForFlowResult`. ViewModels that use Enro functionality will be referred to as "Enro ViewModels".

### Dependencies
Before attempting to test ViewModels that use Enro, you will need to make sure that you have a dependency on `enro-test`. 

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("dev.enro:enro-test:<version>")
}
```

### EnroTestRule and runEnroTest
When testing ViewModels that use Enro, you will need to use `EnroTestRule` or `runEnroTest` to provide the necessary context for the test to function correctly. 

If you are using JUnit4, you can use `EnroTestRule` in your test class: 
```kotlin
class ExampleViewModelTest {
    @get:Rule
    val enroTestRule = EnroTestRule()

    @Test
    fun testViewModel() {
        // Test your ViewModel here
    }
}
```

Alternatively, if you are not using JUnit4, or you would prefer not to use a TestRule, you can wrap your test function with `runEnroTest`:
```kotlin
class ExampleViewModelTest {
    @Test
    fun testViewModel() = runEnroTest {
        // Test your ViewModel here
    }
}
```

### Preparing a ViewModel for testing
Once you have configured your tests with `EnroTestRule` or `runEnroTest`, you can create a ViewModel for testing. Constructing an Enro ViewModel without performing some initial configuration will result in the ViewModel construction failing with a `EnroException.ViewModelCouldNotGetNavigationHandle` exception. Even though you may have configured your test with `EnroTestRule` or `runEnroTest`, you still need to configure the `NavigationHandle` (and it's `NavigationKey`) that the ViewModel will use.

To prepare a NavigationHandle for an Enro ViewModel, use the `putNavigationHandleForViewModel<T>` function. This function takes a `NavigationKey` as an argument, which will be the NavigationKey that you want to associate with the NavigationHandle for the ViewModel. The generic type `<T>` of `putNavigationHandleForViewModel<T>` should be the ViewModel class that is being prepared for testing. After calling `putNavigationHandleForViewModel<T>`, the next ViewModel of type `T` that is constructed will have a `TestNavigationHandle` provided to it, with the NavigationKey that was passed as the argument to `putNavigationHandleForViewModel<T>`. 

`putNavigationHandleForViewModel<T>` returns a TestNavigationHandle, which will be the same NavigationHandle that is provided to the next ViewModel `T` that is constructed. A TestNavigationHandle is a special type of NavigationHandle that provides useful testing related functionality. 

Here's an example of a simple NavigationKey, Enro ViewModel, and an associated ViewModel test class:
```kotlin
// Production code:
@Parcelize
data class ExampleNavigationKey(val arg: String) : NavigationKey.SupportsPush

class ExampleViewModel : ViewModel() {
    private val navigation by navigationHandle<ExampleNavigationKey>()
}

// Test code:
class ExampleViewModelTest {
    @Test
    fun testViewModel() = runEnroTest {
        val navigationKey = ExampleNavigationKey("test")
        val testNavigationHandle = putNavigationHandleForViewModel<ExampleViewModel>(navigationKey)
        val viewModel = ExampleViewModel()
        // ...
    }
}
```

<details markdown="block">
  <summary class="faq-summary">
    Notes
  </summary>

* If a ViewModel uses `by navigationHandle<ExampleType>` and you call `putNavigationHandleForViewModel(...)` with a NavigationKey that is not `ExampleType`, the ViewModel will throw an exception during construction

</details>

### Using a TestNavigationHandle
Once you have prepared an Enro ViewModel for testing using `putNavigationHandleForViewModel`, you can use the `TestNavigationHandle` returned by that function to perform assertions on the navigation actions performed by the ViewModel. 

The following is a code sample of the `assert/expect` functions that are available on a `TestNavigationHandle`:
```kotlin
@Test
fun testViewModel() = runEnroTest {
    val testNavigationHandle = putNavigationHandleForViewModel<ExampleViewModel>(ExampleNavigationKey("test"))
    val viewModel = ExampleViewModel()
    
    viewModel.doSomething() // trigger an action

    // asserts that the last navigation action was to open a destination with "TestNavigationKey" as the key
    // if the assertion is successful, the function will return the TestNavigationKey that was opened,
    // which can be used to perform further assertions
    val assertOpenedExample = testNavigationHandle.assertOpened<TestNavigationKey>()
    assertOpenedExample("test", assertOpenedExample.parameter)
        
    // asserts that the last navigation action was to open a destination with "TestNavigationKey" as the key,
    // and ensures that the NavigationInstruction was a Push, rather than a Present
    val assertPresentedExample = testNavigationHandle.assertOpened<TestNavigationKey>(NavigationDirection.Push)
        
    // asserts that the NavigationHandle has received a close instruction
    testNavigationHandle.assertClosed() 
        
    // asserts that the NavigationHandle has received a close instruction with a specific result,
    // in this example, `ExampleNavigationKey` would need to be a NavigationKey.WithResult<String> 
    // otherwise, it would not be possible for a result of "result" to be returned from the close    
    testNavigationHandle.assertClosedWithResult("result")

    // the "expect" functions work in much the same way as the assertion functions, but return NavigationInstruction.Open<*> 
    // instances, instead of NavigationKey instances. The code below expects that a NavigationInstruction.Open was 
    // performed, with TestNavigationKey as the NavigationKey for the instruction. This is useful when you need access to
    // the NavigationInstruction that was performed, rather than just the NavigationKey.    
    testNavigationHandle.expectOpenInstruction<TestNavigationKey>()
        
    // When you are wanting to test ViewModel result handling, the `expectOpenInstruction` function is quite important,
    // because results are delivered based on a NavigationInstructions not on NavigationKeys.
    // For example, if your ViewModel uses `val exampleResult by registerForNavigationResult<String>()`, and then uses
    // `exampleResult.push(KeyWithStringResult(...))`, and you want to test what happens when the result is delivered, you
    // can use `expectOpenInstruction` and `sendResultForTest` to simulate the result being delivered.
    // Here's an example of testing that a ViewModel triggers navigation for a result, and then how the ViewModel handles the result:
    run {
        // Assuming "getResultFromAnotherScreen" triggers a result channel to be open an "AnotherScreenKey", 
        // and that "AnotherScreenKey" is a NavigationKey.WithResult<String>:
        viewModel.getResultFromAnotherScreen()
        // We can use `expectOpenInstruction` to assert that the testNavigationHandle has received an instruction that has 
        // "AnotherScreenKey" as the NavigationKey, and then store the instruction for later use:
        val anotherScreenInstruction = testNavigationHandle.expectOpenInstruction<AnotherScreenKey>()
        // Now that we have the NavigationInstruction that was performed, we can use `sendResultForTest` to simulate 
        // that NavigationInstruction being closed with a result:
        anotherScreenInstruction.sendResultForTest("result")
        // here's where you would assert that the ViewModel has handled the result correctly, probably by checking that the
        // ViewModel's state has been updated correctly, or some other side effect has occurred
    }
}
```

<details markdown="block">
  <summary class="faq-summary">
    Notes
  </summary>

* If the navigation actions triggered by your Enro ViewModel happen during coroutines, you may need to use a `advanceUntilIdle` or similar in your tests before performing any of the `assert` or `expect` functions on the `TestNavigationHandle`. This is because the TestNavigationHandle does not assume any particular threading model, and will not automatically wait for coroutines to complete before performing assertions.

</details>

## UI Testing
This section is not completed yet. For examples of UI tests that use Enro and run on emulators or devices, please see: 
* [Enro test application tests](https://github.com/isaac-udy/Enro/tree/main/tests/application/src/androidTest/java/dev/enro/test/application)
  * The application that these tests are written for is able to be built and ran locally, and all of the test scenarios can be reproduced through manual testing using this application (which can be useful for understanding how the tests work/what they're doing)
  * These tests are simple and demonstrate the basic usage of Enro in a test application
* [Enro core behaviour tests](https://github.com/isaac-udy/Enro/tree/main/enro/src/androidTest/java/dev/enro)
  * These tests are complex and may be difficult to understand
  * These tests operate on destinations that only exist for the tests, and there is no way to manually reproduce the scenarios that these tests are testing in an application. These tests are used to verify very specific functionality of Enro and ensure backwards compatibility 
