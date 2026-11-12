---
title: Path Bindings
parent: Advanced Topics
nav_order: 7
---

# Path Bindings

A path binding maps a URL path pattern to a `NavigationKey` type. With a
path binding, Enro can:

- **Resolve a path to a key** — convert `/products/abc?source=email` into
  `ProductDetail(productId = "abc", source = "email")`.
- **Serialise a key to a path** — convert the same key into
  `/products/abc?source=email`.

`@NavigationPath` is available on every platform. On web, the web history
plugin writes the path of the active destination to the address bar. On
Android, iOS and desktop, the app receives a deep link through the platform
API (`intent.data`, `application(_:openURL:)`, a custom URI handler) and
calls `getNavigationKeyFromPath` to convert the string into a key.

## Annotating a key

Annotate the key with `@NavigationPath("/pattern/with/{placeholders}")`:

<!-- snippet: path-bindings/simple -->
```kotlin
@Serializable
@NavigationPath("/products/{productId}")
data class ProductDetail(
    val productId: String,
) : NavigationKey
```

The KSP processor matches each `{placeholder}` to the key property with the
same name and generates the binding.
`controller.getNavigationKeyFromPath("/products/shoe-1")` returns
`ProductDetail("shoe-1")`, and
`controller.getPathFromNavigationKey(ProductDetail("shoe-1"))` returns
`/products/shoe-1`.

A key property that is not in the pattern must have a default value.
Without a default value, the build fails.

## Query parameters and optionals

Add query parameters with `?key={value}` and `&key={value}`. A `?` before
the closing brace marks a parameter as optional. Declare the property for
an optional parameter as nullable:

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

`/products/shoe-1`, `/products/shoe-1?source=email` and
`/products/shoe-1?source=email&campaign=spring-sale` each resolve to a
`ProductDetail`. A property is `null` when the path omits the optional
parameter.

## Value classes

A path property can be a `String`, `Int`, `Long`, `Float`, `Double`,
`Short`, `Byte`, `Char` or `Boolean`, or a value class that wraps one of
those types:

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

The path contains the wrapped value: `/products/shoe-1`.

## Custom bindings with `@NavigationPath.FromBinding`

A `NavigationKey.PathBinding<T>` object converts between a path and a key
in code. Use a `PathBinding` to set properties that are missing from the
path, to derive one property from another, or to write the serialisation
by hand. Reference the object with `@NavigationPath.FromBinding`:

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
        // Matches /items and /items?title=…, which the @NavigationPath pattern does not match.
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

A key can have a `@NavigationPath` pattern and a
`@NavigationPath.FromBinding` binding. In the example, `/items/abc?name=x`
and `/items` both resolve to a `MyKey`. The class in
`@NavigationPath.FromBinding` must be an `object`.

When more than one binding matches a path, Enro uses the binding with the
most literal path segments, then the binding with the most required query
parameters. Enro throws an `IllegalArgumentException` when two matching
bindings are equally specific.

`PathData.require(name)` returns a path segment or query parameter as a
`String` and throws when the value is missing. `PathData.optional(name)`
returns the `String` or `null`. `PathData.Builder.set(name, value)` writes
a `String` value.

## Bindings without an annotation

`NavigationPathBinding.createPathBinding(…)` creates a binding for a key
that has no annotation, such as a key declared in another module. Register
the binding with `path(...)` in a navigation module:

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

For an annotated key, the KSP processor generates the same `path(...)`
registration.

## Resolving paths at runtime

<!-- snippet: skip (API outline) -->
```kotlin
fun EnroController.getNavigationKeyFromPath(path: String): NavigationKey?

fun EnroController.getPathFromNavigationKey(key: NavigationKey): String?
```

`getNavigationKeyFromPath` returns `null` when no binding matches the path.
`getPathFromNavigationKey` returns `null` when no binding is registered for
the key's type. `NavigationHandle` and `NavigationContext` have extension
functions with the same names.

The example handles an Android deep link. The Activity stores the URL in
state. A `LaunchedEffect` resolves the URL to a key and adds the key to the
root container's backstack:

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

On the web, the address bar shows the path of the **deepest active
destination that has a path binding**. The web history plugin starts at the
top destination of the root container and follows each destination's active
container. A destination inside a tab or a detail pane can set the URL.

A destination with no path binding does not change the URL. The address bar
keeps the path of the last destination that had a path binding. A dialog or
sheet with no path binding does not appear in the URL.

On a full page load, `rememberInitialBackstackFromUrl` resolves the URL to
one destination on the root container. To load a destination inside a
nested container, parse the URL in the app and set the parent container's
initial backstack, as the [Advanced Deep Link][advanced-deep-link] recipe
does.

To record only the root container in browser history, pass
`nestedContainerHistory = false` to `InstallWebHistoryPlugin`.

See [Web](../platform/web.md) for `EnroBrowserContent`,
`InstallWebHistoryPlugin` and `rememberInitialBackstackFromUrl`.

## See also

- [Web](../platform/web.md) — browser URL routing with `@NavigationPath`.
- [Synthetic Destinations](synthetic-destinations.md) — a synthetic
  destination can choose the screen to open for a resolved path.
- Recipes: [Basic Deep Link][basic-deep-link] uses annotated bindings.
  [Advanced Deep Link][advanced-deep-link] uses bindings created in code.

[basic-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/BasicDeepLink.kt
[advanced-deep-link]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/AdvancedDeepLink.kt
