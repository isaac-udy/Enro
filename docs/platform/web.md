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
                backstack = backstackOf(Home.asInstance()),
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
- `InstallWebHistoryPlugin(container)` wires your container's backstack
  into the browser history API. The URL bar reflects the current
  destination, and the browser's back/forward buttons navigate the
  container.

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
- Saved state across in-page navigation. Full-page reloads start fresh —
  if you need persistence across reload, write to `localStorage` /
  `sessionStorage` yourself.

## Notes

- The WasmJS target requires the Compose for Web toolchain (Kotlin/Wasm).
  See [the Compose Multiplatform docs][cmp-web] for the project setup.
- Deep linking from a URL on first load is on the roadmap; for now, you
  control the initial backstack with the same `backstackOf(...)` call
  you'd use on any other platform.

## See also

- [Installation](../getting-started/installation.md) for the multi-platform setup.
- [Recipes web main][web-main] — full working bootstrap for the recipes app.

[cmp-web]: https://github.com/JetBrains/compose-multiplatform/blob/master/web/README.md
[web-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/wasmJsMain/kotlin/main.kt
