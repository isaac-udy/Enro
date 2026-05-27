---
title: Path Bindings
parent: Advanced Topics
nav_order: 7
---

# Path Bindings

A path binding maps a URL path pattern to a `NavigationKey` type, and
back again. Once a key has a binding, the runtime can:

- **Resolve a path to a key** — turn `/products/abc?source=email` into
  `ProductDetail(productId = "abc", source = "email")` for deep-link
  handling.
- **Serialise a key to a path** — turn that same key back into the URL
  the web history plugin shows in the address bar.

The same `@NavigationPath` annotation works on every platform. On Web
it drives the URL bar; on Android / iOS / Desktop it backs deep-link
parsing without you having to wire up Intent filters or custom URL
schemes yourself. (Inbound deep links still arrive through the
platform's own mechanism — `intent.data`, `application(_:openURL:)`,
custom URI handlers — but once you have the string, `getNavigationKeyFromPath`
turns it into a key you can `open()`.)

`@NavigationPath` and its companions are currently marked
`@ExperimentalEnroApi` while the API surface stabilises through the
3.x cycle. The shape is unlikely to shift meaningfully, but expect
small refinements before the API graduates.

## Simple usage

Annotate the key with `@NavigationPath("/pattern/with/{placeholders}")`:

```kotlin
@Serializable
@NavigationPath("/products/{productId}")
data class ProductDetail(
    val productId: String,
) : NavigationKey
```

The processor reads the pattern, matches each `{placeholder}` to a
property on the key (by name), and generates the binding. At runtime
the controller will accept `/products/shoe-1` and produce
`ProductDetail("shoe-1")`; calling `controller.getPathFromNavigationKey(ProductDetail("shoe-1"))`
returns `/products/shoe-1`.

Properties that aren't part of the path produce a compile-time error
unless they have default values — there has to be something for the
deserialiser to plug into them.

## Query parameters and optionals

Append `?key={value}` (or `&key={value}`) for query parameters. Mark
optional parameters with a trailing `?` inside the braces:

```kotlin
@Serializable
@NavigationPath("/products/{productId}?source={source?}&campaign={campaign?}")
data class ProductDetail(
    val productId: String,
    val source: String? = null,
    val campaign: String? = null,
) : NavigationKey
```

Now `/products/shoe-1`, `/products/shoe-1?source=email`, and
`/products/shoe-1?source=email&campaign=spring-sale` all resolve;
absent params land as `null` on the key.

## Value classes

Inline value classes serialise through their backing field, so they
work as path properties as long as the underlying type is supported
(`String`, primitive numerics, `Boolean`):

```kotlin
@JvmInline
@Serializable
value class ProductId(val value: String)

@Serializable
@NavigationPath("/products/{productId}")
data class ProductDetail(
    val productId: ProductId,
) : NavigationKey
```

The URL is still `/products/shoe-1` — the value class is transparent
at the path layer.

## Custom bindings with `@NavigationPath.FromBinding`

Property-by-name matching covers most cases. When it doesn't — you
want to default missing fields to non-trivial values, derive one
property from another, or hand-write the serialiser — declare a
`NavigationKey.PathBinding<T>` and point at it with
`@NavigationPath.FromBinding`:

```kotlin
@Serializable
@NavigationPath("/items/{id}?name={name}&title={title?}")
@NavigationPath.FromBinding(MyKey.Default::class)
data class MyKey(
    val id: String,
    val name: String,
    val title: String? = null,
) : NavigationKey {

    object Default : NavigationKey.PathBinding<MyKey> {
        // Fallback shape — used when the URL is just /items with a title.
        override val pattern: String = "/items?title={title?}"

        override fun deserialize(data: PathData): MyKey {
            return MyKey(
                id = "default-id",
                name = "default-name",
                title = data.optional("title"),
            )
        }

        override fun serialize(builder: PathData.Builder, key: MyKey) {
            key.title?.let { builder.set("title", it) }
        }
    }
}
```

A key can carry **one primary pattern** (the `@NavigationPath` value)
plus **any number of `FromBinding` patterns**. Resolution tries each
pattern in turn; the first match wins. This is what lets you support
short / long forms of the same URL ("`/items/abc`" and "`/items`" both
producing some `MyKey`).

The `PathData` / `PathData.Builder` API exposes typed accessors for
the parsed path segments and query parameters. See `dev.enro.path.PathData`
for the surface.

## Programmatic bindings (no annotation)

When you can't (or don't want to) annotate the key — typically because
the key is defined in a module you don't control — register the binding
directly through `NavigationPathBinding.createPathBinding(…)`:

```kotlin
val productDetailBinding: NavigationPathBinding<ProductDetail> =
    NavigationPathBinding.createPathBinding(
        pattern = "/products/{productId}?source={source?}",
        propertyOne = ProductDetail::productId,
        propertyTwo = ProductDetail::source,
        constructor = ::ProductDetail,
    )

val pathsModule = createNavigationModule {
    path(productDetailBinding)
}
```

The annotation form is just sugar over this API — the processor
generates equivalent `createPathBinding(…)` calls and registers them
on the component.

## Resolving paths at runtime

Two functions, mirror images of each other:

```kotlin
// String → Key
@ExperimentalEnroApi
fun EnroController.getNavigationKeyFromPath(path: String): NavigationKey?

// Key → String
@ExperimentalEnroApi
fun EnroController.getPathFromNavigationKey(key: NavigationKey): String?
```

`NavigationHandle` and `NavigationContext` have receiver versions of
both for convenience inside destinations — see the
`NavigationHandle.getNavigationKeyFromPath` / `getPathFromNavigationKey`
KDoc. Both return `null` when no binding matches: an unrecognised path
or a key whose type has no binding registered.

Typical use:

```kotlin
// Handling an inbound Android deep link.
override fun onNewIntent(intent: Intent) {
    val key = controller.getNavigationKeyFromPath(intent.dataString.orEmpty()) ?: return
    rootHandle.open(key)
}
```

## The path-vs-state model

A common question: "Why doesn't my modal / tab / inner-container
destination show up in the URL?"

Enro's web URL routing is **root-container-only** — only the active
destination of the root navigation container is reflected in the URL
bar. Modals, sheets, inner tabs, list/detail panes hosted inside
other destinations are *state*, not *URL state*, and don't write to
`location.pathname`.

This is deliberate, and matches how most modern web apps behave:

- Going to a different profile on Bluesky writes the URL
  (`bsky.app/profile/some.handle`).
- Switching tabs within that profile (Posts / Replies / Media /
  Videos / Likes / Feeds) doesn't — the URL stays on the profile.

Path bindings declare what's URL-shaped; the runtime treats unbound
destinations as session-local. If you have a destination that you'd
like to be deep-linkable but it lives inside a nested container, the
two options are:

1. Promote it to root for the URL-bound case (and host the same
   destination inside a parent for the nested case — same key, two
   contexts).
2. Use the synthetic-backstack pattern from the *Advanced Deep Link*
   recipe: parse the URL yourself, derive the parent context, seed the
   nested container's backstack manually.

The web platform docs cover the URL/history wiring in detail — see
[Web Platform Guide](../platform/web.md) for `EnroBrowserContent`,
`InstallWebHistoryPlugin`, and `rememberInitialBackstackFromUrl`.

## See also

- [Web Platform Guide](../platform/web.md) — how `@NavigationPath`
  drives browser URL routing.
- [Synthetic Destinations](synthetic-destinations.md) — pairs well
  with path bindings for "URL hits → decide which screen to show".
- Recipes: [Basic Deep Link][basic-deep-link],
  [Advanced Deep Link][advanced-deep-link] — full working examples of
  annotated and programmatic bindings respectively.

[basic-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/deeplink/BasicDeepLink.kt
[advanced-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/deeplink/AdvancedDeepLink.kt
