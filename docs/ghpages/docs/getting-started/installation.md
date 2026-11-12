---
title: Installation
parent: Getting Started
nav_order: 1
---

# Installation

Enro is published to [Maven Central](https://search.maven.org/). The project
must include the `mavenCentral()` repository.

## Dependencies

<!-- snippet: skip (gradle) -->
```kotlin
plugins {
    id("com.google.devtools.ksp") version "<your-ksp-version>"
}

dependencies {
    implementation("dev.enro:enro:3.0.0")

    // Generates installNavigationController and a binding for each
    // @NavigationDestination.
    ksp("dev.enro:enro-processor:3.0.0")

    // Optional.
    testImplementation("dev.enro:enro-test:3.0.0")
}
```

Enro 3.0.0 requires Kotlin 2.4 and Compose Multiplatform 1.12. On Android,
Enro 3.0.0 also requires AGP 9.1.0 or later and compileSdk 37.

In a Kotlin Multiplatform project, add `enro` to the `commonMain` source set
and add the `enro-processor` KSP dependency for each target:

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

The configuration name is `ksp` followed by the target name. Omit each target
that the project does not build.

An app that still calls the Enro 2 API also requires the `enro-compat` module:

<!-- snippet: skip (gradle) -->
```kotlin
dependencies {
    implementation("dev.enro:enro-compat:3.0.0")
}
```

## Declare a NavigationComponent

A `NavigationComponent` configures Enro for an application. Declare the
`NavigationComponent` as an `object` that extends
`NavigationComponentConfiguration` and is annotated with
`@NavigationComponent`. KSP generates an `installNavigationController`
extension on the `object` for each target.

<!-- snippet: installation/component -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        // Optional: plugins, interceptors, decorators, serializers,
        // and modules from other libraries.
    }
)
```

The `module` block can be empty. The KSP processor registers each destination
that is annotated with `@NavigationDestination`.

In a multiplatform project, one `NavigationComponent` declared in the common
source set is available to every target.

## Install Enro on each platform

### Android

Call `installNavigationController` from `Application.onCreate`:

<!-- snippet: your-first-screen/application -->
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
```

Call `rememberNavigationContainer` and `NavigationDisplay` from the root
Composable of an `Activity`:

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

The [Android platform guide](../platform/android.md) covers Fragment and
Activity destinations and the `enro-compat` module.

### iOS

Declare a function that returns an `EnroUIViewController`. The Swift app
displays the `EnroUIViewController`.

<!-- snippet: installation/ios -->
```kotlin
fun MainViewController(): UIViewController = EnroUIViewController {
    val container = rememberNavigationContainer(
        backstack = backstackOf(Home.asInstance()),
    )
    NavigationDisplay(state = container)
}
```

Call `installNavigationController` once when the iOS app starts. The
[iOS platform guide](../platform/ios.md) shows the install call and the Swift
code that displays the view controller.

### Desktop

`installNavigationController(Unit)` returns an `EnroController`. Open a window
with `EnroController.openWindow` and render the open windows with
`EnroApplicationContent`:

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

The [Desktop platform guide](../platform/desktop.md) covers window
configuration. The [recipes desktop main][recipes-desktop] is a complete
example.

### Web (WasmJS and Kotlin/JS)

Call `installNavigationController(document)` from `main`, then render the
container inside `EnroBrowserContent`:

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

`InstallWebHistoryPlugin` connects the container's backstack to the browser
history and URL. See the [Web platform guide](../platform/web.md).

## Next steps

- [Basic Concepts](basic-concepts.md) defines each Enro term.
- [Your First Screen](your-first-screen.md) builds an example with three screens.
- The [migration guide](../migrating-from-v2.md) covers upgrading an Enro 2 app.

[recipes-desktop]: https://github.com/isaac-udy/Enro/blob/main/recipes/app/desktop/src/desktopMain/kotlin/main.kt
