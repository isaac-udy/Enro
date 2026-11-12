---
title: Testing
parent: Advanced Topics
nav_order: 4
---

# Testing

The `enro-test` module provides:

- `runEnroTest` and `EnroTestRule`, which install a navigation controller
  for the duration of a test.
- `TestNavigationHandle`, which records each navigation operation and does
  not execute the operation on a container.
- Typed assertions on the recorded operations.
- `putNavigationHandleForViewModel`, which provides a `TestNavigationHandle`
  to a ViewModel under test.

Add the dependency:

<!-- snippet: skip (gradle) -->
```kotlin
dependencies {
    testImplementation("dev.enro:enro-test:3.0.0")
}
```

## Installing a test controller

### `runEnroTest`

`runEnroTest` installs a navigation controller, runs the block, then
uninstalls the controller. `runEnroTest` is declared in common code and can
be called from common, JVM and instrumented tests:

<!-- snippet: testing/run-enro-test -->
```kotlin
@Test
fun open_profile_navigates_to_edit_screen() = runEnroTest {
    val handle = createTestNavigationHandle(ShowProfile("user-123"))

    handle.open(EditProfile("user-123"))

    handle.assertOpened<EditProfile>()
}
```

### `EnroTestRule`

`EnroTestRule` is a JUnit `TestRule` that runs each test inside
`runEnroTest`. `EnroTestRule` is available on Android only:

<!-- snippet: testing/enro-test-rule -->
```kotlin
@get:Rule(order = 0)
val enroRule = EnroTestRule()

@get:Rule(order = 1)
val composeRule = createComposeRule()
```

Give `EnroTestRule` a lower `order` than any rule that launches an Activity
or Fragment. The controller must be installed before a destination is
created.

## TestNavigationHandle

`createTestNavigationHandle` takes a key or a `NavigationKey.Instance` and
returns a `TestNavigationHandle<T>`. Call the `open`, `close`, `complete`
and `requestClose` extensions on the `TestNavigationHandle` as on any
`NavigationHandle`. The `TestNavigationHandle` records each operation and
does not execute the operation on a container:

<!-- snippet: testing/test-navigation-handle -->
```kotlin
val handle = createTestNavigationHandle(ShowProfile("user-123"))

handle.open(SelectDate())
handle.complete()

// both operations are recorded on the handle
```

A `TestNavigationHandle` records `Open`, `Close` and `Complete` operations.
Each operation inside an `AggregateOperation` is recorded separately.
`SideEffect` operations are not recorded.

A `TestNavigationHandle` throws on any operation executed after the
handle's own instance is closed or completed. Call
`handle.clearOperationHistory()` to use the same handle for another
scenario.

## Assertions

The assertions read the operations recorded by a `TestNavigationHandle`.

### `assertOpened<T>()`

<!-- snippet: testing/assert-opened -->
```kotlin
// Returns the opened NavigationKey.Instance.
val instance = handle.assertOpened<EditProfile>()

handle.assertOpened(EditProfile("user-123"))

// The predicate receives the NavigationKey.Instance.
handle.assertOpened<EditProfile> { it.key.initial == "user-123" }

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

`assertCompleted<R> { }` checks the result with a predicate instead of an
exact value.

### Container assertions

`assertActive` checks the last entry of a `NavigationContainerState`'s
backstack and returns that entry's `NavigationKey.Instance`. The predicate
receives the key:

<!-- snippet: testing/container-assertions -->
```kotlin
state.assertActive<ShowProfile>()
state.assertActive(ShowProfile("user-123"))
state.assertActive<ShowProfile> { it.userId == "user-123" }
```

`NavigationContainerFixtures.create(...)` creates a container without
Compose. `create` takes the same backstack, filter, interceptor and empty
behaviour as `rememberNavigationContainer`. The backstack assertions check
the container's backstack after an operation:

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
| `assertBackstackSize(n)` | The backstack has `n` entries. |
| `assertBackstackKeys(a, b)` | The backstack's keys equal `a, b`, in order. |
| `assertBackstackContains<T> { }` | The backstack has an entry of type `T`. The predicate receives the key. Returns the first matching instance. |
| `assertBackstackDoesNotContain<T>()` | The backstack has no entry of type `T`. |
| `assertBackstackEmpty()` | The backstack has no entries. |
| `assertActive<T>()` | The last entry of the backstack is of type `T`. |

`NavigationContextFixtures` creates root, container and destination contexts
for tests that need a specific context tree.

### Path assertions

Install the path bindings under test. `assertPathResolvesTo` checks that a
path resolves to a key, and `assertPathFor` checks that a key produces a
path:

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

## Testing ViewModels

`putNavigationHandleForViewModel<MyVm, MyKey>(key)` creates a
`TestNavigationHandle` for `key` and returns the handle.
`by navigationHandle<MyKey>()` and `by registerForNavigationResult { }` in
the next `MyVm` constructed use that handle. Call
`putNavigationHandleForViewModel` before constructing the ViewModel:

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

The handle stays registered for the ViewModel type until another handle is
put for the same type, or until the test controller is uninstalled. Call
`putNavigationHandleForViewModel` inside `runEnroTest`, or in a test class
that has an `EnroTestRule`.

### Simulating child results

When a ViewModel opens a child destination through a
`NavigationResultChannel`, `assertOpened` returns the child's
`NavigationKey.Instance`. The `send*ForTest` extensions on that instance
simulate the child completing or closing:

<!-- snippet: testing/child-results -->
```kotlin
// Calls onCompleted with "confirmed".
val child = handle.assertOpened<ConfirmDestination>()
child.sendResultForTest("confirmed")

// Calls an onCompleted that takes no result.
val dialog = handle.assertOpened<AcknowledgeDialog>()
dialog.sendCompletedForTest()

// Calls onClosed.
val picker = handle.assertOpened<DatePicker>()
picker.sendClosedForTest()
```

A ViewModel's result channel collects results in `viewModelScope`, which
dispatches on `Dispatchers.Main`. Replace `Dispatchers.Main` with an
unconfined dispatcher so that the result callbacks run immediately:

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

`runEnroTest` and `EnroTestRule` install a new `EnroController` for the
test and uninstall the controller afterwards. The new controller has no
destinations and no path bindings. Add the destinations and path bindings a
test needs with `installNavigationModule(module)` or
`installPathBindings(...)`.

When an `EnroController` is already installed, `runEnroTest` and
`EnroTestRule` use the installed controller and leave the controller
installed afterwards. An Android instrumented test whose `Application`
installs Enro is one such case.

## See also

- [Navigation Handles](../core-concepts/navigation-handles.md) — the
  operations that the assertions match.
- [Results](results.md) — result channels.
