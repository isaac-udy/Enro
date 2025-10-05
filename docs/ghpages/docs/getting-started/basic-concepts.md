---
title: Basic Concepts
parent: Getting Started
nav_order: 2
---

# Basic Concepts

Enro is built on a simple but powerful idea: **screens should behave like functions**. This guide
introduces the fundamental concepts that make this possible.

## The Core Idea

Think about how functions work in programming:

```kotlin
fun calculateTotal(items: List<Item>, tax: Double): Money {
    // Calculate and return result
}
```

Functions have:

- **Parameters** (inputs like `items` and `tax`)
- **Return values** (outputs like `Money`)
- **Type safety** (the compiler checks your usage)

Enro applies this same pattern to navigation:

```kotlin
@Serializable
data class CheckoutScreen(
    val items: List<Item>,
    val tax: Double
) : NavigationKey.WithResult<Money>
```

Now screens have:

- **Parameters** (defined as properties)
- **Return values** (defined by `WithResult<T>`)
- **Type safety** (enforced by the compiler)

## The Three Pillars

Enro is built on three fundamental concepts:

### 1. NavigationKeys

**NavigationKeys are contracts** that define what a screen needs and what it returns.

```kotlin
// A simple screen with parameters
@Serializable
data class UserProfile(
    val userId: String,
    val showEditButton: Boolean = true
) : NavigationKey

// A screen that returns a result
@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null
) : NavigationKey.WithResult<LocalDate>
```

NavigationKeys:

- Are serializable (use `@Serializable` annotation)
- Can have parameters (as properties)
- Can optionally return results (implement `WithResult<T>`)
- Serve as the single source of truth for a screen's interface

### 2. NavigationDestinations

**NavigationDestinations are implementations** that fulfill NavigationKey contracts.

```kotlin
@Composable
@NavigationDestination(UserProfile::class)
fun UserProfileScreen() {
    val navigation = navigationHandle<UserProfile>()
    
    // Access parameters from the key
    val userId = navigation.key.userId
    val showEditButton = navigation.key.showEditButton
    
    Column {
        Text("User: $userId")
        if (showEditButton) {
            Button(onClick = { /* edit */ }) {
                Text("Edit Profile")
            }
        }
    }
}
```

NavigationDestinations:

- Implement the screen's UI (Composable, Fragment, or Activity)
- Are bound to a NavigationKey via `@NavigationDestination`
- Access parameters through the `navigationHandle`
- Can be platform-specific or shared

### 3. NavigationHandles

**NavigationHandles are controllers** that let you perform navigation operations.

```kotlin
@Composable
fun MyScreen() {
    val navigation = navigationHandle()
    
    Button(onClick = {
        // Navigate to another screen
        navigation.open(UserProfile(userId = "123"))
    }) {
        Text("Open Profile")
    }
}
```

NavigationHandles let you:

- Open other screens (`open()`)
- Close the current screen (`close()`)
- Complete the current screen (`complete()` or `complete(result)`)
- Access the current screen's parameters (`navigation.key`)

## Completing vs Closing Screens

**Any screen can be completed**, regardless of whether it has a result type. The distinction between
`complete()` and `close()` is semantic:

- **`close()`** - Represents dismissal or cancellation (e.g., back button, dismiss gesture, cancel
  button)
- **`complete()`** - Represents a positive action or confirmation (e.g., save button, confirm
  button, successful completion)

```kotlin
@Composable
@NavigationDestination(ConfirmDialog::class)
fun ConfirmDialogScreen() {
    val navigation = navigationHandle<ConfirmDialog>()
    
    AlertDialog(
        onDismissRequest = { 
            navigation.close() // User dismissed/cancelled
        },
        confirmButton = {
            Button(onClick = { 
                navigation.complete() // User confirmed (no result needed)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { 
                navigation.close() // User explicitly cancelled
            }) {
                Text("Cancel")
            }
        }
    )
}
```

**`WithResult<T>`** adds the requirement to provide a value when completing:

```kotlin
@Serializable
data class SelectColor() : NavigationKey.WithResult<Color>

@Composable
@NavigationDestination(SelectColor::class)
fun SelectColorScreen() {
    val navigation = navigationHandle<SelectColor>()
    
    // Must provide a Color when completing
    Button(onClick = { navigation.complete(Color.Blue) }) {
        Text("Select Blue")
    }
    
    // Can still close without a result (cancellation)
    Button(onClick = { navigation.close() }) {
        Text("Cancel")
    }
}
```

Not all screens need to use `complete()`. If there's no positive action or confirmation, just use
`close()`.

## How It All Works Together

Let's build a simple example showing how these concepts work together:

### Step 1: Define NavigationKeys

```kotlin
// Main screen - no parameters or results
@Serializable
object HomeScreen : NavigationKey

// Detail screen - takes a parameter
@Serializable
data class ProductDetail(val productId: String) : NavigationKey

// Picker screen - returns a result
@Serializable
data class ColorPicker(
    val currentColor: Color
) : NavigationKey.WithResult<Color>
```

### Step 2: Create Destinations

```kotlin
@Composable
@NavigationDestination(HomeScreen::class)
fun HomeScreenDestination() {
    val navigation = navigationHandle()
    
    Column {
        Text("Welcome to the Shop!")
        Button(onClick = {
            navigation.open(ProductDetail(productId = "widget-1"))
        }) {
            Text("View Product")
        }
    }
}

@Composable
@NavigationDestination(ProductDetail::class)
fun ProductDetailDestination() {
    val navigation = navigationHandle<ProductDetail>()
    val productId = navigation.key.productId
    
    var selectedColor by remember { mutableStateOf(Color.Blue) }
    
    val colorPicker = registerForNavigationResult<Color> { color ->
        selectedColor = color
    }
    
    Column {
        Text("Product: $productId")
        Text("Color: $selectedColor")
        
        Button(onClick = {
            colorPicker.open(ColorPicker(currentColor = selectedColor))
        }) {
            Text("Choose Color")
        }
    }
}

@Composable
@NavigationDestination(ColorPicker::class)
fun ColorPickerDestination() {
    val navigation = navigationHandle<ColorPicker>()
    
    Column {
        listOf(Color.Red, Color.Green, Color.Blue).forEach { color ->
            Button(onClick = {
                navigation.complete(color) // Return the selected color
            }) {
                Text("Select $color")
            }
        }
        
        Button(onClick = {
            navigation.close() // User cancelled selection
        }) {
            Text("Cancel")
        }
    }
}
```

### Step 3: Use It

The navigation system handles all the complexity:

- Type-safe parameter passing
- Result delivery
- Backstack management
- State preservation
- UI rendering

## Key Advantages

### Type Safety

```kotlin
// Compile error - wrong parameter type
navigation.open(UserProfile(userId = 123)) // ❌ Type mismatch

// Compile error - missing required parameter
navigation.open(UserProfile()) // ❌ Missing userId

// Correct usage
navigation.open(UserProfile(userId = "123")) // ✅
```

### Discoverability

Your IDE's autocomplete shows you:

- What parameters a screen needs
- What type of result it returns
- Where the screen is used in your codebase

### Refactoring Safety

When you change a NavigationKey:

```kotlin
// Change this...
data class UserProfile(val userId: String)

// To this...
data class UserProfile(val userId: String, val tab: String)
```

The compiler will find every place that needs updating.

### Clear Contracts

Anyone reading your code can immediately see:

- What data flows into a screen
- What data flows out of a screen
- How screens are connected

## Multiplatform Support

Enro supports Kotlin Multiplatform. You can share NavigationKeys across platforms:

```kotlin
// commonMain - shared across all platforms
@Serializable
data class ProductDetail(val productId: String) : NavigationKey
```

And provide platform-specific implementations:

```kotlin
// androidMain
@Composable
@NavigationDestination(ProductDetail::class)
fun AndroidProductDetail() { /* Android-specific UI */ }

// iosMain
@Composable
@NavigationDestination(ProductDetail::class)
fun IosProductDetail() { /* iOS-specific UI */ }
```

## Navigation Scenes

Enro automatically handles how screens are rendered using **scenes**:

```kotlin
// Rendered as a full-screen destination
@NavigationDestination(ProductDetail::class)
fun ProductDetailScreen() { /* ... */ }

// Rendered as a dialog
@NavigationDestination(ConfirmDialog::class)
val confirmDialog = navigationDestination<ConfirmDialog>(
    metadata = { dialog() }
) { /* ... */ }
```

Scenes determine:

- Single-pane vs multi-pane layouts
- Dialog vs full-screen rendering
- Overlay behavior
- Animation styles

## Navigation Containers

Complex apps need nested navigation hierarchies. Enro provides **containers**:

```kotlin
@Composable
fun MainScreen() {
    val container = rememberNavigationContainer(
        backstack = listOf(HomeScreen.asInstance()),
        emptyBehavior = EmptyBehavior.closeParent(),
        filter = accept { /* what this container accepts */ }
    )
    
    NavigationDisplay(container)
}
```

Containers manage:

- Independent backstacks
- Filtering (what screens can appear in this container)
- Empty behavior (what happens when the container is empty)
- Nested navigation

## Next Steps

Now that you understand the basics, dive deeper into each concept:

- **[NavigationKeys](../core-concepts/navigation-keys.md)** - Learn all about defining keys
- **[NavigationDestinations](../core-concepts/navigation-destinations.md)** - Master creating
  destinations
- **[Navigation Operations](../core-concepts/navigation-operations.md)** - Understand all navigation
  operations
- **[Result Handling](../core-concepts/result-handling.md)** - Handle results between screens
- **[Navigation Containers](../core-concepts/navigation-containers.md)** - Build complex navigation
  hierarchies

## Quick Reference

| Concept                   | Purpose                | Example                                                  |
|---------------------------|------------------------|----------------------------------------------------------|
| **NavigationKey**         | Define screen contract | `data class Profile(val id: String) : NavigationKey`     |
| **NavigationDestination** | Implement screen UI    | `@NavigationDestination(Profile::class)`                 |
| **NavigationHandle**      | Control navigation     | `navigation.open(Profile("123"))`                        |
| **WithResult**            | Return a value         | `NavigationKey.WithResult<Color>`                        |
| **@Serializable**         | Make key serializable  | Required on all NavigationKeys                           |
| **open()**                | Navigate to screen     | `navigation.open(key)`                                   |
| **close()**               | Dismiss/cancel screen  | `navigation.close()`                                     |
| **complete()**            | Confirm/finish screen  | `navigation.complete()` or `navigation.complete(result)` |

---

**Questions?** Check the [FAQ](../faq.md) or join the discussion
on [GitHub](https://github.com/isaac-udy/Enro).
