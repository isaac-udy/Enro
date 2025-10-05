---
title: Overview
has_children: true
nav_order: 1
---

# Enro

**A declarative, type-safe navigation library for Kotlin Multiplatform**

Enro is a powerful navigation library built on a simple idea: screens should behave like functions.
Define a contract with parameters and optional results, navigate declaratively, and let Enro handle
the complexity.

Currently supporting: **Android** • **iOS** (experimental) • **Desktop** (experimental) • **Web/WASM
** (experimental)

## Why Enro?

- **🔒 Type-Safe Navigation** - NavigationKeys define screens like function signatures, with
  compile-time safety for parameters and results
- **↩️ Result Handling** - First-class support for screens that return results, with type-safe
  result channels
- **🎨 Compose-First** - Built for Jetpack/Multiplatform Compose, with support for Fragments and
  Activities on Android
- **🌍 Multiplatform Ready** - Shared navigation logic across all platforms with Kotlin Multiplatform
- **🎬 Scene-Based Rendering** - Automatic handling of dialogs, overlays, and multi-pane layouts with
  navigation scenes, built on familiar AndroidX Navigation3 concepts
- **🔧 Flexible Architecture** - Powerful features like navigation flows, interceptors, decorators,
  and container-based navigation

## Getting Started

New to Enro? Start here:

1. **[Installation](docs/getting-started/installation.md)** - Set up Enro in your project
2. **[Basic Concepts](docs/getting-started/basic-concepts.md)** - Understand the core ideas

## Core Concepts

Master the fundamentals:

- **[Navigation Keys](docs/core-concepts/navigation-keys.md)** - Define contracts for your screens
- **[Navigation Destinations](docs/core-concepts/navigation-destinations.md)** - Implement screen UI
- **[Navigation Operations](docs/core-concepts/navigation-operations.md)** - Navigate between
  screens
- **[Result Handling](docs/core-concepts/result-handling.md)** - Return and receive results
  type-safely

## Advanced Topics

Dive deeper into Enro's capabilities:

- **Navigation Containers** - Build complex navigation hierarchies
- **Navigation Scenes** - Control how destinations are rendered
- **Navigation Flows** - Create multi-step wizards and flows
- **View Models** - Integrate with ViewModels
- **Animations & Transitions** - Customize navigation animations
- **Testing** - Test your navigation logic
- **Interceptors** - Modify navigation operations
- **Decorators** - Enhance destinations with additional behavior

## Platform-Specific Guides

- **Android** - Android-specific features and patterns
- **iOS** - iOS integration and setup
- **Desktop** - Desktop application navigation
- **Web** - Web/WASM support

## Quick Example

```kotlin
// 1. Define a NavigationKey
@Serializable
data class UserProfile(val userId: String) : NavigationKey

// 2. Create a destination
@Composable
@NavigationDestination(UserProfile::class)
fun UserProfileScreen() {
    val navigation = navigationHandle<UserProfile>()
    
    Text("User ID: ${navigation.key.userId}")
    
    Button(onClick = { navigation.close() }) {
        Text("Close")
    }
}

// 3. Navigate
@Composable
fun HomeScreen() {
    val navigation = navigationHandle()
    
    Button(onClick = {
        navigation.open(UserProfile(userId = "123"))
    }) {
        Text("Open Profile")
    }
}
```

## Applications Using Enro

<p>
    <a href="https://www.splitwise.com/">
        <img width="80px" src="./assets/images/splitwise-icon.png" alt="Splitwise" />
    </a>
    &nbsp;&nbsp;
    <a href="https://play.google.com/store/apps/details?id=com.beyondbudget">
        <img width="80px" src="./assets/images/beyond-budget-icon.png" alt="Beyond Budget" />
    </a>
    &nbsp;&nbsp;
    <a href="https://play.google.com/store/apps/details?id=com.xero.touch">
        <img width="80px" src="./assets/images/xero-logo.png" alt="Xero Accounting" />
    </a>
    &nbsp;&nbsp;
    <a href="https://play.google.com/store/apps/details?id=com.anz.lotus">
        <img width="80px" src="./assets/images/anz-plus.png" alt="ANZ Plus" />
    </a>
</p>

## Resources

- [GitHub Repository](https://github.com/isaac-udy/Enro)
- [Changelog](https://github.com/isaac-udy/Enro/blob/main/CHANGELOG.md)
- [Issue Tracker](https://github.com/isaac-udy/Enro/issues)
- [Maven Central](https://search.maven.org/search?q=g:%22dev.enro%22)

## Community & Support

- **Questions?** Check the [FAQ](docs/faq.md) or open
  a [GitHub Discussion](https://github.com/isaac-udy/Enro/discussions)
- **Found a bug?** Open an [issue](https://github.com/isaac-udy/Enro/issues)
- **Want to contribute?** See
  our [contribution guidelines](https://github.com/isaac-udy/Enro/blob/main/CONTRIBUTING.md)

## Migrating from 2.x

If you're using Enro 2.x, see the [Migration Guide](docs/migration-from-2x.md) for help upgrading to
3.x.

Key changes:

- NavigationKeys now use `@Serializable` instead of `@Parcelize`
- Navigation unified through `open()` instead of `push()`/`present()`
- Scene metadata determines rendering behavior
- `enro-compat` module available for easier migration

---

*"The novices' eyes followed the wriggling path up from the well as it swept a great meandering arc around the hillside. Its stones were green with moss and beset with weeds. Where the path disappeared through the gate they noticed that it joined a second track of bare earth, where the grass appeared to have been trampled so often that it ceased to grow. The dusty track ran straight from the gate to the well, marred only by a fresh set of sandal-prints that went down, and then up, and ended at the feet of the young monk who had fetched their water." - [The Garden Path](http://thecodelesscode.com/case/156)* 