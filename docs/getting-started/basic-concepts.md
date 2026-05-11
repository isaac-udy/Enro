---
title: Basic Concepts
parent: Getting Started
nav_order: 2
---

# Basic Concepts

This page is the short vocabulary tour. Each concept here gets its own page
under *Core Concepts* — read this one first so the rest of the docs make
sense.

The central idea is that **screens behave like functions**. A screen has a
contract (its inputs and an optional return value); calling code invokes the
contract without knowing how the screen is implemented.

## Navigation Keys

A `NavigationKey` is the contract for a screen — its function signature. The
properties of the key are the inputs to the screen. If the screen produces a
value, the key implements `NavigationKey.WithResult<T>`.

```kotlin
@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

Keys are `@Serializable` (kotlinx.serialization) so Enro can persist the
backstack across process death and across platforms.

A key on its own doesn't do anything — it's a value. The system that *invokes*
the contract is the navigation handle (below).

## Navigation Destinations

A `NavigationDestination` is the *implementation* of a contract — the screen
itself. It's bound to a key with the `@NavigationDestination(KeyClass::class)`
annotation. Two styles are supported:

A regular Composable function:

```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
```

Or a destination provider, when the destination needs metadata (for example,
to behave like a dialog or an overlay):

```kotlin
@NavigationDestination(ShowProfile::class)
val profileDestination = navigationDestination<ShowProfile> {
    Text("Profile for ${navigation.key.userId}")
}
```

The provider form is also what you use to declare a dialog, bottom sheet, or
custom overlay — through the `metadata = { dialog() }` or
`metadata = { directOverlay() }` builders. See
[Navigation Destinations](../core-concepts/navigation-destinations.md).

## Navigation Handle

A `NavigationHandle` is the control surface inside a screen — the variable
you call `open`, `close`, or `complete` on.

```kotlin
val navigation = navigationHandle<ShowProfile>()

navigation.open(SelectDate(maxDate = LocalDate.now())) // open another screen
navigation.close()                                     // close this screen
navigation.complete(result)                            // close with a result
```

The typed parameter (`<ShowProfile>` above) gives you access to the key the
screen was opened with via `navigation.key`.

## Navigation Container

A `NavigationContainer` is a location in your UI that hosts a backstack of
destinations. You create one inside a Composable with
`rememberNavigationContainer`, give it an initial backstack, and render it with
`NavigationDisplay`.

```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
)
NavigationDisplay(state = container)
```

A typical app has one root container; nested containers are supported and are
how features like tabs, list-detail layouts, and multiple back stacks are built.

## NavigationKey.Instance

When a key is added to a backstack, Enro wraps it in a
`NavigationKey.Instance` — the same key may appear in the backstack more than
once, so each appearance gets a unique `id`. You'll mostly see `Instance` when
building a backstack:

```kotlin
val initial = backstackOf(
    Home.asInstance(),
    ShowProfile("user-123").asInstance(),
)
```

You can also attach `Metadata` to an instance to influence how that particular
appearance behaves (animations, scene treatment, etc.).

## Results

A `NavigationKey.WithResult<T>` screen returns a value to its caller. Callers
register a result channel and call `open` on it.

```kotlin
val getDate = registerForNavigationResult<LocalDate>(
    onCompleted = { date -> /* use date */ },
)

Button(onClick = { getDate.open(SelectDate(maxDate = LocalDate.now())) }) {
    Text("Pick a date")
}
```

The screen returns its value with `navigation.complete(date)`. See
[Results](../advanced/results.md).

## NavigationComponent

A `NavigationComponent` is the configuration object for Enro in your
application. It's declared once, annotated with `@NavigationComponent`, and
installed at app startup.

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { /* optional config */ }
)
```

See [Installation](installation.md).

## Where everything fits

```
Application starts
  └── MyComponent.installNavigationController(this)        ← happens once

Compose tree
  └── rememberNavigationContainer(backstack = ...)         ← NavigationContainer
        └── NavigationDisplay(state = container)
              └── Destination for the current key on the stack
                    └── navigationHandle<MyKey>()          ← NavigationHandle
                          └── .open(...) / .close() / .complete(...)
```

## Next steps

- Walk through [Your First Screen](your-first-screen.md) for a complete end-to-end example.
- Then read the [Core Concepts](../../index.md#core-concepts) pages in order — they go into each of the above in depth.
