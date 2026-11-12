# `enro-common`
`enro-common` contains the Enro classes, interfaces and functions that do not depend on a UI framework, such as `NavigationKey`. `enro-common` targets Android, iOS, JVM Desktop, WASM JS and Kotlin/JS, including NodeJS.

A Kotlin Multiplatform module with a non-UI target, such as a NodeJS backend, can depend on `enro-common` to declare `NavigationKey` types and to include `NavigationKey` objects in API responses.

## Example
A KMP project has these modules:

- `:common` (Android, iOS, JVM, WASM, JS)
- `:frontend` (Android, iOS, JVM, WASM)
- `:backend` (JS only, using NodeJS)

`:common` declares the API interfaces and the request and response classes, which are serialized with kotlinx.serialization. `:backend` implements the API interfaces. `:frontend` calls the API interfaces through a client. `:frontend` and `:backend` use the same Kotlin request and response classes.

`:common` depends on `enro-common` and declares `NavigationKey` types. `:backend` includes a `NavigationKey` in a response object, and `:frontend` opens the `NavigationKey`. `:backend` renders no UI, but `:backend` selects the screen that `:frontend` opens.

For example, in an A/B test of an onboarding flow, `:backend` returns the `NavigationKey` of the next onboarding screen.
