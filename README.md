[![Maven Central](https://img.shields.io/maven-central/v/dev.enro/enro.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.enro%22)

> **Note**
>
> Please see the [CHANGELOG](./CHANGELOG.md) to understand the latest changes in Enro.

# Enro 🗺️
### [enro.dev](https://enro.dev)

Enro is a powerful navigation library for Kotlin Multiplatform, based on a simple
idea: **screens within an application should behave like functions**.

A `NavigationKey` is the signature of a screen — it declares the screen's inputs,
and optionally a typed result. Calling code never needs to know how the screen is
implemented; it just invokes the contract.

```kotlin
@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

If you read those as function signatures:

```kotlin
fun showProfile(userId: String): Unit
fun selectDate(minDate: LocalDate? = null, maxDate: LocalDate? = null): LocalDate
```

Enro targets **Android, iOS, Desktop and Web** through Compose Multiplatform.
A first-class compatibility layer (`enro-compat`) keeps Fragments and Activities
working on Android, so you can adopt Enro in an existing app without rewriting it.

## Gradle quick-start

Enro is published to [Maven Central](https://search.maven.org/). Add the
`mavenCentral()` repository and depend on:

```kotlin
dependencies {
    implementation("dev.enro:enro:3.0.0-alpha10")
    ksp("dev.enro:enro-processor:3.0.0-alpha10")
    testImplementation("dev.enro:enro-test:3.0.0-alpha10")
}
```

## A working screen

Define a key, implement a destination, and navigate. That's the loop.

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

Wire Enro into your application once:

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { }
)

// Android — install in your Application
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
```

Then host a backstack anywhere in your UI:

```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
)
NavigationDisplay(state = container)
```

That's the whole flow. See [enro.dev](https://enro.dev) for the full guide.

## Learn Enro

- **Documentation:** [enro.dev](https://enro.dev) — installation, concepts, results, animations, testing, platform guides, and the migration guide from Enro 2.
- **Recipes:** [`recipes/`](./recipes/src/commonMain/kotlin/dev/enro/recipes) — every concept (dialogs, bottom sheets, list-detail, tabs, deep links, managed flows, shared view models, custom animations) is a small runnable sample.
- **Changelog:** [CHANGELOG.md](./CHANGELOG.md).

## Migrating from Enro 2

Enro 3 is a significant rewrite that targets Kotlin Multiplatform and a
Compose-first model. The migration guide at
[enro.dev/docs/migrating-from-v2.html](https://enro.dev/docs/migrating-from-v2.html)
covers the API delta in detail. Highlights:

- `NavigationKey.SupportsPush` / `SupportsPresent` → flat `NavigationKey` (+ optional `WithResult<T>`)
- `@Parcelize` → `@Serializable` (kotlinx)
- `push()` / `present()` → `open(key)`; dialog/overlay behaviour now lives in destination metadata
- `closeWithResult(r)` → `complete(r)`
- `NavigationApplication` interface is gone; install the component directly from `Application.onCreate`
- Fragments and Activities continue to work via the `enro-compat` module

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
