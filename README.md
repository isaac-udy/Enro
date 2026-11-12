[![Maven Central](https://img.shields.io/maven-central/v/dev.enro/enro.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.enro%22)

> **Note**
>
> The [CHANGELOG](./CHANGELOG.md) lists the changes in each Enro release.

# Enro 🗺️
### [enro.dev](https://enro.dev)

Enro is a navigation library for Kotlin Multiplatform. In Enro, **a screen is
declared like a function**.

A `NavigationKey` is the signature of a screen. The `NavigationKey` declares the
screen's inputs and, optionally, the type of the screen's result. Code that opens
a screen depends on the `NavigationKey`, not on the screen's implementation.

```kotlin
@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

The same two contracts, written as function signatures:

```kotlin
fun showProfile(userId: String): Unit
fun selectDate(minDate: LocalDate? = null, maxDate: LocalDate? = null): LocalDate
```

Enro targets **Android, iOS, Desktop and Web** through Compose Multiplatform.
On Android, a Fragment or an Activity can be a destination. The `enro-compat`
module provides the Enro 2 API for apps that are migrating to Enro 3.

## Gradle dependencies

Enro is published to [Maven Central](https://search.maven.org/). Add the
`mavenCentral()` repository and these dependencies:

```kotlin
dependencies {
    implementation("dev.enro:enro:3.0.0")
    ksp("dev.enro:enro-processor:3.0.0")
    testImplementation("dev.enro:enro-test:3.0.0")
}
```

## Defining and opening a screen

Declare a `NavigationKey` for each screen, bind a destination to each key with
`@NavigationDestination`, and open a key with `navigation.open(key)`.

```kotlin
@Serializable
data object Home : NavigationKey

@Serializable
data class Profile(val userId: String) : NavigationKey

@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val navigation = navigationHandle<Home>()
    Button(onClick = { navigation.open(Profile("user-123")) }) {
        Text("View profile")
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
```

Declare one `NavigationComponent` and install the `NavigationComponent` when the
application starts:

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { }
)

// Android
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
```

Create a navigation container in a Composable and render the container with
`NavigationDisplay`:

```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
)
NavigationDisplay(state = container)
```

The documentation at [enro.dev](https://enro.dev) covers each of these steps.

## Documentation and examples

- **Documentation:** [enro.dev](https://enro.dev) covers installation, concepts, results, animations, testing, each platform, and migration from Enro 2.
- **Recipes:** [`recipes/`](./recipes/common/src/commonMain/kotlin/dev/enro/recipes) contains runnable examples. Each recipe shows one concept. [enro.dev/docs/recipes.html](https://enro.dev/docs/recipes.html) lists the recipes. Run the recipes app with `./gradlew :recipes:app:desktop:run`.
- **Sample apps:** [`tests/application`](./tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples) contains three larger samples. Run the test application with `./gradlew :tests:application:app:desktop:run`, then choose "Samples".
- **API reference:** `./gradlew dokkaGenerate` writes the API reference to `build/dokka/html/index.html`. Each artifact on Maven Central also has a `-javadoc.jar`.
- **Changelog:** [CHANGELOG.md](./CHANGELOG.md).

## Migrating from Enro 2

Enro 3 is a rewrite of Enro 2 for Kotlin Multiplatform and Compose. The
migration guide at
[enro.dev/docs/migrating-from-v2.html](https://enro.dev/docs/migrating-from-v2.html)
lists every API change. The main changes:

- `NavigationKey.SupportsPush` / `SupportsPresent` → `NavigationKey`, or `NavigationKey.WithResult<T>` for a screen with a result
- `@Parcelize` → `@Serializable` (kotlinx.serialization)
- `push()` / `present()` → `open(key)`; destination metadata declares dialog and overlay behaviour
- `closeWithResult(r)` → `complete(r)`
- `NavigationApplication` is removed; call `installNavigationController` from `Application.onCreate`
- A Fragment or an Activity can be a destination; the `enro-compat` module provides the Enro 2 API during migration

## Applications using Enro

<p align="center">
    <a href="https://www.splitwise.com/">
        <img width="100px" src="resources/splitwise-icon.png" />
    </a>
   &nbsp;
   &nbsp;
    <a href="https://play.google.com/store/apps/details?id=com.beyondbudget">
        <img width="100px" src="resources/beyond-budget-icon.png" />
    </a>
</p>

---

*"The novices' eyes followed the wriggling path up from the well as it swept a great meandering arc around the hillside. Its stones were green with moss and beset with weeds. Where the path disappeared through the gate they noticed that it joined a second track of bare earth, where the grass appeared to have been trampled so often that it ceased to grow. The dusty track ran straight from the gate to the well, marred only by a fresh set of sandal-prints that went down, and then up, and ended at the feet of the young monk who had fetched their water." — [The Garden Path](http://thecodelesscode.com/case/156)*
