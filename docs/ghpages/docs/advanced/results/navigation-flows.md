---
title: Flows in a ViewModel
parent: Results
grand_parent: Advanced Topics
nav_order: 3
---

# Flows in a ViewModel

`registerForFlowResult` declares a multi-step flow as sequential code in a
ViewModel. The screen that owns the ViewModel displays the steps in a
container, and can display UI around the container: a progress bar, a
cancel button, a summary.

For a flow with no UI around the steps, use a
[managed flow destination](managed-result-flows.md). A managed flow
destination runs the same `flow` block and requires less code.

## Describe the flow

Each `open(key)` call opens a screen whose key is a
`NavigationKey.WithResult` and returns that screen's result. The `flow`
block can branch on earlier results. When the `flow` block returns,
`onCompleted` receives the returned value. A ViewModel can declare one
`registerForFlowResult`.

<!-- snippet: flows/view-model -->
```kotlin
class CreateBookingViewModel : ViewModel() {
    private val navigation by navigationHandle<CreateBooking>()

    val flow by registerForFlowResult(
        flow = {
            val destination = open(ChooseDestination)
            val nights = open(ChooseNights(destination))
            // A branch: the flow opens ChooseBreakfast only for two nights or more.
            val breakfast = if (nights >= 2) open(ChooseBreakfast) else false
            Booking(destination, nights, breakfast)
        },
        onCompleted = { booking ->
            navigation.complete(booking)
        },
    )
}
```

The `flow` block is not a coroutine. Enro runs the `flow` block again from
the top after each step's result. `open` returns the saved result of each
step that has a result, and stops the block at the first step that has no
result. The `flow` block must not have side effects.

## Show the steps

`rememberNavigationContainerForFlow` creates a container whose backstack
contains the flow's steps: one entry for each step that has a result, and
the current step. Pass the container to `NavigationDisplay`.

<!-- snippet: flows/host -->
```kotlin
@Composable
@NavigationDestination(CreateBooking::class)
fun CreateBookingDestination() {
    val navigation = navigationHandle<CreateBooking>()
    val viewModel = viewModel<CreateBookingViewModel> {
        createEnroViewModel { CreateBookingViewModel() }
    }
    // flowContainer displays the steps of the flow. The backstack of flowContainer
    // contains one entry for each step that has a result, plus the current step.
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

Closing a step displays the previous step, where the user can enter a new
result. The `flow` block then runs again with the new result, and each
branch that reads the new result is evaluated again.

Construct the ViewModel inside `createEnroViewModel { }`, as for any
ViewModel that uses a `NavigationHandle`.

## See also

- [Flow in a ViewModel recipe][flow-recipe] — the booking flow on this page.
- [Managed Result Flows](managed-result-flows.md) — a flow declared as a
  destination.
- [Sample Apps](../../samples.md) — the Loan Application sample is a longer
  flow with a summary step that edits the results of earlier steps.

[flow-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/flow/FlowInViewModel.kt
