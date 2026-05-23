---
title: Web Platform Guide
parent: Platform-Specific Guides
nav_order: 4
---

# Web

Enro runs in the browser through Compose for Web's WasmJS target. The
install pattern mirrors the other platforms: call
`installNavigationController` once at start-up, then host a container
inside an `EnroBrowserContent`.

## Minimal install

```kotlin
fun main() {
    MyComponent.installNavigationController(document)

    ComposeViewport {
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = rememberInitialBackstackFromUrl {
                    backstackOf(Home.asInstance())
                },
            )
            InstallWebHistoryPlugin(container)
            NavigationDisplay(state = container)
        }
    }
}
```

The pieces:

- `installNavigationController(document)` ties the controller to the
  browser's `document`. This is currently the only supported binding —
  Enro runs in the browser via Compose for Web.
- `ComposeViewport` is the standard Compose Multiplatform entry point for
  the browser; it mounts your Compose tree at the page's root.
- `EnroBrowserContent { }` provides the Compose locals Enro needs for
  browser-specific behaviour. Treat it like Compose Multiplatform's
  outermost theme wrapper.
- `rememberInitialBackstackFromUrl { ... }` reads `window.location` once
  on first composition and tries to resolve it to a backstack via the
  controller's registered path bindings. If the URL matches a
  `@NavigationPath`, the app boots straight into that destination; if it
  doesn't, the lambda's default is used.
- `InstallWebHistoryPlugin(container)` wires your container's backstack
  into the browser history API. The URL bar reflects the current
  destination, and the browser's back/forward buttons navigate the
  container.

## URL routing with `@NavigationPath`

A `NavigationKey` annotated with `@NavigationPath` opts into having its
URL form derived automatically. Whenever that key becomes the active
destination, the plugin writes the corresponding URL into the address
bar, and bookmarks / shared links resolve back to that key on cold load:

```kotlin
@Serializable
@NavigationPath("/products/{productId}?source={source?}")
data class ProductDetail(
    val productId: String,
    val source: String? = null,
) : NavigationKey
```

With this key registered, navigating to `ProductDetail("abc-123")`
updates the URL to `/products/abc-123`, and pasting
`/products/abc-123?source=email` into a new tab boots directly into the
`ProductDetail("abc-123", "email")` screen.

See the [path-binding recipes][deeplink-recipes] for the full set of
patterns, including value-class parameters and custom
`NavigationKey.PathBinding` implementations.

## What the URL bar shows

The plugin uses two slots in `window.history` and treats them as
independent concerns:

- **URL** (`location.pathname + location.search`) — derived from the
  active leaf destination's `@NavigationPath`. This is the part users
  see and share.
- **`history.state`** — the full container tree as JSON, used for
  accurate back/forward restoration mid-session (modals, sibling
  containers, results in flight, etc.).

Destinations without a `@NavigationPath` still work — they just produce
a positional `#N` hash instead of a semantic URL. Adoption is
incremental: you can annotate the destinations you want bookmarkable and
leave the rest alone.

## Browser history

`InstallWebHistoryPlugin` is what makes the back button do what users
expect. Without it, the navigation container still works but the URL bar
won't move and the browser's back button will leave the page.

Install it as close as possible to where you create the container — same
composable, ideally — so the plugin and the container share a lifetime.

## What Enro provides on Web

- A real backstack that mirrors browser history.
- The full common API: `NavigationKey`, `NavigationKey.WithResult<T>`,
  `navigationHandle<T>()`, `registerForNavigationResult`,
  `NavigationDisplay`, scene strategies, plugins, decorators.
- Deep linking from a URL on first load via
  `rememberInitialBackstackFromUrl` + `@NavigationPath` bindings.
- Saved state across in-page navigation. Full-page reloads start fresh —
  if you need persistence across reload, write to `localStorage` /
  `sessionStorage` yourself.

## Notes

- The WasmJS target requires the Compose for Web toolchain (Kotlin/Wasm).
  See [the Compose Multiplatform docs][cmp-web] for the project setup.
- `rememberInitialBackstackFromUrl` only fires once at composition. To
  react to mid-session URL changes (e.g. the user editing the address
  bar manually), the plugin falls back to parsing
  `window.location` on `popstate` and applying the resolved key.

## See also

- [Installation](../getting-started/installation.md) for the multi-platform setup.
- [Recipes web main][web-main] — full working bootstrap for the recipes app.

[cmp-web]: https://github.com/JetBrains/compose-multiplatform/blob/master/web/README.md
[web-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/wasmJsMain/kotlin/main.kt
[deeplink-recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/src/commonMain/kotlin/dev/enro/recipes/deeplink
