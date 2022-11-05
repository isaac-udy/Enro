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