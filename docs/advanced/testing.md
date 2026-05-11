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

```kotlin
dependencies {
    testImplementation("dev.enro:enro-test:3.0.0-alpha10")
}
```

## Two ways to install a test controller

### `runEnroTest { }` (KMP-friendly)

`runEnroTest` is a plain function that installs a navigation controller
for the duration of the block. Use this in any Kotlin test:

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

```kotlin
val handle = createTestNavigationHandle(ShowProfile("user-123"))

handle.open(SelectDate())
handle.complete()

// every operation is recorded on `handle.operations`
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

```kotlin
// Any open of type T
val instance = handle.assertOpened<EditProfile>()

// A specific key value
handle.assertOpened(EditProfile("user-123"))

// A predicate over the instance
handle.assertOpened<EditProfile> { it.key.userId == "user-123" }

// "Nothing was opened"
handle.assertNoneOpened()
```

### `assertClosed()` / `assertCompleted()`

```kotlin
handle.assertClosed()
handle.assertNotClosed()

handle.assertCompleted()
handle.assertCompleted(LocalDate.now())            // with a specific result
handle.assertCompleted<LocalDate> { it.year > 2020 }
handle.assertNotCompleted()
```

`assertCompleted<R>(predicate)` is the one to reach for when you want to
verify the *kind* of result without pinning down the exact value.

### Container assertions

For tests that build a real `NavigationContainerState` (using the test
fixtures), there's a matching set of container-level assertions:

```kotlin
val state = /* container state */
state.assertActive<ShowProfile>()
state.assertActive(ShowProfile("user-123"))
state.assertActive<ShowProfile> { it.key.userId == "user-123" }
```

`assertActive` checks the currently-rendered destination at the top of the
backstack.

## Testing ViewModels

To unit-test a ViewModel that uses `by navigationHandle<MyKey>()` or
`by registerForNavigationResult { }`, use
`putNavigationHandleForViewModel<MyVm, MyKey>(key)` to inject a recording
handle:

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

## Strict mode

When `runEnroTest { }` or `EnroTestRule` is active, attempting to perform
"real" navigation outside a `TestNavigationHandle` will be blocked. This is
deliberate — the test utilities are designed for **isolated** testing of a
single destination or ViewModel. If you want full app navigation in an
instrumented test, don't install the test controller; let the app's
real controller drive it.

## See also

- [Navigation Handles](../core-concepts/navigation-handles.md) for the
  operations the assertions match against.
- [Results](results.md) for the channel/operation interaction.
