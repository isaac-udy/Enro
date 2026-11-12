---
title: Testing
parent: Advanced Topics
nav_order: 4
---

# Testing

Enro's test utilities live in the `enro-test` module and let you write
tests that:

- Install a controller for the duration of a test (so destinations and
  handles can be created without a full app instance).
- Construct a `TestNavigationHandle` that records every navigation
  operation a handle executes, instead of dispatching to a real container.
- Assert against those recorded operations with typed helpers.
- Inject a navigation handle into a `ViewModel` so the ViewModel can be
  unit-tested in isolation.

Add the dependency:

<!-- snippet: skip (gradle) -->
```kotlin
dependencies {
    testImplementation("dev.enro:enro-test:3.0.0")
}
```

## Two ways to install a test controller

### `runEnroTest { }` (KMP-friendly)

`runEnroTest` is a plain function that installs a navigation controller
for the duration of the block. Use this in any Kotlin test:

<!-- snippet: testing/run-enro-test -->
```kotlin
@Test
fun open_profile_navigates_to_edit_screen() = runEnroTest {
    val handle = createTestNavigationHandle(ShowProfile("user-123"))

    handle.open(EditProfile("user-123"))

    handle.assertOpened<EditProfile>()
}
```

This is the recommended form. It works in common test source sets,
instrumented tests, and JVM unit tests alike.

### `EnroTestRule` (JUnit)

If your test class uses JUnit-style `@Rule`s, `EnroTestRule` does the same
thing as `runEnroTest` but as a `TestRule`:

<!-- snippet: testing/enro-test-rule -->
```kotlin
@get:Rule(order = 0)
val enroRule = EnroTestRule()

@get:Rule(order = 1)
val composeRule = createComposeRule()
```

If you have other `@Rule`s that launch activities or fragments, put the
`EnroTestRule` first by ordering — the controller has to be installed
before any destination is created.

## TestNavigationHandle

Build a `TestNavigationHandle<T>` for a key (or instance) and call the
ordinary `open` / `close` / `complete` / `requestClose` extensions on it.
The handle records every operation rather than dispatching to a container:

<!-- snippet: testing/test-navigation-handle -->
```kotlin
val handle = createTestNavigationHandle(ShowProfile("user-123"))

handle.open(SelectDate())
handle.complete()

// both operations are recorded on the handle
```

The recorded operations are `NavigationOperation.RootOperation`s — `Open`,
`Close`, `Complete`, `CompleteFrom`, `SetBackstack`. Aggregated
operations (`AggregateOperation`) are flattened to their constituent root
operations when recorded.

Once a handle has been closed or completed, it rejects further operations.
Call `handle.clearOperationHistory()` to re-use the same handle for
multiple sequential scenarios.

## Assertions

The handle exposes typed assertions for each common shape.

### `assertOpened<T>()`

<!-- snippet: testing/assert-opened -->
```kotlin
// Any open of type T
val instance = handle.assertOpened<EditProfile>()

// A specific key value
handle.assertOpened(EditProfile("user-123"))

// A predicate over the instance
handle.assertOpened<EditProfile> { it.key.initial == "user-123" }

// "Nothing was opened"
handle.assertNoneOpened()
```

### `assertClosed()` / `assertCompleted()`

<!-- snippet: testing/assert-closed-completed -->
```kotlin
handle.assertClosed()
handle.assertNotClosed()

handle.assertCompleted()
handle.assertCompleted(today)                      // with a specific result
handle.assertCompleted<LocalDate> { it.year > 2020 }
handle.assertNotCompleted()
```

`assertCompleted<R>(predicate)` is the one to reach for when you want to
verify the *kind* of result without pinning down the exact value.

### Container assertions

For tests that build a real `NavigationContainerState` (using the test
fixtures), there's a matching set of container-level assertions. `state` is
the `NavigationContainerState`, and a predicate receives the key:

<!-- snippet: testing/container-assertions -->
```kotlin
state.assertActive<ShowProfile>()
state.assertActive(ShowProfile("user-123"))
state.assertActive<ShowProfile> { it.userId == "user-123" }
```

`NavigationContainerFixtures.create(...)` makes a container without Compose.
It takes the same backstack, filter, interceptor and empty behaviour as
`rememberNavigationContainer`. The backstack assertions check what the
container holds after an operation:

<!-- snippet: testing/container-fixture -->
```kotlin
@Test
fun opening_a_profile_adds_it_to_the_backstack() = runEnroTest {
    val container = NavigationContainerFixtures.create(
        backstack = backstackOf(Home.asInstance()),
    )

    container.execute(NavigationOperation.Open(ShowProfile("user-123").asInstance()))

    container.assertBackstackSize(2)
    container.assertBackstackKeys(Home, ShowProfile("user-123"))
    container.assertBackstackContains<ShowProfile> { it.userId == "user-123" }
    container.assertBackstackDoesNotContain<ProductDetail>()
    container.assertActive<ShowProfile>()
}
```

| Assertion | Checks |
|---|---|
| `assertBackstackSize(n)` | The number of entries. |
| `assertBackstackKeys(a, b)` | The keys, in order. |
| `assertBackstackContains<T> { }` | An entry of type `T`; the predicate receives the key. Returns the instance. |
| `assertBackstackDoesNotContain<T>()` | No entry of type `T`. |
| `assertBackstackEmpty()` | No entries. |
| `assertActive<T>()` | The top entry. |

`NavigationContextFixtures` makes root, container and destination contexts
for tests that need a specific tree.

### Path assertions

Install the bindings under test, then check both directions:

<!-- snippet: testing/path-assertions -->
```kotlin
@Test
fun product_paths_resolve() = runEnroTest {
    installPathBindings(productDetailBinding)
    val controller = EnroTest.getCurrentNavigationController()

    controller.assertPathResolvesTo<ProductDetail>("/products/42") { it.productId == "42" }
    controller.assertPathDoesNotResolve("/unknown")
    controller.assertPathFor(ProductDetail("42"), expectedPath = "/products/42")
}
```

`assertActive` checks the currently-rendered destination at the top of the
backstack.

## Testing ViewModels

To unit-test a ViewModel that uses `by navigationHandle<MyKey>()` or
`by registerForNavigationResult { }`, use
`putNavigationHandleForViewModel<MyVm, MyKey>(key)` to inject a recording
handle:

<!-- snippet: testing/view-model -->
```kotlin
@Test
fun saving_changes_completes_the_destination() = runEnroTest {
    val handle = putNavigationHandleForViewModel<EditProfileViewModel, EditProfile>(
        EditProfile(initial = "Hello"),
    )

    val viewModel = EditProfileViewModel()
    viewModel.onSaveClicked()

    handle.assertCompleted()
}
```

The injected handle survives until you put a new one for the same ViewModel
type. Pair it with `runEnroTest` (or `EnroTestRule`) so the underlying
controller is set up.

### Simulating child results

When a ViewModel opens a child destination through a `NavigationResultChannel`,
use the `send*ForTest` extensions on the child's `NavigationKey.Instance`
(returned by `assertOpened`) to simulate the child completing or closing:

<!-- snippet: testing/child-results -->
```kotlin
// sendResultForTest — typed result
val child = handle.assertOpened<ConfirmDestination>()
child.sendResultForTest("confirmed")

// sendCompletedForTest — Unit/no-payload completion
val dialog = handle.assertOpened<AcknowledgeDialog>()
dialog.sendCompletedForTest()

// sendClosedForTest — the child was dismissed without a result
val picker = handle.assertOpened<DatePicker>()
picker.sendClosedForTest()
```

Result channels observe in `viewModelScope`, which dispatches on `Dispatchers.Main`.
In tests, replace Main with an unconfined dispatcher so callbacks fire eagerly:

<!-- snippet: testing/main-dispatcher -->
```kotlin
@BeforeTest
fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
}

@AfterTest
fun tearDown() {
    Dispatchers.resetMain()
}
```

## The test controller

`runEnroTest { }` and `EnroTestRule` install a new, empty `EnroController`
for the test and remove it afterwards. No destinations or path bindings
are registered on it. Add the ones a test needs with
`installNavigationModule(module)` or `installPathBindings(...)`.

On Android instrumented tests, where the `Application` has already installed
Enro, `runEnroTest` and `EnroTestRule` use that controller and leave it
installed afterwards.

## See also

- [Navigation Handles](../core-concepts/navigation-handles.md) for the
  operations the assertions match against.
- [Results](results.md) for the channel/operation interaction.
