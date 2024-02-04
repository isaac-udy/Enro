# NavigationKeys
NavigationKeys are the foundation of Enro. They are the contract that defines how navigation works in your application.

A NavigationKey is a simple data class that represents a screen in your application. It can be thought of like the function signature or interface for a screen. Just like a function signature, a NavigationKey represents a contract. 

## Defining NavigationKeys
To define a NavigationKey, create a data class that implements the NavigationKey interface. There are several different interfaces underneath NavigationKey which provide different options. The interfaces that your NavigationKeys implement will depend on the type of navigation that the NavigationKey should support.

Note on Parcelable:
The NavigationKey interface extends Parcelable. All NavigationKeys must be parcelable so that they can be passed between screens, and saved/restored during configuration changes and application process death. Using the `kotlin-parcelize` plugin and the `@Parcelize` annotation to generate a Parcelable implementation for your NavigationKeys is the easiest way to support this.

### SupportsPush
NavigationKeys that extend `NavigationKey.SupportsPush` can be used to push a new screen onto a NavigationContainer's backstack. This is the most common type of navigation, and is used for screens that should fill the entire space within a container. When a screen is pushed, the previous screen is hidden, and the new screen is shown. Only the top-most pushed screen will be visible within a container.

```kotlin

@Parcelize
data class ShowUserProfile(
   val userId: UserId
) : NavigationKey.SupportsPush

```

### SupportsPresent
NavigationKeys that extend `NavigationKey.SupportsPresent` can be used to present a new screen on top of the current screen. This is useful for screens that should be shown in a dialog, or screens that should only take up part of the space within a container. When a screen is presented, the top-most pushed screen will be visible underneath it.

```kotlin
data class ShowUpdateRequired(
   val updateUrl: String
) : NavigationKey.SupportsPresent
```

### WithResult
NavigationKeys that implement `NavigationKey.SupportsPush.WithResult`, or `NavigationKey.SupportsPresent.WithResult` can be used to declare screens that return a result. Both SupportsPush and SupportsPresent screens can return results.

```kotlin

@Parcelize
data class SelectDate(
   val minDate: LocalDate? = null,
   val maxDate: LocalDate? = null,
) : NavigationKey.SupportsPresent.WithResult<LocalDate>

```

### Mixing and matching
A NavigationKey can support multiple types of navigation. For example, a screen that can be pushed, presented, and returns a result could look like this:

```kotlin

@Parcelize
data class MixAndMatchScreen(
    val parameter: String, 
) : NavigationKey.SupportsPush.WithResult<String>, NavigationKey.SupportsPresent.WithResult<String>

```

## Using NavigationKeys to Navigate
To perform navigation using a NavigationKey, you'll need to get a NavigationHandle. A NavigationHandle is a simple interface that allows you to perform navigation. The syntax for getting a NavigationHandle is slightly different depending on the type of screen you're in. For more information on NavigationHandles, please see the [NavigationHandles documentation](./navigation-handles.md).

To push or present, all you need is a NavigationHandle:
```kotlin

val navigationHandle = // get a navigation handle from somewhere

// Push a screen onto the backstack
navigationHandle.push(
    ShowUserProfile(userId = "1234")
)

// Present a screen on top of the current screen
navigationHandle.present(
    ShowUpdateRequired(updateUrl = "https://example.com/update")
)

```

To receive a result from a destination, you'll need to set up a NavigationResultChannel. Just with a NavgationHandle, the syntax for creating a NavigationResultChannel is slightly different depending on the type of screen you're in. For more information on NavigationResultChannels, please see the [Navigation results documentation](./navigation-results.md).

When you have created a NavigationResultChannel, it is very similar to using a NavigationHandle to perform navigation. You can use the `push` or `present` functions to either push or present the NavigationKey. The only difference is that you will receive a result from the destination, which will be sent to the `onResult` lambda that you provide when creating the NavigationResultChannel.

```kotlin

val resultChannel by registerForNavigationResult<LocalDate> { selectedDate: LocalDate -> 
  /* do something! */ 
}

resultChannel.present(
  SelectDate(maxDate = LocalDate.now()
)

```

## Binding NavigationKeys to a destination
Once you've defined a NavigationKey, it is important to bind it to a destination. A destination is a screen that will be shown when the NavigationKey is used. Activities, Fragments and Composables can all be used as NavigationDestinations. In general, to bind a NavigationKey to a destination, you'll need to either annotate the destination with `@NavigationDestination` and providing a class reference to the NavigationKey, or manually bind the NavigationKey to the destination when creating the NavigationController for your application. For more information on NavigationDestinations, please see the [Navigation destinations documentation](./navigation-destinations.md).

## Naming NavigationKeys
Enro does not make a strong recommendation on how NavigationKeys should be named at this stage. There are however some conventions that have been identified in different projects. The most important thing is to be consistent within your own project, and use a pattern that feels natural to you. Here are some common patterns that have been identified in different projects:

1. Name NavigationKeys like actions `ShowUserProfile`, `SelectDate`, `ShowUpdateRequired`. This makes it clear that the NavigationKey represents an action, and that invoking the action will result in a screen being shown.
2. Name NavigationKeys using a "screen" suffix `UserProfileScreen`, `DateSelectionScreen`, `UpdateRequiredScreen`. This makes it clear that the NavigationKey represents a screen, and that the screen can be shown by invoking the NavigationKey.
3. Name NavigationKeys using a "key" suffix `UserProfileKey`, `DateSelectionKey`, `UpdateRequiredKey`. This makes it clear that the NavigationKey represents a NavigationKey.
4. Name NavigationKeys using a "destination" suffix `UserProfileDestination`, `DateSelectionDestination`, `UpdateRequiredDestination`. This makes it clear that the NavigationKey represents a NavigationDestination binding.
