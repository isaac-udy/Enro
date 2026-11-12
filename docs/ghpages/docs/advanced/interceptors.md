---
title: Interceptors
parent: Advanced Topics
nav_order: 8
---

# Interceptors

A `NavigationInterceptor` runs before a container applies a navigation
operation. A `NavigationInterceptor` can continue the operation, cancel the
operation, or replace the operation with a different operation. Use a
`NavigationInterceptor` for a rule that applies to a key from every caller:
an auth gate, a feature flag, an audit log.

To observe navigation without changing an operation, use a
[plugin](plugins.md). To choose the destination that one key opens, use a
[synthetic destination](synthetic-destinations.md).

## Writing an interceptor

`navigationInterceptor { }` builds an interceptor from blocks. Each block
applies to one key type and one event:

<!-- snippet: interceptors/redirect -->
```kotlin
val authInterceptor = navigationInterceptor {
    onOpened<Account> {
        if (!Session.isLoggedIn) {
            // replaceWith does not return, so continueWithOpen() runs only when logged in.
            replaceWith(Login(redirectTo = key))
        }
        continueWithOpen()
    }
}
```

A block chooses an outcome by calling an outcome function. An outcome
function does not return, so code after the call does not run. An
`onOpened` or `onCompleted` block that returns without calling an outcome
function continues the operation. An `onClosed<K> { }` block must end with
a call to an outcome function.

## Events and outcomes

| Block | Runs before | Outcomes |
|---|---|---|
| `onOpened<K> { }` | a `K` is opened | `continueWithOpen()`, `cancel()`, `cancelAnd { }`, `replaceWith(key)` |
| `onClosed<K> { }` | a `K` is closed without a result | `continueWithClose()`, `cancel()`, `cancelAnd { }`, `replaceWith(operation)` |
| `onCompleted<K> { }` | a `K` completes | `continueWithComplete()`, `deliverResultOnly()`, `cancel()`, `cancelAnd { }`, `replaceWith(operation)` |

- `cancel()` stops the operation. `cancelAnd { }` stops the operation and
  then runs the block.
- `replaceWith(...)` executes a different key or operation in place of the
  intercepted operation.
- `deliverResultOnly()` delivers the result to the caller and leaves the
  destination on the backstack.

Inside a block, `key` and `instance` identify the destination that the
operation opens, closes or completes. `backstack` is the current backstack
of the container that executes the operation. In an `onCompleted` block for
a `NavigationKey.WithResult` key, `result` is the result value.

The `auditInterceptor` example uses all three events:

<!-- snippet: interceptors/events -->
```kotlin
val auditInterceptor = navigationInterceptor {
    onOpened<AuditedDocument> {
        if (key.name == "restricted") {
            // cancelAnd cancels the open, then runs the block.
            cancelAnd { auditLog.add("Blocked: open ${key.name}") }
        }
        auditLog.add("Opened: ${key.name}")
        continueWithOpen()
    }

    onClosed<AuditedDocument> {
        auditLog.add("Closed: ${key.name}")
        continueWithClose()
    }

    onCompleted<AuditedSignature> {
        // `result` is the value the destination completed with.
        auditLog.add("Signed by: $result")
        continueWithComplete()
    }
}
```

## Where to install an interceptor

To apply an interceptor to every container in the app, add the interceptor
to the `NavigationComponent`'s module:

<!-- snippet: interceptors/on-component -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        interceptor(authInterceptor)
    }
)
```

To apply an interceptor to one container, pass the interceptor to
`rememberNavigationContainer`:

<!-- snippet: interceptors/on-container -->
```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
    interceptor = authInterceptor,
)
NavigationDisplay(state = container)
```

## See also

- [Module Interceptor recipe][module-recipe] — the `auditInterceptor`
  example, installed on the component.
- [Conditional Navigation recipe][conditional-recipe] — an auth gate
  interceptor on one container.
- [Auth Gate recipe][synthetic-recipe] — an auth gate written as a
  synthetic destination.

[module-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/interceptor/ModuleInterceptor.kt
[conditional-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/conditional/ConditionalNavigation.kt
[synthetic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/synthetic/AuthGateSynthetic.kt
