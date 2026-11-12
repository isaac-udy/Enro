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

<!-- snippet: installation/desktop -->
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

### A destination in its own window

`rootWindowDestination` binds a key to a window. `navigation.open(key)`
opens the window. `navigation.close()` and `navigation.complete(value)`
close it. A window destination can return a result.

<!-- snippet: desktop/window-destination -->
```kotlin
@NavigationDestination(NoteWindow::class)
val noteWindowDestination = rootWindowDestination<NoteWindow>(
    windowConfiguration = {
        RootWindow.WindowConfiguration(
            title = "New note",
            // The window's close button closes the destination without a result.
            onCloseRequest = { navigation.close() },
        )
    },
) {
    var text by remember { mutableStateOf("") }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Note ${navigation.key.number}", style = MaterialTheme.typography.titleLarge)
                TextField(value = text, onValueChange = { text = it })
                // Completing closes the window and delivers the result.
                Button(onClick = { navigation.complete(text) }) { Text("Save") }
                Button(onClick = { navigation.close() }) { Text("Discard") }
            }
        }
    }
}
```

To open an existing common destination in a window on desktop only,
annotate a `rootWindowDestination` with
`@NavigationDestination.PlatformOverride(Key::class)`.

See the [Multiple Windows recipe][windows-recipe].

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

[windows-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/desktopMain/kotlin/dev/enro/recipes/windows/MultipleWindows.kt
[desktop-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/app/desktop/src/desktopMain/kotlin/main.kt
