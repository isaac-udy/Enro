---
title: Embedded Result Flows
parent: Results
grand_parent: Advanced Topics
nav_order: 1
---

# Embedded Result Flows

In an embedded result flow, one screen opens several result destinations in
sequence. The `onCompleted` callback of each `registerForNavigationResult`
channel opens the next destination through the next channel.

<!-- snippet: embedded-result-flows/flow -->
```kotlin
@Composable
@NavigationDestination(CreateAccount::class)
fun CreateAccountScreen() {
    val navigation = navigationHandle<CreateAccount>()
    var name by rememberSaveable { mutableStateOf<String?>(null) }
    var email by rememberSaveable { mutableStateOf<String?>(null) }

    val askForEmail = registerForNavigationResult<String>(
        onCompleted = { result ->
            email = result
            navigation.complete(CreateAccountResult(name!!, result))
        },
    )

    val askForName = registerForNavigationResult<String>(
        onCompleted = { result ->
            name = result
            askForEmail.open(EnterEmail)
        },
    )

    Button(onClick = { askForName.open(EnterName) }) {
        Text("Start")
    }
}
```

Use an embedded result flow for:

- Two or three steps.
- A next step that depends on the previous result through one small branch.
- A screen that has UI of its own and opens a few result destinations.

## Limits

Each added step adds a channel and a callback. Each branch adds a
condition inside a callback. The screen stores each intermediate result in
state that the screen declares.

Enro does not save the intermediate results of an embedded result flow.
The screen must store each intermediate result in state that is restored
after process death; see [Saved State](../saved-state.md).

For more than three or four steps, or for more than one branch, use a
[managed result flow](managed-result-flows.md). A managed result flow opens
the same destinations, declares the steps as sequential code and saves the
intermediate results.

## Combining embedded and managed flows

A step of a managed result flow can be a screen that contains an embedded
result flow. An embedded result flow can open a managed flow destination
through a channel.

## See also

- [Managed result flows](managed-result-flows.md) — longer sequences and
  sequences with branches.
- [Returning Results recipe][results-recipe] —
  `registerForNavigationResult` in a Composable and in a ViewModel.

[results-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/results/ReturningResults.kt
