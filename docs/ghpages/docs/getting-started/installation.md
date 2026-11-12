---
title: Installation
parent: Getting Started
nav_order: 1
---

# Installation

Enro is published to [Maven Central](https://search.maven.org/). Make sure your
project includes the `mavenCentral()` repository, then add the dependencies for
your platform below.

## Dependencies

<!-- snippet: skip (gradle) -->
```kotlin
plugins {
    id("com.google.devtools.ksp") version "<your-ksp-version>"
}

dependencies {
    // Core library
    implementation("dev.enro:enro:3.0.0")

    // KSP processor — generates the install function for your NavigationComponent
    // and discovers @NavigationDestination annotations
    ksp("dev.enro:enro-processor:3.0.0")

    // Optional: test utilities
    testImplementation("dev.enro:enro-test:3.0.0")
}
```

Enro 3.0.0 requires Kotlin 2.4, Compose Multiplatform 1.12, and on Android,
AGP 9.1.0 or later with compileSdk 37.

In a Kotlin Multiplatform project, add `enro` to your `commonMain` source set,
and add the `enro-processor` KSP dependency for each target you build:

<!-- snippet: skip (gradle) -->
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.enro:enro:3.0.0")
        }
    }
}

dependencies {
    val enroProcessor = "dev.enro:enro-processor:3.0.0"
    add("kspCommonMainMetadata", enroProcessor)
    add("kspAndroid", enroProcessor)
    add("kspDesktop", enroProcessor)         // a jvm("desktop") target
    add("kspIosArm64", enroProcessor)
    add("kspIosSimulatorArm64", enroProcessor)
    add("kspWasmJs", enroProcessor)
}
```

The configuration name is `ksp` followed by the target name. Leave out the
targets you don't build.

If you have an existing Android app that uses Fragments or Activities and want
to adopt Enro incrementally, also add the compatibility module:

<!-- snippet: skip (gradle) -->
```kotlin
dependencies {
    implementation("dev.enro:enro-compat:3.0.0")
}
```

## Declare a NavigationComponent

A `NavigationComponent` is the entry point for Enro into your application.
Declare one as an `object` extending `NavigationComponentConfiguration`,
annotated with `@NavigationComponent`. KSP will generate an
`installNavigationController` extension on that object for each platform you
target.

<!-- snippet: installation/component -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        // Optional configuration:
        // plugins, interceptors, decorators, custom serializers,
        // additional modules from other libraries, etc.
    }
)
```

The `module` block is where you compose extra configuration. You rarely need
anything in it to start — destinations are discovered automatically through
the `@NavigationDestination` annotations on your screens.

In a multi-platform project, you can declare one `NavigationComponent` in the
common source set and use it from every target.

## Install Enro on each platform

### Android

Call `installNavigationController` from your `Application.onCreate`:

<!-- snippet: your-first-screen/application -->
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
```

Then host a backstack from any `Activity` or Composable. The typical pattern is
to call `rememberNavigationContainer` and `NavigationDisplay` from your root
Composable:

<!-- snippet: your-first-screen/activity -->
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            NavigationDisplay(state = container)
        }
    }
}
```

If you're migrating from Fragments or Activities, see the [Android platform
guide](../platform/android.md) and add `enro-compat` to keep your existing
screens working.

### iOS

On iOS, install the controller during app startup and expose a Composable view
controller for Swift to display.

<!-- snippet: installation/ios -->
```kotlin
fun MainViewController(): UIViewController = EnroUIViewController {
    val container = rememberNavigationContainer(
        backstack = backstackOf(Home.asInstance()),
    )
    NavigationDisplay(state = container)
}
```

The `installNavigationController` call for iOS is typically made once at app
startup; see the [iOS platform guide](../platform/ios.md) for the Swift
boilerplate to bridge the generated view controller into your app.

### Desktop

Call `installNavigationController(Unit)` to get an `EnroController`, then drive
your windows through it:

<!-- snippet: installation/desktop -->
```kotlin
fun main() {
    val controller = MyComponent.installNavigationController(Unit)
    controller.openWindow(
        GenericRootWindow(
            windowConfiguration = {
                RootWindow.WindowConfiguration(
                    title = "My App",
                    onCloseRequest = { navigation.close() },
                )
            },
        ) {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            NavigationDisplay(state = container)
        },
    )

    application {
        EnroApplicationContent(controller)
    }
}
```

See the [Desktop platform guide](../platform/desktop.md) for the full window
configuration story and the [recipes desktop main][recipes-desktop] for a
complete working example.

### Web (WasmJS and Kotlin/JS)

Call `installNavigationController(document)` from your `main`, then render the
backstack inside an `EnroBrowserContent`:

<!-- snippet: installation/web -->
```kotlin
fun main() {
    MyComponent.installNavigationController(document)

    ComposeViewport {
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            InstallWebHistoryPlugin(container)
            NavigationDisplay(state = container)
        }
    }
}
```

`InstallWebHistoryPlugin` ties your container to browser history so the back
button and URL bar behave as users expect. See the
[Web platform guide](../platform/web.md) for more.

## Next steps

- Read [Basic Concepts](basic-concepts.md) for the short vocabulary tour.
- Walk through [Your First Screen](your-first-screen.md) for an end-to-end example.
- If you're upgrading an existing app, read the [migration guide](../migrating-from-v2.md).

[recipes-desktop]: https://github.com/isaac-udy/Enro/blob/main/recipes/app/desktop/src/desktopMain/kotlin/main.kt
