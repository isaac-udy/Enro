---
title: Embedded Result Flows
parent: Results
grand_parent: Advanced Topics
nav_order: 1
---

# Embedded Result Flows

An *embedded result flow* is when a screen chains several result-producing
destinations together, using each result to decide what to ask for next, all
inside that screen's own logic. It's the simplest way to compose a short
sequence of result-producing steps.

The pattern is just `registerForNavigationResult` callbacks calling
`open` on each other.

```kotlin
@Composable
@NavigationDestination(CreateAccount::class)
fun CreateAccountScreen() {
    val navigation = navigationHandle<CreateAccount>()
    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }

    val askForEmail = registerForNavigationResult<String>(
        onCompleted = { result ->
            email = result
            navigation.complete(/* CreateAccountResult(name!!, email!!) */)
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

This is the right shape for:

- Two or three steps where the screen logic is simple.
- Cases where the next step depends on the previous result, but the branching is small.
- A screen that already has its own UI and is only "augmenting" itself with a few sub-questions.

## When this pattern hurts

The callback chain grows quickly. Once you have more than three or four
steps, or once the branching is non-trivial ("if the user picked X, ask Y;
otherwise ask Z and then W"), the chain becomes hard to read and harder to
test. The state at any point is spread across `remember`-ed variables.

The pattern also doesn't survive *partial* progress well. If the user
completes step two of four and then the process is killed, you've lost steps
one and two unless you stash them somewhere persistent yourself.

For anything beyond a small handful of steps, prefer a
[managed result flow](managed-result-flows.md) — same destinations, but the
flow control is written as straight-line code and the intermediate state is
managed for you.

## Mixing the patterns

You can mix the two. A managed flow can launch an embedded sub-flow inside
a step, and an embedded flow can hand off to a managed flow at any point.
The right question is: *for the next stretch, is the logic
straight-line-able?* If yes, use a managed flow; if no, use callbacks.

## See also

- [Managed result flows](managed-result-flows.md) — for longer or more
  conditional sequences.
- [Returning Results recipe][results-recipe] — the canonical small example.

[results-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/results/ReturningResults.kt
