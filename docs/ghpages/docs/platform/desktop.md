---
title: Desktop
parent: Platform-Specific Guides
nav_order: 3
---

# Desktop

Enro runs on Compose Multiplatform for Desktop. `main` calls
`installNavigationController` once, then opens each window through the
returned controller.

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

- `installNavigationController(Unit)` installs the controller for the
  process and returns the `EnroController`.
- `controller.openWindow(...)` registers a window with the controller.
  The `windowConfiguration` lambda returns the window's
  `RootWindow.WindowConfiguration`: title, window state, visibility, key
  event handlers and `onCloseRequest`. The trailing lambda is the
  window's Compose content.
- `EnroApplicationContent(controller)`, called inside Compose
  Multiplatform's `application { }`, renders every window registered
  with the controller.

The `onCloseRequest` lambda and the key event lambdas have a
`RootWindowScope` receiver, and `RootWindowScope.navigation` is the
window's `NavigationHandle`. `onCloseRequest = { navigation.close() }`
closes the window's destination, which removes the window.
`navigation.close()` is the default `onCloseRequest`.

## Multi-window apps

Each window is a root navigation destination. `controller.openWindow(...)`
opens a window from outside a destination. `navigation.open(...)` opens a
window destination from another destination.

The [desktop main file][desktop-main] of the recipes app is an example
with a `MenuBar`, key shortcuts and a complete `WindowConfiguration`.

### A destination in its own window

`rootWindowDestination` binds a key to a window. `navigation.open(key)`
opens the window. `navigation.close()` and `navigation.complete(value)`
close the window. A window destination bound to a
`NavigationKey.WithResult<T>` key returns a result with `complete`.

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

- Each window hosts its own navigation containers and has its own
  `NavigationHandle`.
- Enro does not restore the backstack after a process restart on
  desktop. An app that requires the backstack after a restart must save
  and restore the backstack itself.
- The common API: `NavigationKey`, `NavigationKey.WithResult<T>`,
  `navigationHandle<T>()`, `registerForNavigationResult`,
  `NavigationDisplay`, scene strategies, plugins, decorators.

## See also

- [Installation](../getting-started/installation.md) — installation on every platform.
- [Recipes desktop main][desktop-main] — an example with several
  windows, a `MenuBar` and key shortcuts.

[windows-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/desktopMain/kotlin/dev/enro/recipes/windows/MultipleWindows.kt
[desktop-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/app/desktop/src/desktopMain/kotlin/main.kt
