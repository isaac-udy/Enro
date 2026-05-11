---
title: Results
parent: Advanced Topics
nav_order: 1
has_children: true
---

# Results

Enro's result system is what turns a screen into a function with a return
value. A screen whose key implements `NavigationKey.WithResult<R>` returns a
typed `R` to its caller. The caller registers interest in the result with
`registerForNavigationResult`, opens the destination through the resulting
channel, and is notified asynchronously when the destination completes (or
when the user backs out).

## The shape of a result

A key signals "this screen returns a value" by implementing
`NavigationKey.WithResult<R>`:

```kotlin
@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

The destination returns its value with `navigation.complete(value)`:

```kotlin
@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateScreen() {
    val navigation = navigationHandle<SelectDate>()
    // ...
    Button(onClick = { navigation.complete(LocalDate.now()) }) { Text("Use today") }
}
```

A caller receives the value through a `NavigationResultChannel<R>`:

```kotlin
val getDate = registerForNavigationResult<LocalDate>(
    onCompleted = { date -> /* use it */ },
)

Button(onClick = { getDate.open(SelectDate()) }) { Text("Pick a date") }
```

That's the whole pattern. The rest of this page covers the variations.

## In a Composable

`registerForNavigationResult<R>` is a Composable function. It returns a
`NavigationResultChannel<R>` you call `.open(key)` on:

```kotlin
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val navigation = navigationHandle<Home>()

    val pickDate = registerForNavigationResult<LocalDate>(
        onClosed = { /* dismissed without producing a value */ },
        onCompleted = { date ->
            // do something with the date — note: this is a callback,
            // not a suspension. You can update Compose state from here.
        },
    )

    Button(onClick = { pickDate.open(SelectDate(maxDate = LocalDate.now())) }) {
        Text("Pick a date")
    }
}
```

The channel is `remember`-ed by composition key, so it survives recomposition
and process death.

### Closed vs completed

Two callbacks, two cases:

- `onCompleted(value)` fires when the destination calls
  `navigation.complete(value)`.
- `onClosed()` fires when the destination calls `navigation.close()` (or is
  closed by any other means — back press, container collapse, etc.).

You can register only `onCompleted` if you don't care about cancellation —
it's the required argument; `onClosed` defaults to a no-op.

### Without a result type

For destinations whose key does **not** implement `WithResult<R>`, there's a
`Unit`-typed overload that lets you still distinguish "completed" from
"closed":

```kotlin
@Serializable
data class ConfirmDelete(val itemName: String) : NavigationKey

val confirmDelete = registerForNavigationResult(
    onCompleted = { /* user confirmed */ },
    onClosed    = { /* user cancelled */ },
)

Button(onClick = { confirmDelete.open(ConfirmDelete("Tax return.pdf")) }) {
    Text("Delete")
}
```

Even though `ConfirmDelete` has no result type, the destination can still
call `complete()` to signal "user confirmed" vs `close()` for "user
cancelled". See [close vs complete](../core-concepts/navigation-handles.md#closing-vs-completing).

## In a ViewModel

`registerForNavigationResult` is also available as an extension on
`ViewModel`. The form is a property delegate:

```kotlin
class HomeViewModel : ViewModel() {
    val navigation by navigationHandle<Home>()

    val pickDate by registerForNavigationResult<LocalDate>(
        onCompleted = { date ->
            // update your StateFlow / call a use case / etc.
        },
    )

    fun onPickDateClicked() {
        pickDate.open(SelectDate(maxDate = LocalDate.now()))
    }
}
```

Inside the ViewModel, the channel survives configuration change and process
death along with the ViewModel. The `viewModelScope` is used to observe
incoming results, so callbacks stop when the ViewModel is cleared.

## Sub-pages

There are two patterns worth their own pages:

- **[Embedded result flows](results/embedded-result-flows.md)** — chaining a
  small number of results together inside one screen, using `onCompleted` to
  open the next result-producing key.
- **[Managed result flows](results/managed-result-flows.md)** — defining a
  multi-step flow as sequential, imperative code with
  `managedFlowDestination`.

## How results are routed

Under the hood, calling `channel.open(key)` attaches a metadata tag to the
opened instance identifying *which channel* this instance is feeding. When
the instance completes or closes, the runtime looks up the channel by tag
and dispatches the result. You can ignore this mechanism unless you're
building deep tooling — the point is that a single screen can have multiple
distinct result channels open without them being confused for each other.

A consequence worth noting: a `NavigationResultChannel` is bound to *one*
result type. If you want a screen that picks "either a contact or a phone
number," model that as a sealed class for the result type, not as two
channels racing each other.

## See also

- [Navigation Handles — closing vs completing](../core-concepts/navigation-handles.md#closing-vs-completing).
- [Returning Results recipe][results-recipe] — small worked example with
  both Composable and ViewModel forms.
- [Embedded result flows](results/embedded-result-flows.md) and
  [managed result flows](results/managed-result-flows.md).

[results-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/results/ReturningResults.kt
