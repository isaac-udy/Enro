---
title: Installation
parent: Getting Started
nav_order: 1
---

# Installation

Enro is available through Maven Central. This guide will walk you through installing Enro in your
project and setting it up for your target platform(s).

## Gradle Dependencies

Add the following dependencies to your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Core library
    implementation("dev.enro:enro:3.0.0-alpha05")
    
    // Annotation processor (KSP is recommended)
    ksp("dev.enro:enro-processor:3.0.0-alpha05")
    
    // Optional: Compatibility layer for migrating from 2.x (Android only)
    // implementation("dev.enro:enro-compat:3.0.0-alpha05")
    
    // Optional: Test utilities
    testImplementation("dev.enro:enro-test:3.0.0-alpha05")
}
```

> **Note:** Make sure your project includes the `mavenCentral()` repository in your
`settings.gradle.kts` or root `build.gradle.kts`.

### Using KSP

Enro uses Kotlin Symbol Processing (KSP) for code generation. Make sure you have the KSP plugin
applied in your module:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}
```

KAPT is also supported but KSP is recommended for better build performance.

## Platform Setup

### Creating a NavigationComponent

In all platforms, you'll need to define a `NavigationComponent`. This is a class or object that
extends `NavigationComponentConfiguration` and is annotated with `@NavigationComponent`. The
annotation processor uses this to generate installation code.

You can define the `NavigationComponent` in your common source set for multiplatform projects, or in
platform-specific source sets:

```kotlin
@NavigationComponent
object MyNavigationComponent : NavigationComponentConfiguration()
```

For more advanced configuration, you can pass a `NavigationModule` to configure serializers,
interceptors, and more:

```kotlin
@NavigationComponent
object MyNavigationComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        // Add custom serializers
        serializersModule(SerializersModule {
            polymorphic(Any::class) {
                subclass(MyCustomType::class)
            }
        })
        
        // Add interceptors, decorators, etc.
    }
)
```

> **Important:** After creating your `NavigationComponent`, you need to build/compile your project
once before the `installNavigationController` function becomes available. This function is generated
by the annotation processor.

### Android

For Android applications, install Enro in your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MyNavigationComponent.installNavigationController(this)
    }
}
```

#### Fragment and Activity Support

If you're using Fragments or Activities, include the `enro-compat` module:

```kotlin
dependencies {
    implementation("dev.enro:enro-compat:3.0.0-alpha05")
}
```

The compat module provides support for Fragment and Activity destinations, as well as typealiases to
help with migration from Enro 2.x.

### iOS

> **Note:** iOS support is currently experimental.

Initialize Enro in your iOS application's entry point. For SwiftUI apps:

```swift
import SwiftUI
import EnroTestsApplication // Your shared module

@main
struct MyApp: App {
    init() {
        MyNavigationComponent.shared.installNavigationController(
            application: UIApplication.shared,
            root: RootScreen().asInstance(),
            strictMode: false
        ) { scope in
            // Optional: Add additional configuration
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

For UIKit-based apps using an `AppDelegate`:

```swift
@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        MyNavigationComponent.shared.installNavigationController(
            application: application,
            root: RootScreen().asInstance()
        ) { scope in
            // Optional: Add additional configuration
        }
        return true
    }
}
```

### Desktop (JVM)

> **Note:** Desktop support is currently experimental.

Initialize Enro in your desktop application's main function:

```kotlin
fun main() = application {
    val controller = MyNavigationComponent.installNavigationController(Unit) {
        // Optional: Add additional configuration
    }
    
    // Use Enro's window manager for rendering
    controller.windowManager.Render()
}
```

For more control over windows, you can create your own window structure:

```kotlin
fun main() = application {
    val controller = MyNavigationComponent.installNavigationController(Unit)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "My Application"
    ) {
        // Your navigation content here
        val container = rememberNavigationContainer(
            backstack = listOf(HomeScreen().asInstance()),
            emptyBehavior = EmptyBehavior.closeParent()
        )
        NavigationDisplay(container)
    }
}
```

### Web (WASM)

> **Note:** Web/WASM support is currently experimental.

Initialize Enro in your web application's entry point:

```kotlin
fun main() {
    val controller = MyNavigationComponent.installNavigationController(
        root = HomeScreen().asInstance()
    ) {
        // Optional: Add additional configuration
    }
    
    // Render your navigation viewport
    // This typically involves setting up a ComposeViewport or similar
}
```

## Verification

After installation, verify your setup by creating a simple NavigationKey and destination:

```kotlin
// 1. Define a NavigationKey
@Serializable
data class HomeScreen(val title: String = "Home") : NavigationKey

// 2. Create a destination
@Composable
@NavigationDestination(HomeScreen::class)
fun HomeScreenDestination() {
    val navigation = navigationHandle<HomeScreen>()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Arrangement.Center
    ) {
        Text("Welcome to ${navigation.key.title}!")
    }
}
```

Build your project to generate the navigation bindings, then navigate to your home screen.

## Troubleshooting

### "Unresolved reference: installNavigationController"

This means the annotation processor hasn't generated the installation function yet. Solutions:

1. Make sure you've added the `@NavigationComponent` annotation
2. Build/compile your project once
3. Verify KSP is properly configured
4. Check for any compilation errors that might prevent code generation

### "No NavigationBinding found for [YourNavigationKey]"

This error occurs when you try to navigate to a NavigationKey that doesn't have a corresponding
destination. Solutions:

1. Make sure you've annotated your destination with `@NavigationDestination`
2. Build/compile your project to generate the binding
3. Verify the NavigationKey class reference in the annotation matches your key

## Next Steps

- [Basic Concepts](basic-concepts.md) - Learn the fundamental concepts of Enro
- [Navigation Keys](../core-concepts/navigation-keys.md) - Deep dive into NavigationKeys
- [Navigation Destinations](../core-concepts/navigation-destinations.md) - Learn how to create
  destinations

---

**Need help?** Check the [FAQ](../faq.md) or open an issue
on [GitHub](https://github.com/isaac-udy/Enro).
