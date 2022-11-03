[![Maven Central](https://img.shields.io/maven-central/v/dev.enro/enro.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.enro%22)

> **Note**
>
> Enro 2.x.x has now merged to the main branch, but is still in an alpha/beta release phase. The Enro 1.x.x branch is still being maintained for bug fixes and can be found [here](https://github.com/isaac-udy/Enro/tree/1.x.x)

# Enro 🗺️

Enro is a powerful navigation library based on a simple idea; screens within an application should behave like functions. 

### Gradle quick-start

```gradle
dependencies {
    implementation("dev.enro:enro:2.0.0-alpha10")
    kapt("dev.enro:enro-processor:2.0.0-alpha10")
    testImplementation("dev.enro:enro-test:2.0.0-alpha10")
}
```

### [Read the docs](https://enro.dev)

## Introduction
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

Once you've defined the `NavigationKey` for a screen, you'll want to use it. In any Activity, Fragment or Composable, you will be able to get access to a `NavigationHandle`, which allows you to perform navigation. The syntax is slightly different for each type of screen: 
```kotlin
// Fragments and Activities use the same syntax
class ExampleFragment : Fragment() {
   val selectDate by registerForNavigationResult<LocalDate> { selectedDate: LocalDate -> 
     /* do something! */ 
   }
   
   fun onSelectDateButtonPressed() = selectDate.present(
     SelectDate(maxDate = LocalDate.now())
   )
   
   fun onProfileButtonPressed() = getNavigationHandle().push(
     ShowUserProfile(userId = /* ... */)
   )
}

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

You might have noticed that we've defined our `ExampleFragment` and `ExampleComposable` in the example above before we've even begun to think about how we're going to implement the `ShowUserProfile` and `SelectDate` destinations. That's because implementing a `NavigationDestination` in Enro is the least interesting part of the process. All you need to do to make this application complete is to build an Activity, Fragment or Composable, and mark it as the `NavigationDestination` for a particular `NavigationKey`.

The recommended approach to mark an Activity, Fragment or Composable as a `NavigationDestination` is to use the Enro annotation processor and the `@NavigationDestination` annotation:
```kotlin
@NavigationDestination(ShowUserProfile::class)
class ProfileFragment : Fragment {
    val navigation by navigationHandle<ShowProfile>() // you can use `navigation.key` will be the ShowUserProfile instance used to open this destination
}

@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateComposable() { 
   val navigation = navigationHandle<SelectDate>() // you can use `navigation.key` will be the SelectDate instance used to open this destination
    // ...
   Button(onClick = {
       navigation.closeWithResult( /* pass a local date here to return that as a result */ )
   }) { /* ... */ }
}
```

If you'd prefer to avoid annotation processing, you can use a DSL to define these bindings when creating your application (see [here]() for more information):
```kotlin
// this needs to be registered with your application
val exampleNavigationComponent = createNavigationComponent {
   fragmentDestination<ShowProfile, ProfileFragment>() 
   composableDestination<SelectDate> { SelectDateComposable() }
}
```
