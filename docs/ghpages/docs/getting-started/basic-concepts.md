# Basic Concepts

This guide introduces the fundamental concepts of Enro's navigation system.

## Navigation Philosophy

At its core, Enro is built around a few key principles:

1. **Screen Contracts**: Each screen has a clearly defined contract (NavigationKey) that specifies its inputs and outputs.
2. **Decoupled Navigation**: Screens don't need to know about the implementation of other screens they navigate to.
3. **Type Safety**: Navigation is fully type-safe, with compile-time checking of navigation parameters.
4. **Platform Agnostic**: The same navigation concepts work across different platforms and UI frameworks.

## Core Components

### NavigationKey

A NavigationKey represents the contract for a screen. It defines:
- The inputs required to display the screen
- The output type (if any) that the screen can produce
- Any additional metadata needed for navigation

```kotlin
// Simple key
data class ProfileKey(
    val userId: String
) : NavigationKey.SupportsPush

// Key with result
data class SelectDateKey(
    val initialDate: LocalDate? = null
) : NavigationKey.SupportsPresent.WithResult<LocalDate>
```

### NavigationDestination

A NavigationDestination is a screen implementation that can be navigated to. It is bound to a specific NavigationKey type and can be implemented as:
- Android: Activity, Fragment, or Composable
- iOS: UIViewController or SwiftUI View
- Desktop: Window or Composable

```kotlin
@NavigationDestination(ProfileKey::class)
@Composable
fun ProfileScreen() {
    val navigation = navigationHandle<ProfileKey>()
    // Screen implementation...
}
```

### NavigationContainer

A NavigationContainer is a location within the UI that can host screens. It:
- Maintains a backstack of screens
- Manages screen lifecycles
- Handles navigation animations
- Can be nested within other containers

```kotlin
val container = rememberNavigationContainer(
    root = HomeKey(),
    emptyBehavior = EmptyBehavior.CloseParent
)
```

### NavigationHandle

A NavigationHandle provides the API for controlling navigation within a screen. It:
- Provides access to the screen's NavigationKey
- Allows execution of navigation instructions
- Manages navigation state
- Handles navigation results

```kotlin
val navigation = navigationHandle<ProfileKey>()
navigation.push(EditProfileKey(navigation.key.userId))
```

## Navigation Flow

1. **Navigation Request**: A screen uses its NavigationHandle to request navigation to another screen
2. **Key Creation**: The NavigationKey for the target screen is created with the required parameters
3. **Destination Resolution**: Enro finds the appropriate NavigationDestination for the key
4. **Screen Creation**: The destination is created and displayed
5. **Result Handling**: If the screen produces a result, it's returned to the calling screen

## Next Steps

- Learn about [Navigation Keys](../core-concepts/navigation-keys.md)
- Understand [Navigation Destinations](../core-concepts/navigation-destinations.md)
- Explore [Navigation Containers](../core-concepts/navigation-containers.md)
- See how to use [Navigation Handles](../core-concepts/navigation-handles.md) 