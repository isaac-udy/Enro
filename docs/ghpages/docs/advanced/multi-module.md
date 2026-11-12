---
title: Multi-Module Projects
parent: Advanced Topics
nav_order: 12
---

# Multi-module projects

Feature modules can open each other's screens without depending on each
other. A screen is opened by its `NavigationKey`, so a feature only needs to
see the key, not the destination behind it.

## Module layout

| Module | Contains | Depends on |
|---|---|---|
| navigation | `NavigationKey`s | `enro-common` |
| each feature | destinations for some of the keys | navigation, `enro`, the `enro-processor` KSP dependency |
| app | the `@NavigationComponent` | every feature |

Feature modules do not depend on each other.

## Keys

Put the keys that cross module boundaries in a module of their own. Keys
are plain serializable classes, so this module needs only `enro-common` and
the serialization plugin. It has no Compose and no annotation processing.

<!-- snippet: multi-module/keys -->
```kotlin
/** Implemented in :recipes:modular:feature-home. */
@Serializable
data object HomeKey : NavigationKey

/** Implemented in :recipes:modular:feature-profile. */
@Serializable
data class ProfileKey(val userId: String) : NavigationKey
```

A key used only inside one feature can stay in that feature.

## Destinations

A feature module declares destinations for its keys the usual way:

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

Apply the `enro-processor` KSP dependency in every module that has a
`@NavigationDestination`. The processor generates a binding for each one.

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
It does not import `ProfileScreen`.

## The app module

Declare the `@NavigationComponent` in the app module and make that module
depend on every feature module. The processor finds the features' generated
bindings on the classpath and registers them with the component. There is
nothing to list by hand.

If a destination is missing at runtime, check that its module applies the
processor and that the app module depends on it.

## See also

- [Modular Navigation recipe][modular-recipe] — three Gradle modules laid out
  as above.
- [Navigation Keys](../core-concepts/navigation-keys.md).
- [Troubleshooting](../troubleshooting.md).

[modular-recipe]: https://github.com/isaac-udy/Enro/tree/main/recipes/modular
