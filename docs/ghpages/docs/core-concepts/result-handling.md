---
title: Result Handling
parent: Core Concepts
nav_order: 4
---

# Result Handling

One of Enro's most powerful features is type-safe result handling. Screens can return results just
like functions, with full compiler support.

## The Concept

Think of result handling like async function calls:

```kotlin
// Function that returns a value
suspend fun selectPhoto(): Uri {
    // Show photo picker
    return selectedPhotoUri
}

// Enro equivalent
@Serializable
data class SelectPhoto() : NavigationKey.WithResult<Uri>
```

## Basic Result Handling

### Defining a Result-Returning Screen

Use `NavigationKey.WithResult<T>`:

```kotlin
@Serializable
data class SelectColor(
    val currentColor: Color
) : NavigationKey.WithResult<Color>
```

### Registering for Results in Composables

Use `registerForNavigationResult` to handle results:

```kotlin
@Composable
fun MyScreen() {
    var selectedColor by remember { mutableStateOf(Color.Blue) }
    
    val colorPicker = registerForNavigationResult<Color>(
        onClosed = {
            // User cancelled - no result
            println("Color picker was cancelled")
        },
        onCompleted = { color ->
            // User selected a color
            selectedColor = color
        }
    )
    
    Button(onClick = {
        colorPicker.open(SelectColor(currentColor = selectedColor))
    }) {
        Text("Choose Color")
    }
}
```

### Returning Results from Destinations

Use `navigation.complete(result)` to return a result:

```kotlin
@Composable
@NavigationDestination(SelectColor::class)
fun SelectColorDestination() {
    val navigation = navigationHandle<SelectColor>()
    
    LazyColumn {
        items(availableColors) { color ->
            Button(onClick = {
                navigation.complete(color) // Return the result
            }) {
                Text("Select ${color.name}")
            }
        }
    }
    
    // User can still cancel without providing a result
    Button(onClick = { navigation.close() }) {
        Text("Cancel")
    }
}
```

**Remember:** `complete()` represents a positive action, while `close()` represents cancellation or
dismissal.
Result channels receive callbacks for both cases, allowing you to handle user cancellation
appropriately.

## NavigationResultChannel

`registerForNavigationResult` returns a `NavigationResultChannel` that you use to open destinations:

```kotlin
val resultChannel = registerForNavigationResult<Color> { color ->
    println("Received color: $color")
}

// Open the destination
resultChannel.open(SelectColor(currentColor = Color.Blue))
```

## NavigationResultScope

The callbacks in `registerForNavigationResult` receive a `NavigationResultScope`:

```kotlin
val resultChannel = registerForNavigationResult<LocalDate>(
    onClosed = {
        // Access to the instance that was closed
        println("Closed key: ${instance.key}")
        println("Instance ID: ${instance.id}")
    },
    onCompleted = { date ->
        // Access both the result and the instance
        println("Received date: $date")
        println("From key: ${instance.key}")
    }
)
```

## Result Handling in ViewModels

Register for results in ViewModels using the same API:

```kotlin
class ProfileViewModel : ViewModel() {
    private val navigation by navigationHandle<UserProfile>()
    
    val selectPhoto = registerForNavigationResult<Uri> { photoUri ->
        updateProfilePhoto(photoUri)
    }
    
    fun onChangePhotoClicked() {
        selectPhoto.open(SelectPhoto())
    }
    
    private fun updateProfilePhoto(uri: Uri) {
        // Update profile with new photo
    }
}
```

### ViewModel Result Pattern

```kotlin
class CheckoutViewModel : ViewModel() {
    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Initial)
    val checkoutState = _checkoutState.asStateFlow()
    
    val selectPaymentMethod = registerForNavigationResult<PaymentMethod>(
        onClosed = {
            _checkoutState.value = CheckoutState.PaymentCancelled
        },
        onCompleted = { paymentMethod ->
            _checkoutState.value = CheckoutState.PaymentSelected(paymentMethod)
            processPayment(paymentMethod)
        }
    )
}
```

## Result Handling in Fragments (enro-compat)

```kotlin
class ProfileFragment : Fragment() {
    private val navigation by navigationHandle<UserProfile>()
    
    private val selectPhoto by registerForNavigationResult<Uri> { photoUri ->
        updateProfilePhoto(photoUri)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        changePhotoButton.setOnClickListener {
            selectPhoto.open(SelectPhoto())
        }
    }
}
```

## Handling Multiple Result Types

You can register multiple result channels in the same screen:

```kotlin
@Composable
fun EditProductScreen() {
    var product by remember { mutableStateOf(Product()) }
    
    val selectCategory = registerForNavigationResult<Category> { category ->
        product = product.copy(category = category)
    }
    
    val selectSupplier = registerForNavigationResult<Supplier> { supplier ->
        product = product.copy(supplier = supplier)
    }
    
    val selectImage = registerForNavigationResult<Uri> { imageUri ->
        product = product.copy(imageUri = imageUri)
    }
    
    Column {
        Button(onClick = { selectCategory.open(SelectCategory()) }) {
            Text("Choose Category")
        }
        Button(onClick = { selectSupplier.open(SelectSupplier()) }) {
            Text("Choose Supplier")
        }
        Button(onClick = { selectImage.open(SelectImage()) }) {
            Text("Choose Image")
        }
    }
}
```

## Result Forwarding

### completeFrom()

Delegate result handling to another screen:

```kotlin
@Composable
@NavigationDestination(QuickSelect::class)
fun QuickSelectDestination() {
    val navigation = navigationHandle<QuickSelect>()
    
    Column {
        commonOptions.forEach { option ->
            Button(onClick = {
                navigation.complete(option)
            }) {
                Text(option.name)
            }
        }
        
        Button(onClick = {
            // Open full list and forward its result
            navigation.completeFrom(FullSelection())
        }) {
            Text("See All Options")
        }
    }
}
```

### closeAndCompleteFrom()

Close current screen and forward result from another:

```kotlin
Button(onClick = {
    navigation.closeAndCompleteFrom(AdvancedSelection())
}) {
    Text("Advanced Selection")
}
```

## WithResult<Unit>

When you need to know a screen completed successfully but don't need specific data:

```kotlin
@Serializable
data class ConfirmAction(
    val actionName: String
) : NavigationKey.WithResult<Unit>

// Register for confirmation
val confirmAction = registerForNavigationResult<Unit>(
    onClosed = {
        // User dismissed or cancelled
        println("Action cancelled")
    },
    onCompleted = {
        // User confirmed - perform action
        performAction()
    }
)

// Open confirmation dialog
confirmAction.open(ConfirmAction("Delete Item"))
```

In the destination:

```kotlin
@Composable
@NavigationDestination(ConfirmAction::class)
fun ConfirmActionDestination() {
    val navigation = navigationHandle<ConfirmAction>()
    
    AlertDialog(
        title = { Text("Confirm") },
        text = { Text("Are you sure you want to ${navigation.key.actionName}?") },
        onDismissRequest = { navigation.close() }, // User dismissed
        confirmButton = {
            Button(onClick = { navigation.complete(Unit) }) { // User confirmed
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { navigation.close() }) { // User explicitly cancelled
                Text("Cancel")
            }
        }
    )
}
```

## Complex Result Types

### Data Classes

```kotlin
@Serializable
data class UserSelection(
    val selectedUsers: List<UserId>,
    val includeInactive: Boolean
) : NavigationKey.WithResult<UserSelection>

val selectUsers = registerForNavigationResult<UserSelection> { selection ->
    processUserSelection(
        users = selection.selectedUsers,
        includeInactive = selection.includeInactive
    )
}
```

### Sealed Classes

```kotlin
@Serializable
sealed class FilterResult {
    @Serializable
    data class Applied(val filters: List<Filter>) : FilterResult()
    
    @Serializable
    object Cleared : FilterResult()
}

@Serializable
data class ApplyFilters() : NavigationKey.WithResult<FilterResult>

val filterResults = registerForNavigationResult<FilterResult> { result ->
    when (result) {
        is FilterResult.Applied -> applyFilters(result.filters)
        FilterResult.Cleared -> clearFilters()
    }
}
```

## Result Channels and Lifecycle

Result channels are lifecycle-aware:

```kotlin
@Composable
fun MyScreen() {
    // Channel is automatically managed
    val resultChannel = registerForNavigationResult<String> { result ->
        // This callback is only called when the screen is active
    }
    
    // When the screen is disposed, the channel is cleaned up
}
```

In ViewModels, channels survive configuration changes:

```kotlin
class MyViewModel : ViewModel() {
    // Survives configuration changes
    val resultChannel = registerForNavigationResult<String> { result ->
        // Handle result
    }
}
```

## Handling Navigation Without Results

For screens that don't return results, simply open them:

```kotlin
@Composable
fun MyScreen() {
    val navigation = navigationHandle()
    
    Button(onClick = {
        navigation.open(DetailsScreen(id = "123"))
    }) {
        Text("View Details")
    }
}
```

## Testing Result Handling

With `enro-test`:

```kotlin
@Test
fun testResultHandling() = runEnroTest {
    val testNavigation = fakeNavigationHandle<MyScreen>()
    
    // Register a test result channel
    val resultChannel = testNavigation.registerForResult<Color>()
    
    // Open destination
    resultChannel.open(SelectColor(Color.Blue))
    
    // Simulate result
    testNavigation.completeWithResult(Color.Red)
    
    // Verify result was received
    assertEquals(Color.Red, resultChannel.lastResult)
}
```

## Best Practices

### Use Specific Result Types

```kotlin
// ✅ GOOD - Specific type
NavigationKey.WithResult<UserId>
NavigationKey.WithResult<List<Product>>

// ❌ BAD - Too generic
NavigationKey.WithResult<Any>
NavigationKey.WithResult<String> // When you need more structure
```

### Handle Both Closed and Completed

```kotlin
// ✅ GOOD - Handle both cases
val resultChannel = registerForNavigationResult<Color>(
    onClosed = {
        // User cancelled - keep current color
        println("Selection cancelled")
    },
    onCompleted = { color ->
        // User selected - update color
        updateColor(color)
    }
)

// ❌ BAD - Only handle completed
val resultChannel = registerForNavigationResult<Color> { color ->
    updateColor(color)
}
// If user cancels, you won't know
```

### Use Descriptive Names

```kotlin
// ✅ GOOD
val selectPaymentMethod = registerForNavigationResult<PaymentMethod> { }
val chooseDate = registerForNavigationResult<LocalDate> { }

// ❌ BAD
val result1 = registerForNavigationResult<PaymentMethod> { }
val x = registerForNavigationResult<LocalDate> { }
```

### Keep Result Types Serializable

```kotlin
// ✅ GOOD
@Serializable
data class SelectionResult(val items: List<String>)

NavigationKey.WithResult<SelectionResult>

// ❌ BAD - Not serializable
data class SelectionResult(
    val items: List<String>,
    val callback: () -> Unit // Can't serialize functions!
)
```

## Common Patterns

### Confirmation Dialogs

```kotlin
@Composable
fun DeleteButton(itemId: String) {
    val confirmDelete = registerForNavigationResult<Boolean> { confirmed ->
        if (confirmed) {
            deleteItem(itemId)
        }
    }
    
    Button(onClick = {
        confirmDelete.open(
            ConfirmDialog(
                title = "Delete Item",
                message = "Are you sure?",
                confirmText = "Delete",
                cancelText = "Cancel"
            )
        )
    }) {
        Text("Delete")
    }
}
```

### Form Results

```kotlin
@Composable
fun ProfileScreen() {
    var userProfile by remember { mutableStateOf(UserProfile()) }
    
    val editProfile = registerForNavigationResult<UserProfile> { updatedProfile ->
        userProfile = updatedProfile
        saveProfile(updatedProfile)
    }
    
    Button(onClick = {
        editProfile.open(EditProfile(currentProfile = userProfile))
    }) {
        Text("Edit Profile")
    }
}
```

### Multi-Step Selection

```kotlin
@Composable
fun FilterScreen() {
    var filters by remember { mutableStateOf(Filters()) }
    
    val selectCategory = registerForNavigationResult<Category> { category ->
        filters = filters.copy(category = category)
    }
    
    val selectPriceRange = registerForNavigationResult<PriceRange> { range ->
        filters = filters.copy(priceRange = range)
    }
    
    val selectTags = registerForNavigationResult<List<Tag>> { tags ->
        filters = filters.copy(tags = tags)
    }
}
```

## Next Steps

- [Navigation Flows](../advanced/navigation-flows.md) - Build multi-step result flows
- [Navigation Containers](navigation-containers.md) - Understand container-based navigation
- [Testing](../advanced/testing.md) - Test result handling

---

**Questions?** Check the [FAQ](../faq.md) or open an issue
on [GitHub](https://github.com/isaac-udy/Enro).
