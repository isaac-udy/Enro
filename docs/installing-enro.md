# Installing Enro

## Add the Gradle Dependencies 
The first step in installing Enro is to add the Gradle dependencies to your project. These should be added to your application module, as well as any modules that will use Enro. 

[![Maven Central](https://img.shields.io/maven-central/v/dev.enro/enro.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.enro%22)
```kotlin
dependencies {
    implementation("dev.enro:enro:<latest_version>")
    kapt("dev.enro:enro-processor:<latest_version>") // optional but highly recommended
    testImplementation("dev.enro:enro-test:<latest_version>") // optional test extensions
}
```

## Add Enro to your Application class
Enro needs to be added to the Application class used by your Application. We'll look at how to do this step-by-step.

**0. An application class without Enro installed**
```kotlin
class ExampleApplication : Application {
    // ...
}
```

**1. Add the `NavigationApplication` interface**
```kotlin
class ExampleApplication : Application, NavigationApplication {
```
{:.code-important .code-start}
```kotlin
    // ...
}
```
{:.code-not-important .code-end}

**2. Override the `navigationController` property**
```kotlin
class ExampleApplication : Application, NavigationApplication {
```
{:.code-not-important .code-start}
```kotlin
    override val navigationController = createNavigationController { }
```
{:.code-important}
```kotlin

    // ...
}
```
{:.code-not-important .code-end}

In the example above we're passing an empty block to the `createNavigationController` function, but this block is used to provide configuration to Enro. In a simple application that uses annotation processing, you may not need to provide any configuration, but it's useful to be aware that this is what the block is used for. Please see [Configuring Enro](./configuring-enro.md) for more information.

**3. Add the `@NavigationComponent` annotation to your Application (if using kapt/annotation processing)**
```kotlin
@NavigationComponent
```
{:.code-important .code-start}
```kotlin
class ExampleApplication : Application, NavigationApplication {
    override val navigationController = createNavigationController { }
    
    // ...
}
```
{:.code-not-important .code-end}

If you are using annotation processing (which is optional, but recommended), you are required to annotate your Application class with `@NavigationComponent` so that the annotation processor has a hook to generate and provide configuration. 

If you are not using annotation processing, you won't need to add this annotation. Instead, you'll need to provide your Application's configuration within the `createNavigationController` block. Please see [Configuring Enro](./configuring-enro.md) for more information.

## Add Enro to an Activity
Once you've added Enro to your Application, it's likely that you'll want to add a Navigation Container to an Activity. This isn't necessary, as navigation using Enro will work even without a Navigation Container, but it is recommended. The exact configuration of the Navigation Container will depend on your needs, and the examples below will deal with a reasonably simple case, so if you need more information on how to configure a Navigation Container, please see the [Navigation Container documentation](./navigation-containers.md).

**What is a Navigation Container?**

A Navigation Container is a ViewGroup or Composable that maintains a backstack and displays the active Navigation Destination for that backstack. If you're familiar with Fragments, think of it as the `FrameLayout` that holds the Fragments. If you're more familiar with Compose, think of it as a `Box` that holds some child content (the active destination). For more information, please see the [Navigation Container documentation](./navigation-containers.md).

<details markdown="block">
  <summary>
    Adding a Navigation Container for Fragments and Composables
  </summary>
  {: .text-gamma }
If your application has Navigation Destinations that are a mix of Fragments and Composables, your top level Navigation Container should be a View based Navigation Container, as this will accept both Fragment and Composable destinations. 

**0. An Activity without a Navigation Container**
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```

**1. Add a FrameLayout to your Activity's layout file**
```xml
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
```
{:.code-not-important .code-start}
```xml
    <FrameLayout
        android:id="@+id/exampleNavigationContainer"
        />
```
{:.code-important}
```xml
    <!-- ... -->
</androidx.constraintlayout.widget.ConstraintLayout>
```
{:.code-not-important .code-end}

**2. Add a Navigation Container property to your Activity**
```kotlin
class MainActivity : AppCompatActivity() {

```
{:.code-not-important .code-start}
```kotlin
    private val exampleContainer by navigationContainer(
        containerId = R.id.exampleNavigationContainer,
    )
```
{:.code-important}
```kotlin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```
{:.code-not-important .code-end}

**3. Configure the Navigation Container**

The Navigation Container that we've defined above will start off with nothing in it, and it will allow any Navigation Destination to be pushed into it. Below is an example of a configured Navigation Container that will initially show the Navigation Destination for a particular Navigation Key, and will `finish` the Activity if the Navigation Container is ever about to become empty. This isn't always the behavior that you will want for a Navigation Container, but it is a reasonably common way to set up an Activity's root Navigation Container. For more information, please see the [Navigation Container documentation](./navigation-containers.md).

```kotlin
class MainActivity : AppCompatActivity() {

    private val exampleContainer by navigationContainer(
        containerId = R.id.exampleNavigationContainer,
```
{:.code-not-important .code-start}
```kotlin
        root = { ExampleRootNavigationKey(/* ... */) }, 
        emptyBehavior = EmptyBehavior.CloseParent,
```
{:.code-important}
```kotlin
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```
{:.code-not-important .code-end}
</details>


<details markdown="block">
  <summary>
    Adding a Navigation Container for Composables only
  </summary>
  {: .text-gamma }
If your application only has Composable destinations, you can choose to use a View based Navigation Container (as these support Composable destinations too), but you may want to consider directly using a Composable NavigationContainer.

**0. A Composable Activity without a Navigation Container**
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ...
        }
    }
}
```

**1. Add a Navigation Container variable**
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
```
{:.code-not-important .code-start}
```kotlin
            val container = rememberNavigationContainer()
```
{:.code-important}
```kotlin
            // ...
        }
    }
}
```

**2. Render the Navigation Container**
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer()
            
```
{:.code-not-important .code-start}
```kotlin
            Box(modifier = Modifier.fillMaxSize()) {
                container.Render()
            }
```
{:.code-important}
```kotlin
            // ...
        }
    }
}
```

**3. Configure the Navigation Container**

The Navigation Container that we've defined above will start off with nothing in it, and it will allow any Navigation Destination to be pushed into it. Below is an example of a configured Navigation Container that will initially show the Navigation Destination for a particular Navigation Key, and will `finish` the Activity if the Navigation Container is ever about to become empty. This isn't always the behavior that you will want for a Navigation Container, but it is a reasonably common way to set up an Activity's root Navigation Container. For more information, please see the [Navigation Container documentation](./navigation-containers.md).

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(
```
{:.code-not-important .code-start}
```kotlin
                root = ExampleRootNavigationKey(/* ... */), 
                emptyBehavior = EmptyBehavior.CloseParent,
```
{:.code-important}
```kotlin
            )

            Box(modifier = Modifier.fillMaxSize()) {
                container.Render()
            }
            // ...
        }
    }
}
```
</details>