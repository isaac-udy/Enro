---
title: Flows in a ViewModel
parent: Results
grand_parent: Advanced Topics
nav_order: 3
---

# Flows in a ViewModel

`registerForFlowResult` describes a multi-step flow as sequential code in a
ViewModel. The screen that hosts the flow shows the steps in a container of
its own, so it can put its own UI around them: a progress bar, a cancel
button, a summary.

If the flow needs no UI of its own, use a
[managed flow destination](managed-result-flows.md) instead; it is the same
idea with less code.

## Describe the flow

Each `open(key)` opens a screen whose key is a `NavigationKey.WithResult`,
and returns that screen's result. The block can branch on earlier results.
When it returns, `onCompleted` receives the value it returned.

<!-- snippet: flows/view-model -->
```kotlin
class CreateBookingViewModel : ViewModel() {
    private val navigation by navigationHandle<CreateBooking>()

    val flow by registerForFlowResult(
        flow = {
            val destination = open(ChooseDestination)
            val nights = open(ChooseNights(destination))
            // A branch: breakfast is only offered for stays of two nights or more.
            val breakfast = if (nights >= 2) open(ChooseBreakfast) else false
            Booking(destination, nights, breakfast)
        },
        onCompleted = { booking ->
            navigation.complete(booking)
        },
    )
}
```

The block is not a coroutine. Enro runs it again from the top each time a
step produces a result, replaying the results it already has, until it
reaches a step with no result yet. Keep the block free of side effects.

## Show the steps

`rememberNavigationContainerForFlow` makes a container whose backstack is the
flow's steps: one entry for each step answered so far, and the step being
asked now. Display it like any other container.

<!-- snippet: flows/host -->
```kotlin
@Composable
@NavigationDestination(CreateBooking::class)
fun CreateBookingDestination() {
    val navigation = navigationHandle<CreateBooking>()
    val viewModel = viewModel<CreateBookingViewModel> {
        createEnroViewModel { CreateBookingViewModel() }
    }
    // Displays the flow's steps. Its backstack holds one entry per answered
    // step, plus the step being asked now.
    val flowContainer = rememberNavigationContainerForFlow(viewModel.flow)

    Column(modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { flowContainer.backstack.size / 3f },
            modifier = Modifier.fillMaxWidth(),
        )
        NavigationDisplay(
            state = flowContainer,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = { navigation.close() },
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Cancel booking")
        }
    }
}
```

Closing a step returns to the previous one, where the user can change their
answer. The flow block then runs again with the new answer, so a branch that
depended on it is taken again.

Create the ViewModel with `createEnroViewModel { }`, as for any ViewModel
that uses a NavigationHandle.

## See also

- [Flow in a ViewModel recipe][flow-recipe] — the booking flow above.
- [Managed Result Flows](managed-result-flows.md) — a flow as a destination.
- [Sample Apps](../../samples.md) — the Loan Application sample is a longer
  flow with a summary step that edits earlier answers.

[flow-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/flow/FlowInViewModel.kt
