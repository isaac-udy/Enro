---
title: Your First Screen
parent: Getting Started
nav_order: 3
---

# Your First Screen

This page walks through a complete, working example: two screens that navigate
to each other, plus a third screen that returns a result. By the end you'll
have touched every concept covered in [Basic Concepts](basic-concepts.md).

If you'd rather read the finished code, the equivalent runnable example lives in
[`recipes/basic/BasicNavigation.kt`][basic-recipe] and
[`recipes/results/ReturningResults.kt`][results-recipe].

## 1. Define the keys

A key is a contract — what does this screen need, and what does it produce?

```kotlin
@Serializable
data object Home : NavigationKey

@Serializable
data class Profile(val userId: String) : NavigationKey

@Serializable
data object PickName : NavigationKey.WithResult<String>
```

`Home` takes no inputs. `Profile` requires a `userId`. `PickName` produces a
`String` back to whoever opened it.

## 2. Implement the destinations

Each destination is annotated with `@NavigationDestination(KeyClass::class)`.
The simplest form is a `@Composable` function.

```kotlin
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val navigation = navigationHandle<Home>()

    val askForName = registerForNavigationResult<String>(
        onCompleted = { name -> /* we'll use it in a moment */ },
    )

    Column {
        Text("Welcome home")

        Button(onClick = { navigation.open(Profile("user-123")) }) {
            Text("View profile")
        }

        Button(onClick = { askForName.open(PickName) }) {
            Text("Pick a name")
        }
    }
}

@Composable
@NavigationDestination(Profile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<Profile>()

    Column {
        Text("Profile for ${navigation.key.userId}")
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

@Composable
@NavigationDestination(PickName::class)
fun PickNameScreen() {
    val navigation = navigationHandle<PickName>()
    var input by remember { mutableStateOf("") }

    Column {
        TextField(value = input, onValueChange = { input = it })

        Button(onClick = { navigation.complete(input) }) {
            Text("Done")
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}
```

A few things worth noticing:

- `navigationHandle<T>()` returns a `NavigationHandle` that knows the type of the
  current key — `navigation.key` is typed `Profile` inside `ProfileScreen`.
- `navigation.open(otherKey)` adds another destination on top of this one.
- `navigation.close()` removes the current destination.
- `navigation.complete(value)` is the result-returning form of `close()` — only
  callable on a destination whose key implements `NavigationKey.WithResult<T>`.
- `registerForNavigationResult<T>` returns a channel; `open` it with a key and
  `onCompleted` is called with the result. There's also `onClosed` for when the
  user dismisses without producing a result.

## 3. Wire up the NavigationComponent

Declare a `NavigationComponent` once for your application:

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { }
)
```

KSP generates an `installNavigationController` extension on this object for
each platform you target. On Android, install it in your `Application`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
```

See [Installation](installation.md) for the equivalent on iOS, Desktop and
Web.

## 4. Host the backstack

Decide where your navigation lives in the UI. The minimal pattern is a single
container at the root of your Compose tree:

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

`backstackOf(Home.asInstance())` says "start with `Home` on the stack".
`NavigationDisplay` watches the container's backstack and renders the current
destination, animating transitions for you.

## 5. Run it

Open the app. You're on `Home`. Tap **View profile** — the backstack pushes
`Profile`, and `NavigationDisplay` animates the transition. Tap **Back** —
`Profile` closes and you're back on `Home`.

Tap **Pick a name** — `PickName` opens. Type something and tap **Done**. The
`onCompleted` callback fires on `Home` with the string you typed.

## What's next

That's a working app. From here:

- For different presentation styles (dialogs, bottom sheets, custom overlays),
  see [Navigation Destinations](../core-concepts/navigation-destinations.md).
- For multiple containers, tabs, or nested back stacks, see
  [Navigation Containers](../core-concepts/navigation-containers.md).
- For richer result handling, see [Results](../advanced/results.md).
- For custom transitions and per-element animations, see
  [Animations](../advanced/animations.md).
- For testing, see [Testing](../advanced/testing.md).

[basic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/basic/BasicNavigation.kt
[results-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/results/ReturningResults.kt
