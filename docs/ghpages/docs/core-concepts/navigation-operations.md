---
title: Navigation Operations
parent: Core Concepts
nav_order: 3
---

# Navigation Operations

Navigation operations are actions that change the navigation state of your application. In Enro 3.x,
all navigation is unified through the `open()` method, which internally uses different operation
types based on context.

## The Unified `open()` Method

In Enro 3.x, you use `open()` for all navigation:

```kotlin
@Composable
fun MyScreen() {
    val navigation = navigationHandle()
    
    Button(onClick = {
        navigation.open(UserProfile(userId = "123"))
    }) {
        Text("Open Profile")
    }
}
```

Enro automatically determines the appropriate operation based on:

- The NavigationKey type
- The current navigation context
- Available navigation containers
- Scene metadata

This is a major simplification from Enro 2.x, which required choosing between `push()` and
`present()`.

## Core Operations

### open()

Opens a destination by creating a NavigationKey.Instance and executing it:

```kotlin
// Simple navigation
navigation.open(ProductDetail(productId = "widget-1"))

// With metadata
navigation.open(
    ConfirmDialog(message = "Are you sure?")
        .withMetadata(DialogPropertiesKey, customProperties)
)
```

### close()

Closes the current destination:

```kotlin
Button(onClick = { navigation.close() }) {
    Text("Close")
}
```

When a destination closes:

- It's removed from the backstack
- Result channels are notified (if registered)
- The previous destination becomes active

### complete()

Completes a destination, representing a positive action or confirmation.

**Any screen can be completed**, regardless of whether it has a result type:

```kotlin
// Screen without a result - complete represents confirmation
@Composable
@NavigationDestination(ConfirmDialog::class)
fun ConfirmDialogDestination() {
    val navigation = navigationHandle<ConfirmDialog>()
    
    AlertDialog(
        onDismissRequest = { navigation.close() }, // Dismissal
        confirmButton = {
            Button(onClick = { 
                navigation.complete() // Positive action
            }) {
                Text("Confirm")
            }
        }
    )
}

// Screen with a result - complete requires a value
@Composable
@NavigationDestination(SelectColor::class)
fun SelectColorDestination() {
    val navigation = navigationHandle<SelectColor>()
    
    Button(onClick = {
        navigation.complete(Color.Blue) // Return the result
    }) {
        Text("Select Blue")
    }
}
```

**The semantic difference between `complete()` and `close()`:**

- `complete()` - Positive action (confirm, save, select, finish)
- `close()` - Dismissal or cancellation (back button, cancel, dismiss)

For `NavigationKey.WithResult<T>`, you must provide a value when completing:

```kotlin
// Must provide a result
navigation.complete(Color.Blue)

// Can still close without a result (cancellation)
navigation.close()
```

For regular `NavigationKey` (no result type), complete takes no arguments:

```kotlin
// Positive action completed
navigation.complete()

// User cancelled/dismissed
navigation.close()
```

Not all screens need to use `complete()`. If there's no positive action or confirmation scenario,
just use `close()`.

### completeFrom()

Delegates the result responsibility to another screen:

```kotlin
@Composable
@NavigationDestination(EditProfile::class)
fun EditProfileScreen() {
    val navigation = navigationHandle<EditProfile>()
    
    Button(onClick = {
        // Delegate the result to the confirmation screen
        navigation.completeFrom(
            ConfirmChanges(changes = currentChanges)
        )
    }) {
        Text("Save Changes")
    }
}
```

When the delegated screen completes, its result is returned as if the original screen completed.

## Compound Operations

### closeAndReplaceWith()

Closes the current screen and opens another:

```kotlin
Button(onClick = {
    navigation.closeAndReplaceWith(
        ProductDetail(productId = "widget-2")
    )
}) {
    Text("View Related Product")
}
```

Equivalent to:

```kotlin
navigation.close()
navigation.open(ProductDetail(productId = "widget-2"))
```

### closeAndCompleteFrom()

Closes the current screen and delegates the result:

```kotlin
Button(onClick = {
    navigation.closeAndCompleteFrom(
        SelectFromList(items = availableItems)
    )
}) {
    Text("Select from Full List")
}
```

## Advanced: Direct NavigationOperation Usage

For advanced use cases, you can create operations manually:

```kotlin
import dev.enro.NavigationOperation

// Create an Open operation
val openOp = NavigationOperation.Open(
    UserProfile(userId = "123").asInstance()
)

// Create a Close operation
val closeOp = NavigationOperation.Close(
    navigation.instance
)

// Create a Complete operation
val completeOp = NavigationOperation.Complete(
    navigation.instance,
    result = selectedColor
)

// Execute the operation
navigation.execute(openOp)
```

### Aggregate Operations

Execute multiple operations atomically:

```kotlin
val aggregateOp = NavigationOperation.AggregateOperation(
    NavigationOperation.Close(currentInstance),
    NavigationOperation.Open(newInstance)
)

navigation.execute(aggregateOp)
```

### SetBackstack Operation

Directly set a container's backstack:

```kotlin
val setBackstackOp = NavigationOperation.SetBackstack(
    currentBackstack = container.backstack,
    targetBackstack = listOf(
        HomeScreen.asInstance(),
        CategoryList(category = "electronics").asInstance(),
        ProductDetail(productId = "widget-1").asInstance()
    )
)
```

This is useful for:

- Deep linking
- State restoration
- Complex navigation flows

## Navigation Context and Operation Routing

When you call `open()`, Enro needs to determine WHERE to display the destination. This routing is
based on:

### 1. Scene Metadata

```kotlin
// Dialog destinations render as overlays
@NavigationDestination(ConfirmDialog::class)
val confirmDialog = navigationDestination<ConfirmDialog>(
    metadata = { dialog() }
) { /* content */ }

// When opened, appears as a dialog
navigation.open(ConfirmDialog(message = "Confirm?"))
```

### 2. Container Filters

Containers can accept or reject destinations:

```kotlin
val container = rememberNavigationContainer(
    backstack = listOf(HomeScreen.asInstance()),
    filter = accept {
        key<ProductDetail>()
        key<ProductList>()
    }
)
```

Only `ProductDetail` and `ProductList` can be added to this container.

### 3. Navigation Hierarchy

Enro searches up the navigation hierarchy for a suitable container:

```
Root Container (accepts all)
  ├─ Main Screen Container (accepts main screens)
  │   ├─ Home Screen
  │   └─ (open here)
  └─ Settings Container (accepts settings screens)
```

When you call `open()` from Home Screen, Enro looks for:

1. A container in Home Screen (if it has one)
2. The Main Screen Container
3. The Root Container

## Best Practices

### Use Descriptive Variable Names

```kotlin
// ✅ GOOD
val confirmationDialog = ConfirmDialog(message = "Delete item?")
navigation.open(confirmationDialog)

// ❌ BAD
val x = ConfirmDialog(message = "Delete item?")
navigation.open(x)
```

### Handle Navigation in ViewModels

```kotlin
class ProfileViewModel : ViewModel() {
    private val navigation by navigationHandle<UserProfile>()
    
    fun onEditButtonClicked() {
        navigation.open(EditProfile(userId = navigation.key.userId))
    }
    
    fun onBackPressed() {
        navigation.close()
    }
}
```

### Check for Navigation Availability

```kotlin
if (navigation.canNavigate()) {
    navigation.open(NextScreen())
}
```

### Avoid Navigation in Initialization

```kotlin
// ❌ BAD - Don't navigate during composition
@Composable
fun BadScreen() {
    val navigation = navigationHandle()
    navigation.open(NextScreen()) // Don't do this!
}

// ✅ GOOD - Navigate in response to events
@Composable
fun GoodScreen() {
    val navigation = navigationHandle()
    
    LaunchedEffect(someCondition) {
        if (shouldNavigate) {
            navigation.open(NextScreen())
        }
    }
}
```

## Next Steps

- [Result Handling](result-handling.md) - Learn how to handle results from destinations
- [Navigation Containers](navigation-containers.md) - Understand container hierarchies
- [Navigation Scenes](../advanced/navigation-scenes.md) - Control how destinations are rendered

---

**Questions?** Check the [FAQ](../faq.md) or open an issue
on [GitHub](https://github.com/isaac-udy/Enro).
