---
title: Navigation Keys
parent: Core Concepts
nav_order: 1
---

# Navigation Keys

NavigationKeys are the foundation of Enro. They define the contract for a screen: what parameters it
needs, and optionally, what result it returns.

## The Function Analogy

Think of NavigationKeys like function signatures. Just as a function signature declares:

```kotlin
fun checkout(items: List<Item>, coupon: String?): Receipt
```

A NavigationKey declares the same information for a screen:

```kotlin
@Serializable
data class CheckoutScreen(
    val items: List<Item>,
    val coupon: String? = null
) : NavigationKey.WithResult<Receipt>
```

## Basic NavigationKey

The simplest NavigationKey has no parameters and returns no result:

```kotlin
@Serializable
object HomeScreen : NavigationKey
```

Use an `object` for screens that don't need parameters, or a `data class` when you need to pass
information:

```kotlin
@Serializable
data class UserProfile(
    val userId: String,
    val tab: ProfileTab = ProfileTab.OVERVIEW
) : NavigationKey
```

## Required Elements

Every NavigationKey must:

### 1. Be Serializable

Use the `@Serializable` annotation from kotlinx.serialization:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetail(val productId: String) : NavigationKey
```

This allows Enro to:

- Save and restore navigation state
- Pass keys across process boundaries
- Handle configuration changes
- Support deep linking

### 2. Implement NavigationKey

All navigation keys must implement the `NavigationKey` interface or one of its subtypes:

```kotlin
interface NavigationKey {
    // Simple navigation with no result
}

interface NavigationKey.WithResult<T> {
    // Navigation that returns a typed result
}
```

## NavigationKeys with Results

When a screen needs to return data, use `NavigationKey.WithResult<T>`:

```kotlin
@Serializable
data class SelectColor(
    val currentColor: Color,
    val allowCustomColors: Boolean = true
) : NavigationKey.WithResult<Color>
```

The type parameter `T` specifies what type of result the screen returns. Common result types:

```kotlin
// Return a simple value
NavigationKey.WithResult<String>
NavigationKey.WithResult<Int>
NavigationKey.WithResult<Boolean>

// Return a complex object
NavigationKey.WithResult<User>
NavigationKey.WithResult<PaymentMethod>

// Return a list
NavigationKey.WithResult<List<Item>>

// Return Unit when you need to know the screen completed successfully
// but don't need any specific data
NavigationKey.WithResult<Unit>
```

## Default Parameters

Use default parameters to make keys more convenient:

```kotlin
@Serializable
data class SettingsScreen(
    val section: SettingsSection = SettingsSection.GENERAL,
    val highlightRecentChanges: Boolean = false
) : NavigationKey

// Can be called with no arguments
navigation.open(SettingsScreen())

// Or with specific arguments
navigation.open(SettingsScreen(section = SettingsSection.PRIVACY))
```

## Complex Parameters

NavigationKeys can accept complex types, but they must be serializable:

```kotlin
@Serializable
data class RecipeDetail(
    val recipeId: String,
    val servings: Int,
    val dietary: List<DietaryRestriction>
) : NavigationKey

@Serializable
enum class DietaryRestriction {
    VEGETARIAN,
    VEGAN,
    GLUTEN_FREE,
    DAIRY_FREE
}
```

### Nested Data Classes

```kotlin
@Serializable
data class FilteredProductList(
    val filters: ProductFilters
) : NavigationKey

@Serializable
data class ProductFilters(
    val category: String?,
    val priceRange: PriceRange?,
    val inStockOnly: Boolean = true
)

@Serializable
data class PriceRange(
    val min: Double,
    val max: Double
)
```

### Collections

```kotlin
@Serializable
data class MultiSelect(
    val options: List<String>,
    val preselected: Set<String> = emptySet()
) : NavigationKey.WithResult<Set<String>>
```

## Serialization Considerations

### Registering Custom Serializers

For polymorphic types, register serializers in your NavigationComponent:

```kotlin
@NavigationComponent
object AppNavigationComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        serializersModule(SerializersModule {
            polymorphic(Any::class) {
                subclass(CustomType::class)
                subclass(AnotherCustomType::class)
            }
        })
    }
)
```

### Non-Serializable Types

Some types can't be serialized (like `Context`, `ViewModel`, etc.). For these cases:

**Don't pass them in the NavigationKey:**

```kotlin
// ❌ BAD - Don't do this
@Serializable
data class BadScreen(
    val context: Context, // Can't serialize Context!
    val callback: () -> Unit // Can't serialize lambdas!
) : NavigationKey
```

**Instead, pass IDs or references:**

```kotlin
// ✅ GOOD - Pass identifiers
@Serializable
data class GoodScreen(
    val userId: String, // Pass an ID
    val action: UserAction // Pass a serializable enum
) : NavigationKey

@Serializable
enum class UserAction {
    VIEW, EDIT, DELETE
}
```

## NavigationKey.Instance

When you use a NavigationKey for navigation, Enro wraps it in a `NavigationKey.Instance`:

```kotlin
val key = UserProfile(userId = "123")
val instance = key.asInstance()

// Instance has:
instance.id          // Unique ID for this navigation instance
instance.key         // The original NavigationKey
instance.metadata    // Additional metadata (see below)
```

You typically don't create instances manually - Enro handles this automatically. But you may need
them when:

- Creating custom navigation operations
- Building initial backstacks for containers
- Implementing advanced navigation patterns

```kotlin
// Create initial backstack for a container
val container = rememberNavigationContainer(
    backstack = listOf(
        HomeScreen.asInstance(),
        ProductList(category = "electronics").asInstance()
    )
)
```

## NavigationKey.Metadata

Metadata allows attaching additional information to a navigation instance without changing the
NavigationKey:

```kotlin
// Define a custom metadata key
object IsFromDeepLink : NavigationKey.MetadataKey<Boolean>(default = false)

// Use it
val key = ProductDetail(productId = "123")
    .withMetadata(IsFromDeepLink, true)

navigation.open(key)

// Read it in the destination
val navigation = navigationHandle<ProductDetail>()
val isDeepLink = navigation.instance.metadata.get(IsFromDeepLink)
```

### Built-in Metadata

Enro uses metadata internally for:

- Scene configuration (dialogs, overlays)
- Result channel routing
- Animation preferences
- Custom destination behavior

You can define your own metadata keys for:

- Analytics tracking
- A/B test variants
- Feature flags
- Entry point tracking

### Metadata Keys

Create type-safe metadata keys:

```kotlin
// Boolean metadata
object SkipAnimation : NavigationKey.MetadataKey<Boolean>(default = false)

// String metadata
object AnalyticsSource : NavigationKey.MetadataKey<String>(default = "unknown")

// Custom type metadata (must be serializable)
object EntryContext : NavigationKey.MetadataKey<AppContext>(
    default = AppContext.NORMAL
)

@Serializable
enum class AppContext {
    NORMAL, DEEP_LINK, PUSH_NOTIFICATION
}
```

### Transient Metadata

For metadata that shouldn't persist across process death:

```kotlin
@AdvancedEnroApi
object TemporaryFlag : NavigationKey.TransientMetadataKey<Boolean>(default = false)
```

Transient metadata is not serialized and will be lost during state restoration.

## Naming Conventions

Choose a naming pattern and stick to it throughout your project:

### Option 1: Action-based

Names describe what the navigation does:

```kotlin
@Serializable
data class ShowUserProfile(val userId: String) : NavigationKey

@Serializable
data class SelectDate(val maxDate: LocalDate?) : NavigationKey.WithResult<LocalDate>

@Serializable
object EditSettings : NavigationKey
```

**Pros:** Reads naturally in code
**Cons:** Can feel verbose

### Option 2: Screen suffix

```kotlin
@Serializable
data class UserProfileScreen(val userId: String) : NavigationKey

@Serializable
data class DateSelectionScreen(val maxDate: LocalDate?) : NavigationKey.WithResult<LocalDate>

@Serializable
object SettingsScreen : NavigationKey
```

**Pros:** Clear that it's a screen
**Cons:** Repetitive suffix

### Option 3: Simple names

```kotlin
@Serializable
data class UserProfile(val userId: String) : NavigationKey

@Serializable
data class DatePicker(val maxDate: LocalDate?) : NavigationKey.WithResult<LocalDate>

@Serializable
object Settings : NavigationKey
```

**Pros:** Concise and clean
**Cons:** Can clash with domain models

Choose what works best for your team!

## Organization Patterns

### Feature-based

```kotlin
// features/profile/ProfileNavigation.kt
@Serializable
data class UserProfile(val userId: String) : NavigationKey

@Serializable
data class EditProfile(val userId: String) : NavigationKey

// features/settings/SettingsNavigation.kt
@Serializable
object Settings : NavigationKey

@Serializable
data class SettingsSection(val section: String) : NavigationKey
```

### Centralized

```kotlin
// navigation/NavigationKeys.kt
sealed interface AppNavigation {
    @Serializable
    data class UserProfile(val userId: String) : AppNavigation, NavigationKey
    
    @Serializable
    data class EditProfile(val userId: String) : AppNavigation, NavigationKey
    
    @Serializable
    object Settings : AppNavigation, NavigationKey
}
```

### Module-based (multimodule projects)

```kotlin
// :feature:profile module
@Serializable
data class UserProfile(val userId: String) : NavigationKey

// :feature:settings module  
@Serializable
object Settings : NavigationKey

// :app module aggregates them
val allNavigationKeys = listOf(
    UserProfile::class,
    Settings::class
)
```

## Best Practices

### Keep Keys Simple

NavigationKeys should be thin data holders:

```kotlin
// ✅ GOOD - Simple data holder
@Serializable
data class UserProfile(
    val userId: String,
    val tab: ProfileTab = ProfileTab.POSTS
) : NavigationKey

// ❌ BAD - Contains logic
@Serializable
data class UserProfile(
    val userId: String
) : NavigationKey {
    fun loadUser() { /* don't do this */ }
    val isValid: Boolean get() = /* don't do this */
}
```

### Use Value Objects

For complex parameters, use value objects:

```kotlin
// ✅ GOOD
@Serializable
@JvmInline
value class UserId(val value: String)

@Serializable
data class UserProfile(val userId: UserId) : NavigationKey
```

### Document Complex Keys

Use KDoc for keys with non-obvious parameters:

```kotlin
/**
 * Displays a filtered product list.
 *
 * @param categoryId The category to filter by, or null for all categories
 * @param sortOrder How to sort the results
 * @param showOutOfStock Whether to include out-of-stock items
 */
@Serializable
data class ProductList(
    val categoryId: String? = null,
    val sortOrder: SortOrder = SortOrder.POPULARITY,
    val showOutOfStock: Boolean = false
) : NavigationKey
```

### Avoid Mutable State

NavigationKeys should be immutable:

```kotlin
// ✅ GOOD - Immutable
@Serializable
data class Settings(val theme: Theme) : NavigationKey

// ❌ BAD - Mutable
@Serializable
data class Settings(var theme: Theme) : NavigationKey
```

## Common Patterns

### Wizard/Flow Keys

For multi-step flows:

```kotlin
@Serializable
sealed class OnboardingStep : NavigationKey {
    @Serializable
    object Welcome : OnboardingStep()
    
    @Serializable
    data class UserInfo(val email: String) : OnboardingStep()
    
    @Serializable
    data class Preferences(val email: String, val name: String) : OnboardingStep()
}
```

### Tab-based Navigation

```kotlin
@Serializable
data class MainScreen(
    val selectedTab: Tab = Tab.HOME
) : NavigationKey

@Serializable
enum class Tab {
    HOME, SEARCH, PROFILE
}
```

### Configuration Variants

```kotlin
@Serializable
data class ProductList(
    val mode: DisplayMode = DisplayMode.GRID
) : NavigationKey

@Serializable
enum class DisplayMode {
    GRID, LIST, COMPACT
}
```

## Next Steps

- [Navigation Destinations](navigation-destinations.md) - Learn how to implement screens
- [Navigation Operations](navigation-operations.md) - Understand how to navigate
- [Result Handling](result-handling.md) - Master returning results

---

**Questions?** Check the [FAQ](../faq.md) or open an issue
on [GitHub](https://github.com/isaac-udy/Enro).
