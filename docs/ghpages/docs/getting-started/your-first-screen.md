---
title: Your First Screen
parent: Getting Started
nav_order: 3
---

# Your First Screen

This page builds an example app with three screens: a `Home` screen, a
`Profile` screen that `Home` opens, and a `PickName` screen that returns a
result to `Home`. The example uses each term defined in
[Basic Concepts](basic-concepts.md).

The recipes [`recipes/basic/BasicNavigation.kt`][basic-recipe] and
[`recipes/results/ReturningResults.kt`][results-recipe] are runnable versions
of the same example.

## 1. Define the keys

Each key declares the inputs of one screen and, optionally, the type of the
screen's result.

<!-- snippet: your-first-screen/keys -->
```kotlin
@Serializable
data object Home : NavigationKey

@Serializable
data class Profile(val userId: String) : NavigationKey

@Serializable
data object PickName : NavigationKey.WithResult<String>
```

`Home` has no inputs. `Profile` requires a `userId`. `PickName` returns a
`String` to the caller.

## 2. Implement the destinations

Each destination is a `@Composable` function annotated with
`@NavigationDestination(KeyClass::class)`.

<!-- snippet: your-first-screen/destinations -->
```kotlin
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val navigation = navigationHandle<Home>()

    val askForName = registerForNavigationResult<String>(
        onCompleted = { name -> /* use name */ },
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

The destinations call these functions:

- `navigationHandle<T>()` returns a `NavigationHandle<T>`. `navigation.key` has
  type `Profile` inside `ProfileScreen`.
- `navigation.open(otherKey)` opens the destination for `otherKey` on top of
  the current destination.
- `navigation.close()` closes the current destination.
- `navigation.complete(value)` closes the current destination and returns
  `value` to the caller. `complete(value)` compiles only when the key
  implements `NavigationKey.WithResult<T>`.
- `registerForNavigationResult<T>` returns a result channel. `open(key)` on the
  result channel opens `key`. Enro calls `onCompleted` with the result when the
  opened destination calls `complete(value)`, and calls `onClosed` when the
  opened destination closes without a result.

## 3. Declare and install the NavigationComponent

Declare one `NavigationComponent` for the application:

<!-- snippet: your-first-screen/component -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { }
)
```

KSP generates an `installNavigationController` extension on `MyComponent` for
each target. On Android, call `installNavigationController` from
`Application.onCreate`:

<!-- snippet: your-first-screen/application -->
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
```

[Installation](installation.md) shows the install call for iOS, Desktop and
Web.

## 4. Create a navigation container

Create one container at the root of the Compose tree:

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

`backstackOf(Home.asInstance())` is the container's initial backstack: one
`Home` instance. `NavigationDisplay` renders the container's backstack and
animates each change to the backstack.

## 5. Run the app

1. Open the app. The app shows `Home`.
2. Tap **View profile**. Enro adds `Profile` to the backstack and
   `NavigationDisplay` animates the transition.
3. Tap **Back**. `Profile` closes and the app shows `Home`.
4. Tap **Pick a name**. `PickName` opens.
5. Type a name and tap **Done**. Enro calls `onCompleted` in `HomeScreen` with
   the typed name.

## Next steps

- Dialogs, bottom sheets and overlays:
  [Navigation Destinations](../core-concepts/navigation-destinations.md).
- Multiple containers, tabs and nested back stacks:
  [Navigation Containers](../core-concepts/navigation-containers.md).
- Results: [Results](../advanced/results.md).
- Transitions and shared elements: [Animations](../advanced/animations.md).
- Tests: [Testing](../advanced/testing.md).

[basic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/basic/BasicNavigation.kt
[results-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/results/ReturningResults.kt
