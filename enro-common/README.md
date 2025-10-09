# `enro-common`
The `enro-common` module exists as a place to put common Enro classes/interfaces/functions that do not depend on platform specific UI functionality. `enro-runtime` targets Android, iOS, JVM Desktop, and WASM JS, but `enro-common` also targets "normal" JS (non-WASM). At the surface level, it might seem a little odd to also target "normal" JS from this module, when `enro-runtime` does not support this target, but it's not uncommon to use NodeJS as a backend for Kotlin Multiplatform applications. By providing some of the non-UI related Enro definitions (such as `NavigationKey`) in `enro-common`, we allow KMP applications where a NodeJS backend is able to provide API responses that contain these objects.

## Example
Imagine that you are working on a KMP project with the following modules: 
`:common` (Android, iOS, JVM, WASM, JS)
`:frontend` (Android, iOS, JVM, WASM)
`:backend` (JS only, using NodeJS)

The `:common` module is able to define API interfaces and their request/response classes (which are serialized using kotlinx serialization). The `:backend` module is able to implement these APIs, and the `:frontend` module is able to request a client for these APIs. This is a good developer experience, because you're dealing with the exact same kotlin classes on the frontend and backend, and can share almost anything related to the APIs/requests/responses. 

By providing the `enro-common` module, we allow the `:common` module to define `NavigationKey`s, which means that the `:backend` could include a `NavigationKey` in a response object. Even though the NodeJS `:backend` module could never render UI that uses Enro for navigation, this would allow the `:backend` module to control navigation on the clients.

Allowing the backend to control the navigation of frontend clients in some situations can be very useful. For example, when A/B testing an onboarding flow, the backend may want to tell the frontend which screen to show next within that onboarding flow.