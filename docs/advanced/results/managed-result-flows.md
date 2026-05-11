---
title: Managed Result Flows
parent: Results
grand_parent: Advanced Topics
nav_order: 2
---

# Managed Result Flows

A managed flow lets you define a multi-step navigation sequence as
**straight-line code**. Each step is a result-producing destination; the
flow scope's `open(key)` looks like a suspending function call that returns
the destination's result. Under the hood, Enro runs the steps, persists
intermediate state, and re-evaluates the flow on every result so the code
reads top-to-bottom.

This is the pattern to reach for when an [embedded result
flow](embedded-result-flows.md) would have too many nested callbacks or too
much manual intermediate state.

```kotlin
@OptIn(ExperimentalEnroApi::class)
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
        onCompleted = { details -> /* ... */ },
    )
```

The complete worked version is the [managed flow recipe][managedflow-recipe].

## The mental model

Inside the `flow = { }` block:

- `open(key)` returns the value of `key`'s `WithResult<R>` type. The block
  is re-evaluated each time a result comes in, so by the time `open` "returns,"
  Enro has the value.
- The block's *return value* is the flow's overall result, delivered to
  whoever opened the flow.
- The keys you `open` are registered as steps of the flow. Enro renders each
  one in the flow's own container as the user reaches it.
- If the user backs up the flow, Enro re-evaluates from the top with the
  preserved results for the earlier steps still in scope.
- `open(NavigationKey)` (without a `WithResult`) is also supported for
  non-result destinations you want to step through.

The flow is the destination — `managedFlowDestination<KeyType, ResultType>`
produces a `NavigationDestinationProvider<KeyType>` that you bind to a
`NavigationKey.WithResult<ResultType>`. Callers open it like any other
result-producing destination through `registerForNavigationResult`.

## Branching

Because the flow body is plain Kotlin, branches and loops are exactly what
they look like:

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

When a branch is taken, only the keys actually opened in that branch become
steps of the flow. If the user backs up and re-evaluates into the other
branch, the steps for the abandoned branch are discarded.

## When `open` returns

`open(KeyWithResult)` participates in the flow's state machine — it doesn't
"suspend" in the coroutine sense. When the flow scope encounters an `open`
for a step that hasn't produced a result yet, it throws a sentinel that the
flow infrastructure catches; this is how the flow knows where to pause and
wait. **Don't** put `open` calls inside `try / catch` blocks that catch
`Throwable` — you'll break the flow's control flow. Catch specific
exceptions only.

## Persistence

Each managed-flow step's result is persisted with the flow's saved state.
If the process is killed and restored, the flow resumes at the same step
with all previous results intact.

## See also

- [Embedded result flows](embedded-result-flows.md) — when callbacks are
  enough.
- [Managed flow recipe][managedflow-recipe] — full runnable example.

[managedflow-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/managedflow/ManagedFlow.kt
