---
title: Android
parent: Platform-Specific Guides
nav_order: 1
---

# Android

Enro on Android uses the same Compose runtime as iOS, Desktop and Web.
`enro-runtime` also supports Fragment and Activity destinations on
Android. The `enro-compat` module provides the Enro 2 API for apps that
are migrating to Enro 3.

## Installation

Call `installNavigationController` from `Application.onCreate`:

<!-- snippet: android/install -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { /* optional config */ }
)

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
```

`installNavigationController(this)` attaches the controller to the
`Application` instance. The `Application` class does not implement an
Enro interface or declare a controller property.

## Hosting the backstack

Create one root container in the Activity's Compose content and pass the
container to `NavigationDisplay`:

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

A composable inside `NavigationDisplay` opens any destination annotated
with `@NavigationDestination(...)` through the composable's navigation
handle.

## Migrating an existing Android app — `enro-compat`

The `enro-compat` module provides the Enro 2 API on top of Enro 3. An
Enro 2 app that depends on `enro-compat` keeps its Fragment and Activity
destinations and adds Composable destinations one screen at a time.

<!-- snippet: skip (gradle) -->
```kotlin
dependencies {
    implementation("dev.enro:enro:3.0.0")
    implementation("dev.enro:enro-compat:3.0.0")
    ksp("dev.enro:enro-processor:3.0.0")
}
```

When `enro-compat` is on the classpath, the controller registers
`compatModule` during initialisation. The app does not install
`compatModule`. `compatModule` contains:

- Serializers for the Enro 2 `NavigationDirection.Push` and
  `NavigationDirection.Present` directions, so a backstack that contains
  an Enro 2 direction can be saved and restored.
- `LegacyNavigationDirectionPlugin`, which converts Enro 2 direction
  metadata into Enro 3 metadata at runtime.

`enro-compat` also contains the Enro 2 extension functions for Fragments
and Activities (`registerForNavigationResult`, `getNavigationHandle`,
`addOpenInstruction` and others), so Enro 2 Fragment and Activity code
compiles without changes.

### Annotating a Fragment

<!-- snippet: android/fragment -->
```kotlin
@NavigationDestination(ShowProfile::class)
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val navigation by navigationHandle<ShowProfile>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.title).text =
            "Profile for ${navigation.key.userId}"
    }
}
```

`by navigationHandle<T>()` returns the same `NavigationHandle<T>` type in
a Fragment as in a Composable or a ViewModel. The key, the operations
(`open`, `close`, `complete`) and the result channels behave the same.

### Annotating an Activity

<!-- snippet: android/activity -->
```kotlin
@NavigationDestination(EditProfile::class)
class EditProfileActivity : AppCompatActivity() {
    private val navigation by navigationHandle<EditProfile>()
    // ...
}
```

A Composable destination opens an Activity destination with
`navigation.open(EditProfile(...))`. An Activity or Fragment destination
opens a Composable destination the same way.

### Returning a result from an Activity

An Activity bound to a `NavigationKey.WithResult<T>` key returns a result
with `navigation.complete(value)`. `complete` finishes the Activity, and
the caller's `registerForNavigationResult` receives the value. The
Activity must be declared in the manifest.

<!-- snippet: android/activity-result -->
```kotlin
// Declared in the recipes app's AndroidManifest.xml.
@NavigationDestination(PickNameActivityKey::class)
class PickNameActivity : ComponentActivity() {
    private val navigation by navigationHandle<PickNameActivityKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Pick a name, in an Activity", style = MaterialTheme.typography.titleLarge)
                        listOf("Ada", "Grace").forEach { name ->
                            // Finishes the Activity and delivers the result to the caller.
                            Button(onClick = { navigation.complete(name) }) { Text(name) }
                        }
                        Button(onClick = { navigation.close() }) { Text("Cancel") }
                    }
                }
            }
        }
    }
}
```

The [Fragment and Activity recipe][fragment-activity-recipe] shows a
Fragment destination and `PickNameActivity` in one container with
Composable destinations.

### Bridging individual destinations

Convert one screen at a time, starting with the screens that open no
other screen:

1. Change the screen's `NavigationKey` to the `@Serializable` form: remove
   `@Parcelize` and the `SupportsPush` / `SupportsPresent` marker. See the
   [migration guide](../migrating-from-v2.md).
2. Write the screen as a `@Composable` destination annotated with
   `@NavigationDestination(KeyClass::class)`.
3. Leave the callers unchanged. Callers open the same key.
4. Repeat for the next screen.

An app with some screens converted can be released. A Fragment or
Activity that has not been converted keeps its `@NavigationDestination`
annotation and is still opened by its key.

## Notes

- **Activity context**: Enro does not replace or wrap
  `LocalContext.current`.
- **Predictive back**: `NavigationDisplay` enables predictive back for
  Compose destinations by default. See
  [Animations → Predictive back](../advanced/animations.md#predictive-back).
- **Process death**: `rememberNavigationContainer` saves the container's
  backstack with `rememberSaveable`, and the backstack is restored when
  the process is recreated. To test the restore, enable "Don't keep
  activities" in Developer Options.
- **Multiple Activities**: each Activity hosts its own root container.

## See also

- [Installation](../getting-started/installation.md) — installation on every platform.
- [Migrating from Enro 2](../migrating-from-v2.md) — the API changes between Enro 2 and Enro 3.
- [Recipes][recipes] — every recipe runs on Android. The
  [interop recipe][interop-recipe] renders a native `AndroidView` inside
  an Enro destination.

[recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes
[fragment-activity-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/androidMain/kotlin/dev/enro/recipes/interop/FragmentAndActivity.kt
[interop-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/interop/NativeInterop.kt
