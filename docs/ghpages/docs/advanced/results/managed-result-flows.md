---
title: Managed Result Flows
parent: Results
grand_parent: Advanced Topics
nav_order: 2
---

# Managed Result Flows

`managedFlowDestination` declares a multi-step flow as sequential code.
Each step is a destination. Inside the `flow` block, `open(key)` returns
the result of the step's destination. Enro displays the steps, saves each
step's result, and runs the `flow` block again after each result.

Use a managed result flow when an
[embedded result flow](embedded-result-flows.md) would require nested
callbacks or several pieces of intermediate state.

<!-- snippet: managed-result-flows/flow -->
```kotlin
@NavigationDestination(BookingFlow::class)
val bookingFlowDestination: NavigationDestinationProvider<BookingFlow> =
    managedFlowDestination<BookingFlow, BookingDetails>(
        flow = {
            val destination = open(SelectFlightDestination)
            val date        = open(SelectDate)
            val passengers  = open(SelectPassengers(maxPassengers = 5))

            val seats = when {
                passengers > 3 -> listOf("Group Seating")
                else           -> listOf("12A", "12B", "14C")
            }
            val seat = open(SelectSeat(availableSeats = seats))

            BookingDetails(destination, date, passengers, seat)
        },
    )
```

The [managed flow recipe][managedflow-recipe] is a complete, runnable flow.

## How the `flow` block runs

- Enro runs the `flow` block from the top after each step's result.
  `open(key)` returns the saved result of a step that has a result, and
  stops the block at the first step that has no result.
- `open(key)` returns a value of the result type of a
  `NavigationKey.WithResult<R>` key.
- `open(key)` also accepts a key that does not implement `WithResult`. The
  step has a result when the step's destination completes.
- Each key passed to `open` is a step of the flow. Enro displays the steps
  in a container that belongs to the flow's destination.
- The value that the `flow` block returns is the result of the flow. The
  flow's destination completes with that value.
- Closing a step clears that step's result and displays the previous step.
  The results of the earlier steps are kept.

`managedFlowDestination<KeyType, ResultType>` returns a
`NavigationDestinationProvider<KeyType>`. Bind the provider to a
`NavigationKey.WithResult<ResultType>` key with `@NavigationDestination`.
A caller opens the flow through `registerForNavigationResult`, as for any
other destination with a result.

## Branching

The `flow` block is Kotlin code and can contain `if`, `when` and loops:

<!-- snippet: managed-result-flows/branching -->
```kotlin
flow = {
    val name = open(EnterName)
    if (name == "admin") {
        val token = open(RequestAdminToken)
        AdminAccount(name, token)
    } else {
        StandardAccount(name)
    }
}
```

Only the keys opened by the branch that runs become steps of the flow. When
the user returns to an earlier step and the new result selects the other
branch, Enro removes the steps of the first branch from the flow's
backstack.

## `open` and `try` / `catch`

`open(key)` is not a suspending function. For a step that has no result,
`open` throws an internal `RuntimeException`. The flow catches that
exception and waits for the step's result. A `try` / `catch` around an
`open` call that catches `RuntimeException`, `Exception` or `Throwable`
stops the flow from waiting at the step. Catch specific exception types
only.

## Saved state

Enro saves each step's result in the saved state of the flow's destination.
After process death, the flow is restored at the same step with the results
of the earlier steps.

## See also

- [Embedded result flows](embedded-result-flows.md) — a short sequence of
  result destinations opened from callbacks.
- [Flows in a ViewModel](navigation-flows.md) — a flow displayed inside a
  screen's own UI.
- [Managed flow recipe][managedflow-recipe].

[managedflow-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/managedflow/ManagedFlow.kt
