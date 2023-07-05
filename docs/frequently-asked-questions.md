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

When you "present" a screen/destination, you're saying that the screen should appear above the most recently pushed screen. Generally, these destinations are Dialogs, BottomSheets, or similar.

For example, if you have a container with a backstack that looks like this: 
`push(A), push(B), push(C)`, that container will show "C", and no other screens will be visible. 

If you pushed "D", and the backstack became:
`push(A), push(B), push(C), push(D)`, then "C" would animate out, and "D" would become visible. "C" would become inactive.

But if you presented "D" instead, and the backstack was:
`push(A), push(B), push(C), present(D)`, then "C" would not animate out, and both "C" and "D" would be visible (assuming that D did not cover the entire screen). "C" remains active in the background.

If "D" then pushed to "E", and the backstack was:
`push(A), push(B), push(C), present(D), push(E)`, then both "C" and "D" would animate out, and "E" would be visible. Once "E" was closed, both "C" and "D" would become visible again.

</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I create a BottomSheet or a Dialog screen in Compose?
  </summary>
</details>

<details markdown="block">
  <summary class="faq-summary">
    What's a SyntheticDestination?
  </summary>
</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I configure animations?
  </summary>
</details>

<details markdown="block">
  <summary class="faq-summary">
    How do I do analytics when a user views a screen?
  </summary>
</details>
