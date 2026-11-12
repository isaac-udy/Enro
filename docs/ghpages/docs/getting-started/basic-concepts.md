---
title: Basic Concepts
parent: Getting Started
nav_order: 2
---

# Basic Concepts

This page defines each Enro term. The pages under *Core Concepts* describe each
term in detail.

In Enro, **a screen is declared like a function**. A screen has a contract: the
screen's inputs and an optional result. Code that opens a screen depends on the
contract, not on the screen's implementation.

## Navigation Keys

A `NavigationKey` is the contract for a screen. The properties of the
`NavigationKey` are the screen's inputs. A key for a screen that returns a
result implements `NavigationKey.WithResult<T>`.

<!-- snippet: basic-concepts/keys -->
```kotlin
@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

A `NavigationKey` must be `@Serializable` (kotlinx.serialization). Enro
serializes each `NavigationKey` to save and restore the backstack. Every
property of a `NavigationKey` must also be serializable. `LocalDate` in the
sample is `kotlinx.datetime.LocalDate`.

A `NavigationKey` is a value and performs no navigation. A
[`NavigationHandle`](#navigation-handle) opens a `NavigationKey`.

## Navigation Destinations

A `NavigationDestination` is the screen that implements a contract. The
`@NavigationDestination(KeyClass::class)` annotation binds a destination to a
key. A destination has one of two forms.

A Composable function:

<!-- snippet: basic-concepts/composable-destination -->
```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
```

A destination provider, created with `navigationDestination<K> { }`:

<!-- snippet: basic-concepts/provider-destination -->
```kotlin
@NavigationDestination(ShowProfile::class)
val profileDestination = navigationDestination<ShowProfile> {
    Text("Profile for ${navigation.key.userId}")
}
```

A destination provider can declare metadata. `metadata = { dialog() }` and
`metadata = { directOverlay() }` declare a dialog, bottom sheet or overlay
destination. See
[Navigation Destinations](../core-concepts/navigation-destinations.md).

## Navigation Handle

A `NavigationHandle` is the object a screen calls to navigate. A
`NavigationHandle` has `open`, `close` and `complete` functions.

<!-- snippet: basic-concepts/handle -->
```kotlin
val navigation = navigationHandle<SelectDate>()

navigation.open(ShowProfile("user-123"))
navigation.close()
navigation.complete(today) // closes SelectDate and returns `today` to the caller
```

`navigation.key` is the key that opened the screen, typed as the type parameter
of `navigationHandle` (`SelectDate` in the sample). `complete(result)` compiles
only when the key type implements `NavigationKey.WithResult`.

## Navigation Container

A `NavigationContainer` contains a backstack of destinations.
`rememberNavigationContainer` creates a `NavigationContainer` in a Composable
with an initial backstack. `NavigationDisplay` renders the
`NavigationContainer`.

<!-- snippet: basic-concepts/container -->
```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
)
NavigationDisplay(state = container)
```

An app usually has one root container. A destination can contain nested
containers. Tabs, list-detail layouts and multiple back stacks use nested
containers. See [Navigation Containers](../core-concepts/navigation-containers.md).

## NavigationKey.Instance

A backstack contains `NavigationKey.Instance` values. A
`NavigationKey.Instance` is a key and a unique `id`, so a backstack can contain
the same key more than once. `asInstance()` creates a `NavigationKey.Instance`
from a key:

<!-- snippet: basic-concepts/instance -->
```kotlin
val initial = backstackOf(
    Home.asInstance(),
    ShowProfile("user-123").asInstance(),
)
```

A `NavigationKey.Instance` also has `metadata`. Metadata set on one instance
applies to that instance only.

## Results

A screen with a `NavigationKey.WithResult<T>` key returns a value to the
caller. The caller registers a result channel with
`registerForNavigationResult` and calls `open` on the result channel.

<!-- snippet: basic-concepts/results -->
```kotlin
val getDate = registerForNavigationResult<LocalDate>(
    onCompleted = { date -> /* use date */ },
)

Button(onClick = { getDate.open(SelectDate(maxDate = today)) }) {
    Text("Pick a date")
}
```

The `SelectDate` screen returns the value with `navigation.complete(date)`. See
[Results](../advanced/results.md).

## NavigationComponent

A `NavigationComponent` configures Enro for an application. An application
declares one `NavigationComponent`, annotated with `@NavigationComponent`, and
installs the `NavigationComponent` when the application starts.

<!-- snippet: basic-concepts/component -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { /* optional config */ }
)
```

See [Installation](installation.md).

## Structure of an Enro app

```
Application starts
  └── MyComponent.installNavigationController(this)        ← called once

Compose tree
  └── rememberNavigationContainer(backstack = ...)         ← NavigationContainer
        └── NavigationDisplay(state = container)
              └── Destination for the current key on the stack
                    └── navigationHandle<MyKey>()          ← NavigationHandle
                          └── .open(...) / .close() / .complete(...)
```

## Next steps

- [Your First Screen](your-first-screen.md) builds an example with three screens.
- The [Core Concepts](../../index.md#core-concepts) pages describe each term on this page in detail.
