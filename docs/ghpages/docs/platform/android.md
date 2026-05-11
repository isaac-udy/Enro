---
title: Android
parent: Platform-Specific Guides
nav_order: 1
---

# Android

Enro on Android is the same Compose-first runtime you'd use on iOS, Desktop
or Web, plus a compatibility module — `enro-compat` — that lets you keep
existing Fragments and Activities working while you adopt the new API.

## Installation

Install in your `Application.onCreate`:

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

The `installNavigationController(this)` call is what attaches the
controller to your application instance — no `NavigationApplication`
interface to implement, no property to expose.

## Hosting the backstack

The typical pattern is one root container at the top of your Compose tree:

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

That's it. From here, every destination annotated with
`@NavigationDestination(...)` is reachable through the navigation handle on
any composable in the tree.

## Migrating an existing Android app — `enro-compat`

If you have an existing app with Fragments or Activities, the
`enro-compat` module lets you adopt Enro 3 incrementally. Existing
Fragment- and Activity-backed destinations keep working alongside new
Composable destinations; you can migrate screens one at a time.

```kotlin
dependencies {
    implementation("dev.enro:enro:3.0.0-alpha10")
    implementation("dev.enro:enro-compat:3.0.0-alpha10")
    ksp("dev.enro:enro-processor:3.0.0-alpha10")
}
```

When `enro-compat` is on the classpath, Enro automatically registers a
`compatModule` that provides:

- Serialization support for legacy `NavigationDirection.Push` /
  `NavigationDirection.Present` directions, so backstacks built from old
  call sites still serialize correctly.
- `LegacyNavigationDirectionPlugin`, which translates legacy direction
  metadata at runtime into the new metadata model.
- Extension helpers for Fragment and Activity (`registerForNavigationResult`,
  `getNavigationHandle`, `addOpenInstruction`, etc.) so existing Fragment-
  and Activity-style code compiles unchanged.

You don't need to install the compat module explicitly — its presence on
the classpath is detected during controller initialisation.

### Annotating a Fragment

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

The handle delegate is the same one Composables and ViewModels use; the
key, operations (`open`, `close`, `complete`), and result channels all
behave the same way.

### Annotating an Activity

```kotlin
@NavigationDestination(EditProfile::class)
class EditProfileActivity : AppCompatActivity() {
    private val navigation by navigationHandle<EditProfile>()
    // ...
}
```

Activities and Fragments live in the same backstack as Composable
destinations, so a Composable can `navigation.open(EditProfile(...))` and
have an Activity launched — and vice versa.

### Bridging individual destinations

A common migration pattern is to convert one screen at a time, starting
with leaves of the navigation graph:

1. Identify a screen and copy its `NavigationKey` to the new
   `@Serializable` form (drop `@Parcelize`, drop the `SupportsPush` /
   `SupportsPresent` marker — see the [migration guide](../migrating-from-v2.md)).
2. Re-implement that screen as a `@Composable` destination annotated with
   `@NavigationDestination(KeyClass::class)`.
3. Existing callers continue to use the same key; the call sites don't
   need to change.
4. Repeat for the next screen.

You can ship a half-migrated app. Just leave the not-yet-converted
Fragments and Activities annotated as they are; `enro-compat` keeps them
addressable.

## Notes

- **Activity context** is available wherever Compose's
  `LocalContext.current` works — Enro doesn't replace or wrap it.
- **Predictive back** is on by default for Compose destinations rendered
  by `NavigationDisplay`. See
  [Animations → Predictive back](../advanced/animations.md#predictive-back).
- **Process death** is handled automatically. Your container's backstack
  is saved to `SavedStateHandle` and restored when the process is
  recreated. Test it with "Don't keep activities" in Developer Options.
- **Multi-Activity layouts** are supported via `enro-compat` — each
  Activity hosts its own root container.

## See also

- [Installation](../getting-started/installation.md) — covers the multi-platform install side.
- [Migrating from Enro 2](../migrating-from-v2.md) — what changes when you bring a v2 app forward.
- [Recipes][recipes] — every recipe runs on Android out of the box; the
  [interop recipe][interop-recipe] demonstrates a native `AndroidView`
  inside an Enro destination.

[recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/src/commonMain/kotlin/dev/enro/recipes
[interop-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/interop/NativeInterop.kt
