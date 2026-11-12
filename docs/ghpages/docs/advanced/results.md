---
title: Results
parent: Advanced Topics
nav_order: 1
has_children: true
---

# Results

A destination whose key implements `NavigationKey.WithResult<R>` returns a
value of type `R` to the caller. The caller creates a
`NavigationResultChannel<R>` with `registerForNavigationResult` and opens
the destination through the channel. The channel calls a callback when the
destination completes or closes.

## Returning and receiving a result

A key declares a result type by implementing `NavigationKey.WithResult<R>`:

<!-- snippet: navigation-keys/select-date -->
```kotlin
@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
```

The destination returns the result with `navigation.complete(value)`:

<!-- snippet: results/complete -->
```kotlin
@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateScreen() {
    val navigation = navigationHandle<SelectDate>()
    // ...
    Button(onClick = { navigation.complete(today) }) { Text("Use today") }
}
```

A caller receives the result through a `NavigationResultChannel<R>`:

<!-- snippet: results/channel -->
```kotlin
val getDate = registerForNavigationResult<LocalDate>(
    onCompleted = { date -> /* use the date */ },
)

Button(onClick = { getDate.open(SelectDate()) }) { Text("Pick a date") }
```

## In a Composable

`registerForNavigationResult<R>` is a Composable function that returns a
`NavigationResultChannel<R>`. Call `open(key)` on the channel:

<!-- snippet: results/composable -->
```kotlin
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val navigation = navigationHandle<Home>()

    val pickDate = registerForNavigationResult<LocalDate>(
        onClosed = { /* SelectDate closed without a result */ },
        onCompleted = { date ->
            // onCompleted is a callback, not a suspending function.
            // Compose state can be updated here.
        },
    )

    Button(onClick = { pickDate.open(SelectDate(maxDate = today)) }) {
        Text("Pick a date")
    }
}
```

The channel's id is the destination's instance id plus the composite key
hash of the `registerForNavigationResult` call. A channel created by the
same call after recomposition or process death has the same id and receives
the result.

### `onCompleted` and `onClosed`

- `onCompleted(value)` is called when the opened destination calls
  `navigation.complete(value)`.
- `onClosed()` is called when the opened destination closes without
  completing, for example through `navigation.close()` or a back press.

`onCompleted` is required. `onClosed` defaults to a no-op.

### Without a result type

For a key that does not implement `WithResult<R>`,
`registerForNavigationResult` has an overload with no type argument. The
`onCompleted` callback of the overload takes no value:

<!-- snippet: results/no-result-key -->
```kotlin
@Serializable
data class ConfirmDelete(val itemName: String) : NavigationKey
```

<!-- snippet: results/no-result-channel -->
```kotlin
val confirmDelete = registerForNavigationResult(
    onCompleted = { /* user confirmed */ },
    onClosed    = { /* user cancelled */ },
)

Button(onClick = { confirmDelete.open(ConfirmDelete("Tax return.pdf")) }) {
    Text("Delete")
}
```

The `ConfirmDelete` destination calls `complete()` when the user confirms
and `close()` when the user cancels. See
[closing vs completing](../core-concepts/navigation-handles.md#closing-vs-completing).

## In a ViewModel

`registerForNavigationResult` is also an extension on `ViewModel`. The
`ViewModel` extension is used as a property delegate:

<!-- snippet: results/view-model -->
```kotlin
class HomeViewModel : ViewModel() {
    val navigation by navigationHandle<Home>()

    val pickDate by registerForNavigationResult<LocalDate>(
        onCompleted = { date ->
            // Update a StateFlow or call a use case here.
        },
    )

    fun onPickDateClicked() {
        pickDate.open(SelectDate(maxDate = today))
    }
}
```

The channel's id is the destination's instance id plus the ViewModel's
class name and the property name. A ViewModel that is recreated after
process death creates a channel with the same id. The channel collects
results in `viewModelScope`, so the callbacks stop when the ViewModel is
cleared.

## Result flows

- **[Embedded result flows](results/embedded-result-flows.md)** — one
  screen opens a short sequence of result destinations, each from the
  `onCompleted` callback of the previous channel.
- **[Managed result flows](results/managed-result-flows.md)** — a
  multi-step flow written as sequential code with `managedFlowDestination`.
- **[Flows in a ViewModel](results/navigation-flows.md)** — a multi-step
  flow written as sequential code with `registerForFlowResult`, displayed
  inside a screen's own UI.

## How results are routed

`channel.open(key)` stores the channel's id in the metadata of the opened
instance. When the opened instance completes or closes, Enro delivers the
result to the channel with that id. A destination can register several
channels. Each channel receives only the results of the instances that the
channel opened.

A `NavigationResultChannel` has one result type. For a result that is one
of several types, such as a contact or a phone number, declare a sealed
class as the result type.

## Forwarding a result from a synthetic destination

Several keys can declare the same result type, for example a legacy screen
and a redesigned screen. A synthetic destination can choose one of the keys
at runtime with `completeFrom`. Callers open only the synthetic
destination's key:

<!-- snippet: results/decider -->
```kotlin
@Serializable object EditProfile : NavigationKey.WithResult<ProfileUpdate>
@Serializable object EditProfileLegacy : NavigationKey.WithResult<ProfileUpdate>
@Serializable object EditProfileV2 : NavigationKey.WithResult<ProfileUpdate>

@NavigationDestination(EditProfile::class)
val editProfile = syntheticDestination<EditProfile> {
    if (featureFlags.useNewProfileUi) completeFrom(EditProfileV2)
    else completeFrom(EditProfileLegacy)
}
```

`completeFrom` opens the chosen key and copies the result channel id from
the synthetic destination's instance to the opened instance. The result of
the chosen screen's `complete(value)` is delivered to the channel that
opened `EditProfile`. See [Synthetic Destinations](synthetic-destinations.md).

## See also

- [Synthetic Destinations](synthetic-destinations.md) — `completeFrom`.
- [Navigation Handles — closing vs completing](../core-concepts/navigation-handles.md#closing-vs-completing).
- [Returning Results recipe][results-recipe] — `registerForNavigationResult`
  in a Composable and in a ViewModel.

[results-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/results/ReturningResults.kt
