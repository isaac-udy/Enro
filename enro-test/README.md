# `enro-test`

`enro-test` contains test helpers for Enro destinations and the Enro
runtime. A unit test installs a test `EnroController` for the duration of
the test and asserts on navigation handles, backstacks, paths, operations
and synthetic destinations. The test does not require Android
instrumentation.

`enro-test` targets the same platforms as `enro-runtime`: Android, iOS,
JVM Desktop, WASM JS and Kotlin/JS. `EnroTestRule` is Android only.

## Contents

- **`runEnroTest { … }`** — installs a controller before the block and
  uninstalls the controller after the block. Wrap the body of each test
  that requires the runtime in `runEnroTest`.
- **`TestNavigationHandle`** — a `NavigationHandle` that records every
  operation executed on the handle. `createTestNavigationHandle(key)`
  creates a `TestNavigationHandle` for a test of destination logic that
  does not require a container.
- **Backstack, path and operation assertions** — `assertBackstackKeys`,
  `assertBackstackSize`, `assertBackstackContains<T>`, `assertPathResolvesTo<T>`,
  `assertOperationSequence(*KClass)`, `lastOperationOfType<T>()`.
- **Synthetic destination tester** — `testSyntheticDestination(key)` and
  `testSyntheticDestination(key, provider)` run a synthetic destination
  without rendering UI. The outcome assertions are `assertOpens<T>`,
  `assertCompletesFrom<T>`, `assertCloses`, `assertCompletes` and
  `assertSideEffect`.
- **Fixtures** — `NavigationContextFixtures`, `NavigationContainerFixtures`
  and `NavigationDestinationFixtures` create the contexts, containers
  and destinations that a test requires.
- **`installNavigationModule(module)` / `installPathBindings(vararg)`**
  — register destinations or path bindings on the test controller.

## Usage

```kotlin
dependencies {
    testImplementation("dev.enro:enro-test:3.0.0")
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

For a Compose test of `NavigationDisplay`, use `enro-test` with
`androidx.compose.ui:ui-test` and `runComposeUiTest`.
`SceneIntegrationTests` in `enro-runtime` is an example.
