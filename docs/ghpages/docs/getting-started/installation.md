---
title: Installation
parent: Getting Started
nav_order: 1
---

# Installation

Enro is available through Maven Central. Add the following dependencies to your project:

## Gradle (Kotlin DSL)

```kotlin
dependencies {
    // Core library
    implementation("dev.enro:enro:2.8.3")
    
    // Annotation processor (choose one)
    ksp("dev.enro:enro-processor:2.8.3")
    // If using KAPT (deprecated, prefer KSP)
    // kapt("dev.enro:enro-processor:2.8.3") 
    
    // Optional test utilities
    testImplementation("dev.enro:enro-test:2.8.3")
}
```

## Platform Setup

### Common
In your application module, define a "NavigationComponent". This is a class that extends `NavigationComponentConfiguration` and is annotated with `@NavigationComponent`.

When Enro runs code generation, it uses this `NavigationComponent` to generate code that allows Enro to be installed into your application. In a multi-platform project, you can declare the `NavigationComponent` in the common source set, or declare one in each platform source set. Android applications may also annotate their Application class with `@NavigationComponent` instead of using a `NavigationComponentConfiguration` class.

```kotlin
@NavigationComponent
class ExampleNavigationComponent : NavigationComponentConfiguration()
```

### Android
To install Enro into an Android application: 
1. Implement `NavigationApplication` on your Application class
2. Override the `navigationController` property, and call `installNavigationController` on your NavigationComponent.
3. Optionally, you can pass a lambda to the `installNavigationController` function to set additional configuration

```kotlin
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = ExampleNavigationComponent.installNavigationController(this) {
        // optional additional configuration
    }
}
```

If you are building an Android-only application, you can annotate your Application class with `@NavigationComponent` instead of declaring a separate NavigationComponent class:
1. Annotate your Application class with `@NavigationComponent`
2. Implement `NavigationApplication` on your Application class
3. Override the `navigationController` property, and call `installNavigationController`.
4. Optionally, you can pass a lambda to the `installNavigationController` function to set additional configuration

```kotlin
@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = installNavigationController(this) {
        // optional additional configuration
    }
}
```

### iOS

Initialize Enro in your iOS application's AppDelegate:

```swift
@main
class ExampleAppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        EnroComponent.shared.installNavigationController(
            application: application,
            root: // Provide a Root NavigationInstruction here
            strictMode: false,
            useLegacyContainerPresentBehavior: false,
            backConfiguration: Enro.shared.backConfiguration.Default,
            block: { scope in
                // Add any additional configuration here
            }
        )
        return true
    }
}
```

### Desktop

Initialize Enro in your desktop application's main function:

```kotlin
fun main() = application {
    val controller = EnroComponent.rememberNavigationController(
        root = // Provide a Root NavigationInstruction here
    ) {
        // Add any additional configuration here
    }
    controller.windowManager.Render()
}
```

### Web

Initialize Enro in your web application's entry point:

```kotlin
fun main() {
    val controller = EnroComponent.installNavigationController(
        document = document,
        root = // Provide a Root NavigationInstruction here
    ) {
        // Add any additional configuration here
    }

    EnroViewport(
        controller = controller,
    )
}
```
