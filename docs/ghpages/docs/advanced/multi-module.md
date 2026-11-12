---
title: Multi-Module Projects
parent: Advanced Topics
nav_order: 12
---

# Multi-module projects

A feature module can open a screen from another feature module without a
dependency on that module. A screen is opened by `NavigationKey`, so the
calling module depends on the module that declares the key and not on the
module that declares the destination.

## Module layout

| Module | Contains | Depends on |
|---|---|---|
| navigation | `NavigationKey`s | `enro-common` |
| each feature | destinations for some of the keys | navigation, `enro`, the `enro-processor` KSP dependency |
| app | the `@NavigationComponent` | every feature |

Feature modules do not depend on each other.

## Keys

Declare each key that is used by more than one module in a separate
navigation module. A key is a serializable class, so the navigation module
requires only `enro-common` and the serialization plugin. The navigation
module does not require Compose or the `enro-processor`.

<!-- snippet: multi-module/keys -->
```kotlin
/** Implemented in :recipes:modular:feature-home. */
@Serializable
data object HomeKey : NavigationKey

/** Implemented in :recipes:modular:feature-profile. */
@Serializable
data class ProfileKey(val userId: String) : NavigationKey
```

A key that is used by one feature only can be declared in that feature's
module.

## Destinations

A feature module declares a destination with `@NavigationDestination`, as
in a single-module project:

<!-- snippet: multi-module/destination -->
```kotlin
@Composable
@NavigationDestination(ProfileKey::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ProfileKey>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Profile: ${navigation.key.userId}", style = MaterialTheme.typography.titleLarge)
        Text("In :recipes:modular:feature-profile", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
```

Add the `enro-processor` KSP dependency to every module that declares a
`@NavigationDestination`. The `enro-processor` generates a binding for each
`@NavigationDestination`.

<!-- snippet: skip (gradle) -->
```kotlin
dependencies {
    val enroProcessor = "dev.enro:enro-processor:3.0.0"
    add("kspCommonMainMetadata", enroProcessor)
    add("kspAndroid", enroProcessor)
    // ... one line per target
}
```

Another feature opens the screen with `navigation.open(ProfileKey("user-123"))`.
The calling feature does not import `ProfileScreen`.

## The app module

Declare the `@NavigationComponent` in the app module. The app module must
depend on every feature module. The `enro-processor` finds each feature's
generated bindings on the classpath and registers the bindings with the
`@NavigationComponent`. The app module does not list the bindings.

If a destination is missing at runtime, check that the destination's module
has the `enro-processor` KSP dependency and that the app module depends on
the destination's module.

## See also

- [Modular Navigation recipe][modular-recipe] — three Gradle modules that
  follow the module layout on this page.
- [Navigation Keys](../core-concepts/navigation-keys.md).
- [Troubleshooting](../troubleshooting.md).

[modular-recipe]: https://github.com/isaac-udy/Enro/tree/main/recipes/modular
