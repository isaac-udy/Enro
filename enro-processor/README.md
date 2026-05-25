# `enro-processor`

The KSP / kapt code generator that turns `@NavigationDestination`,
`@NavigationComponent`, and `@NavigationPath` annotations into the glue
the runtime consumes. Apply it as a `ksp("…")` dependency on any module
that declares Enro destinations; it generates:

- A per-component **`*Navigation`** class that registers every annotated
  destination (and path binding) with the runtime when you call
  `installNavigationController(…)`. This is what lets you write a
  destination once and have it discovered automatically — no manual
  `registerDestination(…)` boilerplate.
- **Path-binding** glue for `@NavigationPath`, including the
  serialisers needed to bridge URL segments to typed `NavigationKey`
  properties.
- Per-destination metadata (key type, render kind, optional metadata
  overrides) so the runtime can resolve `open(KeyType)` calls without
  reflection at runtime.

The processor itself is pure JVM — it has no multiplatform target — but
its output is multiplatform-aware and lands in the right source set per
target.

## Typical usage

You don't usually add `enro-processor` to a module directly. The
`dev.enro:enro` meta-artefact takes care of pulling it in alongside the
runtime; the KSP wiring you write looks like:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("dev.enro:enro:3.0.0-alpha10")
    ksp("dev.enro:enro-processor:3.0.0-alpha10")
}
```

For multi-target KMP modules, attach the processor to every target source
set that declares destinations (e.g. `kspCommonMainMetadata`,
`kspAndroid`, `kspIosArm64`, etc.). The output classes are then visible
on each platform.

## When to depend on this directly

Only when you're building tooling that needs to invoke the processor
outside the standard `ksp(…)` flow (e.g. a custom build plugin that
generates extra glue from the same annotations).
