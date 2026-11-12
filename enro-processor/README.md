# `enro-processor`

`enro-processor` is the KSP processor that reads `@NavigationDestination`,
`@NavigationComponent` and `@NavigationPath` annotations. Add
`enro-processor` as a `ksp("…")` dependency to each module that declares
Enro destinations. `enro-processor` generates:

- A **binding** class for each `@NavigationDestination`. The binding
  registers the destination for the destination's key type.
- **Path bindings** for each `@NavigationPath`, including the code that
  converts URL segments and query parameters to and from the
  `NavigationKey` properties.
- A **`<Component>Navigation`** class and an
  `installNavigationController(…)` extension for each
  `@NavigationComponent`. `installNavigationController(…)` registers
  every generated binding with the controller.

`enro-processor` is a JVM module with no multiplatform targets. KSP runs
`enro-processor` once for each target that has a `ksp` configuration.

## Usage

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("dev.enro:enro:3.0.0")
    ksp("dev.enro:enro-processor:3.0.0")
}
```

`dev.enro:enro` does not include `enro-processor`. In a KMP module, add
`enro-processor` to the KSP configuration of each target that declares
destinations, for example `kspCommonMainMetadata`, `kspAndroid` and
`kspIosArm64`.

## Depending on `enro-processor` as a library

Depend on `enro-processor` outside a `ksp(…)` configuration only to
build tooling that runs the processor itself, such as a custom build
plugin that generates extra code from the same annotations.
