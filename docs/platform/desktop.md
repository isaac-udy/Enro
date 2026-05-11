---
title: Desktop
parent: Platform-Specific Guides
nav_order: 3
---

# Desktop

Enro runs on Compose Multiplatform for Desktop. Installation is a single
call from `main`, after which you open one or more windows through the
controller.

## Minimal install

```kotlin
fun main() {
    val controller = MyComponent.installNavigationController(Unit)
    controller.openWindow(
        GenericRootWindow(
            windowConfiguration = {
                RootWindow.WindowConfiguration(
                    title = "My App",
                    onCloseRequest = { navigation.close() },
                )
            },
        ) {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            NavigationDisplay(state = container)
        },
    )

    application {
        EnroApplicationContent(controller)
    }
}
```

What's going on here:

- `installNavigationController(Unit)` installs the controller for the
  current process and hands you an `EnroController`.
- `controller.openWindow(...)` registers a window with the controller.
  The `windowConfiguration` lambda describes how the window appears
  (title, size, close behaviour, key handling); the trailing content
  lambda is the window's Compose content.
- `application { EnroApplicationContent(controller) }` is Compose
  Multiplatform's standard entry point — it renders every window the
  controller knows about.

The window's `WindowConfiguration` block runs inside a scope that has the
window's own `NavigationHandle` available as `navigation`, so
`onCloseRequest = { navigation.close() }` closes the window through the
navigation system rather than killing the process.

## Multi-window apps

Desktop apps often want more than one window — a main window plus
detached editors, palettes, debug panes, etc. Each window in Enro is its
own top-level navigation destination, opened through `controller.openWindow(...)`
or pushed from a destination via the regular `navigation.open(...)` API.

The runnable example for the recipes app — including a `MenuBar`, key
shortcuts, and the full window-configuration block — is the [desktop main
file][desktop-main]. The patterns there generalise to any multi-window
desktop app.

## What Enro provides on desktop

- Per-window navigation containers — each window can host its own
  backstack and predictive-back behaviour.
- Standard saved-state survival across in-process Compose recompositions.
  (Process restart is not handled out of the box; persist state yourself
  if you need it.)
- The full common API: `NavigationKey`, `NavigationKey.WithResult<T>`,
  `navigationHandle<T>()`, `registerForNavigationResult`,
  `NavigationDisplay`, scene strategies, plugins, decorators.

## See also

- [Installation](../getting-started/installation.md) for the multi-platform setup.
- [Recipes desktop main][desktop-main] — full working example with
  multi-window, `MenuBar`, key shortcuts.

[desktop-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/desktopMain/kotlin/main.kt
