---
title: Navigation Keys
parent: Core Concepts
nav_order: 1
---

# Navigation Keys

A `NavigationKey` is the contract for a screen. The properties of the key
are the inputs to the screen. A key for a screen that returns a result
implements `NavigationKey.WithResult<T>`.

A key is a serializable value. A caller opens a screen by passing the
screen's key, and does not reference the screen's implementation.

<!-- snippet: navigation-keys/show-profile -->
```kotlin
@Serializable
data class ShowProfile(val userId: String) : NavigationKey
```

`ShowProfile` is equivalent to the function signature
`fun showProfile(userId: String): Unit`.

<!-- snippet: navigation-keys/select-date -->
```kotlin
@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

`SelectDate` is equivalent to the function signature
`fun selectDate(minDate: LocalDate? = null, maxDate: LocalDate? = null): LocalDate`.

## Serialization

A key must be annotated with `@Serializable` (kotlinx.serialization). Enro
serializes keys to save and restore a container's backstack, including
across process death, and to pass a key to an Android Activity or Fragment
in a `Bundle`.

Each property of a key requires a serializer. Built-in Kotlin types
(`String`, `Int`, `Boolean`, `List<T>`, `Map<K, V>`, nullable types,
`kotlin.uuid.Uuid`) require no configuration. Annotate your own types with
`@Serializable`:

<!-- snippet: navigation-keys/domain-type -->
```kotlin
@Serializable
data class UserId(val value: String)

@Serializable
data class ShowProfile(val userId: UserId) : NavigationKey
```

For types from other libraries, and for polymorphic types, register a
`SerializersModule` on the `NavigationComponent`:

<!-- snippet: navigation-keys/serializers-module -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        serializersModule(kotlinx.serialization.modules.SerializersModule {
            // contextual / polymorphic registrations
        })
    }
)
```

## Keys with results

A key that implements `NavigationKey.WithResult<T>` declares that the screen
returns a value of type `T` to the caller. The screen returns the value by
calling `navigation.complete(value)`. The caller receives the value in a
`registerForNavigationResult` channel. See [Results](../advanced/results.md).

<!-- snippet: navigation-keys/with-result -->
```kotlin
@Serializable
data class ConfirmDelete(val itemName: String) : NavigationKey.WithResult<Boolean>
```

`complete` requires a value only when the key implements `WithResult`. A
screen with either kind of key can call `close` and `complete`. See
[Closing vs completing](navigation-handles.md#closing-vs-completing).

## NavigationKey.Instance

A backstack can contain the same key more than once. Enro wraps each key in
a `NavigationKey.Instance` when the key is added to a backstack. Each
`Instance` has a unique `id` and separate `metadata`.

Application code creates an `Instance` to build an initial backstack:

<!-- snippet: navigation-keys/instance -->
```kotlin
val initial = backstackOf(
    Home.asInstance(),
    ShowProfile("user-123").asInstance(),
)
```

`asInstance()` wraps a key in an `Instance` with a new `id`. Two `Instance`
values are equal only when the two `id`s are equal, so a
`Set<NavigationKey.Instance<*>>` keeps two instances of the same key.

## Instance metadata

`NavigationKey.Metadata` is a typed, serializable key-value store. Each
`Instance` has one `Metadata`. Instance metadata stores data about one
`Instance` of a key that is not part of the key's contract.

Examples of instance metadata:

- The screen coordinates of the click that opened the destination, as the
  origin of an animation.
- An analytics correlation id assigned when the instance was created.
- The time the instance was opened.
- The feature flow that opened the destination.

Instance metadata is optional for every key and does not change how the
destination is rendered.

> **Instance metadata is not destination metadata.** A
> `navigationDestination(metadata = { dialog() }) { ... }` block declares
> destination metadata, which a scene strategy reads to decide how to
> render the destination. See
> [Navigation Destinations](navigation-destinations.md#destination-metadata-and-scene-strategies).

Declare a `MetadataKey<T>` to read and write a metadata value:

<!-- snippet: navigation-keys/metadata-key -->
```kotlin
object OpenedAt : NavigationKey.MetadataKey<Long?>(default = null)
```

`withMetadata` sets a metadata value for one `open` call:

<!-- snippet: navigation-keys/with-metadata -->
```kotlin
navigation.open(
    ShowProfile("user-123").withMetadata(OpenedAt, Clock.System.now().toEpochMilliseconds())
)
```

A [plugin](../advanced/plugins.md) can set a metadata value on every
instance in the app. The [OpenedTimestampPlugin recipe][timestamp-recipe]
sets an opened-at timestamp on every instance.

Enro stores a metadata value under the `name` of the `MetadataKey`. The
default `name` is the class name of the `MetadataKey`, so declare each
`MetadataKey` as an `object`. On Kotlin/JS the class name is the simple
name. Two `MetadataKey` classes with the same simple name throw on
Kotlin/JS unless one class passes an explicit `name`.

Enro does not save a `TransientMetadataKey` value in saved state. After
the app is restored from saved state, reading a `TransientMetadataKey`
returns the default value. `TransientMetadataKey` requires an
`@AdvancedEnroApi` opt-in.

## Naming conventions

Name a `NavigationKey` with a verb phrase or a noun phrase for the screen:

- Use `ShowProfile`, `EditProfile`, `SelectDate`, `ConfirmDelete`.
- Do not use `ProfileKey`, `ProfileScreenKey`, `ProfileNavigationKey`. The
  `NavigationKey` type already states that the class is a key.

Name a key that has a result after the result:

- `SelectDate : NavigationKey.WithResult<LocalDate>`
- `PickContact : NavigationKey.WithResult<Contact>`
- `ConfirmDelete : NavigationKey.WithResult<Boolean>`

Declare a key that has no inputs as a `data object`:

<!-- snippet: navigation-keys/data-object -->
```kotlin
@Serializable
data object Home : NavigationKey

@Serializable
data object PickName : NavigationKey.WithResult<String>
```

## Keys in a multi-module project

In a multi-module project, declare each `NavigationKey` in the API module
of the feature, not in the module that implements the screen. A caller
module then depends on the key and not on the implementation.

Example module layout:

```
:feature-profile-api          ← declares ShowProfile, EditProfile
:feature-profile-impl         ← declares the Composables annotated with @NavigationDestination
:app                          ← depends on both modules and on the other feature modules
```

See the [modular navigation recipe][modular-recipe].

## See also

- [Navigation Destinations](navigation-destinations.md): binding a key to a screen implementation.
- [Navigation Handles](navigation-handles.md): reading the key inside a screen.
- [Results](../advanced/results.md): returning and receiving a result for a `WithResult` key.
- [Basic recipe][basic-recipe]: a minimal app with two screens.

[basic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/basic/BasicNavigation.kt
[modular-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/modular/ModularNavigation.kt
[timestamp-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/plugins/OpenedTimestampPlugin.kt
