# `enro-test`

Test helpers for Enro destinations and the runtime as a whole. Lets you
exercise navigation behaviour from a unit test — without spinning up an
Android instrumentation harness — by installing a controllable
`EnroController` for the duration of the test and exposing assertion
helpers around handles, backstacks, paths, operations, and synthetic
destinations.

Targets the same set as `enro-runtime` (Android, iOS, JVM Desktop,
WASM JS), so multiplatform code can be tested on any host the runtime
supports.

## What's in here

- **`runEnroTest { … }`** — installs an isolated controller per test
  and tears it down afterwards. Use this as the outer wrapper for any
  test that needs the runtime.
- **`TestNavigationHandle`** — a handle implementation that records
  every operation executed against it. Combine with `createTestNavigationHandle(key)`
  for tests of destination logic that doesn't need the full backstack.
- **Backstack / path / operation assertions** — `assertBackstackKeys`,
  `assertBackstackSize`, `assertBackstackContains<T>`, `assertPathResolvesTo<T>`,
  `assertOperationSequence(*KClass)`, `lastOperationOfType<T>()`.
- **Synthetic destination tester** — `testSyntheticDestination(key)` /
  `testSyntheticDestination(key, provider)` plus an outcome DSL
  (`assertOpens<T>`, `assertCompletesFrom<T>`, `assertCloses`,
  `assertCompletes`, `assertSideEffect`). Lets you unit-test a synthetic's
  decision without rendering it.
- **Fixtures** — `NavigationContextFixtures`, `NavigationContainerFixtures`,
  `NavigationKeyFixtures` for building the bits of state a test needs.
- **`installNavigationModule(module)` / `installPathBindings(vararg)`**
  — one-line shortcuts for registering destinations or path bindings on
  the test controller.

## Typical usage

```kotlin
dependencies {
    testImplementation("dev.enro:enro-test:3.0.0-beta01")
}
```

```kotlin
@Test
fun `profile button opens profile destination`() = runEnroTest {
    val handle = createTestNavigationHandle(HomeKey)
    HomeViewModel(handle).onProfileClicked()

    handle.assertOpened<ProfileKey> { it.key.userId == "user-123" }
}
```

For end-to-end Compose tests of `NavigationDisplay` and friends, pair
this module with `androidx.compose.ui:ui-test` and `runComposeUiTest`
— see `enro-runtime`'s `SceneIntegrationTests` for examples.
