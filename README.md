[![Maven Central](https://img.shields.io/maven-central/v/dev.enro/enro.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.enro%22)
[![libs.tech recommends](https://libs.tech/project/256179010/badge.svg)](https://libs.tech/project/256179010/enro)
> **Note**
>
> Please see the [CHANGELOG](./CHANGELOG.md) to understand the latest changes in Enro

# Enro 🗺️
### [enro.dev](https://enro.dev)

Enro is a powerful navigation library based on a simple idea; screens within an application should behave like functions.

### Gradle quick-start
Enro is published to [Maven Central](https://search.maven.org/). Make sure your project includes the mavenCentral() repository, and then include the following in your module's build.gradle:

```kotlin
dependencies {
    implementation("dev.enro:enro:2.7.0")
    ksp("dev.enro:enro-processor:2.7.0") // both kapt and ksp are supported
    testImplementation("dev.enro:enro-test:2.7.0")
}
```

# Introduction
This introduction is designed to give a brief overview of how Enro works. It doesn't contain all the information you might need to know to get Enro installed in an application, or provide specific details about each of the topics covered. For this information please refer to the other documentation, such as:
* [Installing Enro](https://enro.dev/docs/installing-enro.html)
* [Navigation Keys](https://enro.dev/docs/navigation-keys.html)
* [FAQ](https://enro.dev/docs/frequently-asked-questions.html)

## NavigationKeys
Building a screen using Enro begins with defining a `NavigationKey`. A `NavigationKey` can be thought of like the function signature or interface for a screen. Just like a function signature, a `NavigationKey` represents a contract. By invoking the contract, and providing the requested parameters, an action will occur and you may (or may not) receive a result.

Here's an example of two `NavigationKey`s that you might find in an Enro application:
```kotlin

@Parcelize
data class ShowUserProfile(
   val userId: UserId
) : NavigationKey.SupportsPush

@Parcelize
data class SelectDate(
   val minDate: LocalDate? = null,
   val maxDate: LocalDate? = null,
) : NavigationKey.SupportsPresent.WithResult<LocalDate>

```

If you think of the `NavigationKey`s as function signatures, they could look something like this:
```kotlin

fun showUserProfile(userId: UserId): Unit
fun selectDate(minDate: LocalDate? = null, maxDate: LocalDate? = null): LocalDate

```

## NavigationHandles
Once you've defined the `NavigationKey` for a screen, you'll want to use it. In any Activity, Fragment or Composable, you will be able to get access to a `NavigationHandle`, which allows you to perform navigation. The syntax is slightly different for each type of screen.

### In a Fragment or Activity:
```kotlin

class ExampleFragment : Fragment() {
   val selectDate by registerForNavigationResult<LocalDate> { selectedDate: LocalDate -> 
     /* do something! */ 
   }
   
   fun onSelectDateButtonPressed() = selectDate.present(
     SelectDate(maxDate = LocalDate.now())
   )
   
   fun onProfileButtonPressed() {
      getNavigationHandle().push(
         ShowUserProfile(userId = /* ... */)
      )
   }
}

```

### In a Composable:
```kotlin

@Composable
fun ExampleComposable() {
   val navigation = navigationHandle()
   val selectDate = registerForNavigationResult<LocalDate> { selectedDate: LocalDate -> 
        /* do something! */ 
   }
   
   Button(onClick = {
      selectDate.present(
         SelectDate(maxDate = LocalDate.now())
      )
   }) { /* ... */ }

   Button(onClick = {
      navigation.push(
         ShowUserProfile(userId = /* ... */)
      )
   }) { /* ... */ }
}

```

## NavigationDestinations
You might have noticed that we've defined our `ExampleFragment` and `ExampleComposable` in the example above before we've even begun to think about how we're going to implement the `ShowUserProfile` and `SelectDate` destinations. That's because implementing a `NavigationDestination` in Enro is the least interesting part of the process. All you need to do to make this application complete is to build an Activity, Fragment or Composable, and mark it as the `NavigationDestination` for a particular `NavigationKey`.

The recommended approach to mark an Activity, Fragment or Composable as a `NavigationDestination` is to use the Enro annotation processor and the `@NavigationDestination` annotation.

### In a Fragment or Activity:
```kotlin

@NavigationDestination(ShowUserProfile::class)
class ProfileFragment : Fragment {
   // providing a type to `by navigationHandle<T>()` gives you access to the NavigationKey 
   // used to open this destination, and you can use this to read the 
   // arguments for the destination
    val navigation by navigationHandle<ShowProfile>() 
}

```

### In a Composable:
```kotlin

@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateComposable() { 
   // providing a type to `navigationHandle<T>()` gives you access to the NavigationKey 
   // used to open this destination, and you can use this to read the 
   // arguments for the destination
   val navigation = navigationHandle<SelectDate>()
   // ...
   Button(onClick = {
       navigation.closeWithResult( /* pass a local date here to return that as a result */ )
   }) { /* ... */ }
}

```

### Without annotation processing:
If you'd prefer to avoid annotation processing, you can use a DSL to define these bindings when creating your application (see [here]() for more information):
```kotlin

// this needs to be registered with your application
val exampleNavigationComponent = createNavigationComponent {
   fragmentDestination<ShowProfile, ProfileFragment>() 
   composableDestination<SelectDate> { SelectDateComposable() }
}

```

# Applications using Enro
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

*"The novices’ eyes followed the wriggling path up from the well as it swept a great meandering arc around the hillside. Its stones were green with moss and beset with weeds. Where the path disappeared through the gate they noticed that it joined a second track of bare earth, where the grass appeared to have been trampled so often that it ceased to grow. The dusty track ran straight from the gate to the well, marred only by a fresh set of sandal-prints that went down, and then up, and ended at the feet of the young monk who had fetched their water." - [The Garden Path](http://thecodelesscode.com/case/156)*

