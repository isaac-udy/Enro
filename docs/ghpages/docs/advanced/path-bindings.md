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

## Simple usage

Annotate the key with `@NavigationPath("/pattern/with/{placeholders}")`:

<!-- snippet: path-bindings/simple -->
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

<!-- snippet: path-bindings/query -->
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

<!-- snippet: path-bindings/value-class -->
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

<!-- snippet: path-bindings/from-binding -->
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

<!-- snippet: path-bindings/programmatic -->
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

<!-- snippet: skip (API outline) -->
```kotlin
// String → Key
fun EnroController.getNavigationKeyFromPath(path: String): NavigationKey?

// Key → String
fun EnroController.getPathFromNavigationKey(key: NavigationKey): String?
```

`NavigationHandle` and `NavigationContext` have receiver versions of
both for convenience inside destinations — see the
`NavigationHandle.getNavigationKeyFromPath` / `getPathFromNavigationKey`
KDoc. Both return `null` when no binding matches: an unrecognised path
or a key whose type has no binding registered.

Typical use — handling an inbound Android deep link. The Activity keeps the
URL in state, and the composition resolves it and opens the key on the root
container:

<!-- snippet: path-bindings/android-deep-link -->
```kotlin
class MainActivity : ComponentActivity() {
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLink = intent.dataString
        setContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            val context = LocalNavigationContext.current
            LaunchedEffect(pendingDeepLink) {
                val path = pendingDeepLink ?: return@LaunchedEffect
                pendingDeepLink = null
                val key = context.getNavigationKeyFromPath(path) ?: return@LaunchedEffect
                container.updateBackstack { (it + key.asInstance()).asBackstack() }
            }
            NavigationDisplay(state = container)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLink = intent.dataString
    }
}
```

## Which destinations appear in the URL

On the web, the URL bar shows the path of the **deepest active destination
that has a path binding**. Enro walks from the root container's top
destination down through each destination's active container, so a
destination inside a tab or a detail pane can set the URL.

A destination with no path binding never changes the URL. The URL keeps
the last path an annotated destination set. Dialogs, sheets and other
destinations you don't annotate stay out of the URL this way.

A full-page load resolves the URL to one destination on the root container.
To cold-load a destination that lives inside a nested container, parse the
URL yourself and seed the parent backstack, as the *Advanced Deep Link*
recipe does.

To record only the root container in browser history, pass
`nestedContainerHistory = false` to `InstallWebHistoryPlugin`.

See [Web](../platform/web.md) for `EnroBrowserContent`,
`InstallWebHistoryPlugin`, and `rememberInitialBackstackFromUrl`.

## See also

- [Web](../platform/web.md) — how `@NavigationPath`
  drives browser URL routing.
- [Synthetic Destinations](synthetic-destinations.md) — pairs well
  with path bindings for "URL hits → decide which screen to show".
- Recipes: [Basic Deep Link][basic-deep-link],
  [Advanced Deep Link][advanced-deep-link] — full working examples of
  annotated and programmatic bindings respectively.

[basic-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/BasicDeepLink.kt
[advanced-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/AdvancedDeepLink.kt
