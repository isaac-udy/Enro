---
title: Interceptors
parent: Advanced Topics
nav_order: 8
---

# Interceptors

A `NavigationInterceptor` runs before a navigation operation is applied. It
can let the operation continue, cancel it, or run a different operation.
Use one for rules that apply to a key wherever it is opened from: an auth
gate, a feature flag, an audit log.

To observe navigation without changing it, a
[plugin](plugins.md) is simpler. To decide where one specific key leads, a
[synthetic destination](synthetic-destinations.md) keeps the logic with the
key.

## Writing an interceptor

`navigationInterceptor { }` builds an interceptor from blocks, one per key
type and event:

<!-- snippet: interceptors/redirect -->
```kotlin
val authInterceptor = navigationInterceptor {
    onOpened<Account> {
        if (!Session.isLoggedIn) {
            // Open Login instead, and tell it where the user was going.
            replaceWith(Login(redirectTo = key))
        }
        continueWithOpen()
    }
}
```

Every block must end by choosing an outcome. The outcome functions do not
return, so code after one does not run.

## Events and outcomes

| Block | Runs before | Outcomes |
|---|---|---|
| `onOpened<K> { }` | a `K` is opened | `continueWithOpen()`, `cancel()`, `cancelAnd { }`, `replaceWith(key)` |
| `onClosed<K> { }` | a `K` is closed without a result | `continueWithClose()`, `cancel()`, `cancelAnd { }`, `replaceWith(operation)` |
| `onCompleted<K> { }` | a `K` completes | `continueWithComplete()`, `deliverResultOnly()`, `cancel()`, `cancelAnd { }`, `replaceWith(operation)` |

- `cancel()` stops the operation. `cancelAnd { }` stops it and then runs the
  block.
- `replaceWith(...)` runs a different key or operation in place of this one.
- `deliverResultOnly()` sends the result to the caller and leaves the
  destination on the backstack.

Inside a block, `key` and `instance` are the destination the operation is
about, and `backstack` is the current backstack of the container it is
happening in. In `onCompleted` for a `NavigationKey.WithResult` key, `result`
is the value the destination completed with.

This interceptor uses all three events:

<!-- snippet: interceptors/events -->
```kotlin
val auditInterceptor = navigationInterceptor {
    onOpened<AuditedDocument> {
        if (key.name == "restricted") {
            // Stop the open. cancelAnd runs the block after cancelling.
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

For every container in the app, add the interceptor to the
`NavigationComponent`'s module:

<!-- snippet: interceptors/on-component -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        interceptor(authInterceptor)
    }
)
```

For one container, pass it to `rememberNavigationContainer`:

<!-- snippet: interceptors/on-container -->
```kotlin
val container = rememberNavigationContainer(
    backstack = backstackOf(Home.asInstance()),
    interceptor = authInterceptor,
)
NavigationDisplay(state = container)
```

## See also

- [Module Interceptor recipe][module-recipe] — the audit interceptor above,
  installed on the component.
- [Conditional Navigation recipe][conditional-recipe] — an auth gate on one
  container.
- [Auth Gate recipe][synthetic-recipe] — the same gate as a synthetic
  destination.

[module-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/interceptor/ModuleInterceptor.kt
[conditional-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/conditional/ConditionalNavigation.kt
[synthetic-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/synthetic/AuthGateSynthetic.kt
