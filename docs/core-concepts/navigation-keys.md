---
title: Navigation Keys
parent: Core Concepts
nav_order: 1
---

# Navigation Keys

A `NavigationKey` is the contract for a screen — its function signature. The
properties of the key are the inputs to the screen. If the screen produces a
typed return value, the key implements `NavigationKey.WithResult<T>`.

Keys are values: declarative, serializable, and decoupled from the screen
they identify. Callers refer to screens only through their keys, never
through their implementations.

```kotlin
@Serializable
data class ShowProfile(val userId: String) : NavigationKey
```

Read that as `fun showProfile(userId: String): Unit`.

```kotlin
@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

Read that as `fun selectDate(minDate: LocalDate? = null, maxDate: LocalDate? = null): LocalDate`.

## Serialization

Keys must be `@Serializable` (kotlinx.serialization). Enro uses the serializer
to persist backstacks across process death, to bridge instances between
platforms, and to support deep links.

Every property on the key needs a serializer too. Built-in Kotlin types
(`String`, `Int`, `Boolean`, `List<T>`, `Map<K, V>`, nullable types,
`kotlin.uuid.Uuid`, etc.) work without configuration. For domain types you
own, annotate them with `@Serializable`:

```kotlin
@Serializable
data class UserId(val value: String)

@Serializable
data class ShowProfile(val userId: UserId) : NavigationKey
```

For types you don't own (or for polymorphic types in a sealed hierarchy),
register a custom `SerializersModule` on your `NavigationComponent`:

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
produces a value of type `T` to its caller. The screen returns the value by
calling `navigation.complete(value)`. The caller receives it through a
`registerForNavigationResult` channel — see [Results](../advanced/results.md).

```kotlin
@Serializable
data class ConfirmDelete(val itemName: String) : NavigationKey.WithResult<Boolean>
```

Whether a key implements `WithResult` only determines whether `complete`
*requires* a value — every screen can still use both `close` and `complete`
regardless. See [close vs complete](navigation-handles.md#closing-vs-completing)
on the Navigation Handles page for the semantic distinction.

## NavigationKey.Instance

A key on its own is a value — the same key may appear in a backstack more
than once (push the same profile, push another copy of the same profile). To
distinguish individual appearances, Enro wraps every key in a
`NavigationKey.Instance` when it enters a backstack. Each instance has a
unique `id` and its own `metadata`.

You'll usually only see `Instance` when building the initial backstack:

```kotlin
val initial = backstackOf(
    Home.asInstance(),
    ShowProfile("user-123").asInstance(),
)
```

`asInstance()` is the standard way to wrap a key. Two instances of the same
key are distinct values (different `id`s) — `Set<NavigationKey.Instance<*>>`
will not collapse them.

## Instance metadata

`NavigationKey.Metadata` is a typed, serializable key-value bag attached to
each `Instance` on the backstack. It's a place to carry data **about a
particular appearance of a key** that isn't part of the key's contract.

A few real-world uses for instance metadata:

- The screen-space coordinates of the click that opened this destination,
  so an animation can play from that point.
- An analytics correlation id assigned at the moment the instance was
  created, so events emitted while the destination is open can be linked
  back to its origin.
- A timestamp of when this instance was opened.
- A note indicating which feature flow opened this destination.

These are all things that *might be present, or might not*, and that don't
change what the destination is or how it renders. They're closer to "extra
parameters that any key might carry" than to anything in the key itself.

> **Don't confuse this with destination metadata.** A
> `navigationDestination(metadata = { dialog() }) { ... }` block configures
> *the destination* — it drives rendering decisions like "show this as a
> dialog" or "render as an overlay." That's a separate system. See
> [Navigation Destinations](navigation-destinations.md#destination-metadata-and-scene-strategies).

Define a `MetadataKey<T>` to read and write a metadata value:

```kotlin
object OpenedAt : NavigationKey.MetadataKey<Long?>(default = null)
```

Attach a value to a key for a single open via `withMetadata`:

```kotlin
navigation.open(
    ShowProfile("user-123").withMetadata(OpenedAt, Clock.System.now().toEpochMilliseconds())
)
```

Or set it from a [plugin](../advanced/plugins.md) on every instance globally
— useful when you want metadata that applies uniformly across the app (an
analytics correlation id, an origin point captured from the last user input,
an opened-at timestamp, etc.). The
[OpenedTimestampPlugin recipe][timestamp-recipe] is a small worked example
of this.

A `MetadataKey`'s `name` is its `qualifiedName` — make it an `object` or a
top-level singleton.

For metadata that should not be persisted across process death, use
`TransientMetadataKey`. This is marked `@AdvancedEnroApi` because skipping
persistence has consequences — your code has to tolerate the value being
absent after the app is restored from saved state.

## Naming conventions

A `NavigationKey` should read like a verb-phrase or noun-phrase for a screen:

- `ShowProfile`, `EditProfile`, `SelectDate`, `ConfirmDelete` ✅
- `ProfileKey`, `ProfileScreenKey`, `ProfileNavigationKey` ❌ (the `Key`
  suffix is redundant — the type already tells you what it is)

For keys that produce results, the name often matches the result:

- `SelectDate : NavigationKey.WithResult<LocalDate>`
- `PickContact : NavigationKey.WithResult<Contact>`
- `ConfirmDelete : NavigationKey.WithResult<Boolean>`

A `data object` is the right choice for a key with no inputs:

```kotlin
@Serializable
data object Home : NavigationKey

@Serializable
data object PickName : NavigationKey.WithResult<String>
```

## Where keys live

In a multi-module project, a `NavigationKey` should live in the *contract*
module for the feature it represents — not in the module that *implements*
the screen. That's the whole point of the contract: callers depend on the
key without depending on the implementation.

A common arrangement:

```
:feature-profile-api          ← contains ShowProfile, EditProfile, etc.
:feature-profile-impl         ← contains the Composables annotated with @NavigationDestination
:app                          ← depends on both, plus :feature-other-impl, etc.
```

See the [modular navigation recipe][modular-recipe] for a worked example.

## See also

- [Navigation Destinations](navigation-destinations.md) — how a key is bound to a screen implementation.
- [Navigation Handles](navigation-handles.md) — how a screen reads its own key.
- [Results](../advanced/results.md) — how `WithResult` keys produce and consume values.
- [Basic recipe][basic-recipe] — minimal end-to-end example.

[basic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/basic/BasicNavigation.kt
[modular-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/modular/ModularNavigation.kt
[timestamp-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/plugins/OpenedTimestampPlugin.kt
