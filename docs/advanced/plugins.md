---
title: Plugins
parent: Advanced Topics
nav_order: 5
---

# Plugins

A `NavigationPlugin` receives a callback when any destination in the app is
opened, becomes active or is closed. A plugin can set instance metadata,
send analytics events, or change the metadata that controls how a
destination is rendered. The destinations do not reference the plugin.

Use a plugin for behaviour that applies to every destination, or to every
destination of one type. Examples:

- Analytics: log a screen view for each opened destination.
- Metadata: store the time each instance was opened.
- Diagnostics: log a warning when a destination stays open longer than a
  limit.

## The NavigationPlugin class

Subclass `NavigationPlugin` and override the callbacks that the plugin
uses:

<!-- snippet: skip (API outline) -->
```kotlin
abstract class NavigationPlugin {
    open fun onAttached(controller: EnroController) {}
    open fun onDetached(controller: EnroController) {}

    open fun onOpened(navigationHandle: NavigationHandle<*>) {}
    open fun onActive(navigationHandle: NavigationHandle<*>) {}
    open fun onClosed(navigationHandle: NavigationHandle<*>) {}

    @AdvancedEnroApi
    open fun onDestinationCreated(
        destination: NavigationDestination<*>,
        additionalMetadata: MutableMap<String, Any?>,
    ) {}
}
```

| Callback | Called when |
|---|---|
| `onAttached(controller)` | The plugin is installed on a controller. Set up the plugin here. |
| `onDetached(controller)` | The plugin is removed from a controller. Release the plugin's resources here. |
| `onOpened(handle)` | A destination instance is composed for the first time. The handle's lifecycle is `CREATED` or later. |
| `onActive(handle)` | A destination instance becomes the active destination. |
| `onClosed(handle)` | A destination instance has been closed and the destination's ViewModels are cleared. The handle's lifecycle is `DESTROYED`. |
| `onDestinationCreated(destination, additionalMetadata)` | A destination is created for rendering. A value added to `additionalMetadata` replaces the destination metadata with the same key, and a `null` value removes the key. For example, a plugin can render a key as an overlay without a change to the destination declaration. |

`onDestinationCreated` is marked `@AdvancedEnroApi`, because a change to
rendering metadata can break how a destination is rendered.

## Installing a plugin

Install a plugin with `plugin(...)` in the `NavigationComponent`'s module:

<!-- snippet: plugins/install -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        plugin(MyAnalyticsPlugin())
        plugin(OpenedTimestampPlugin())
    }
)
```

A controller can have more than one plugin. Enro does not guarantee the
order in which the plugins receive a callback.

## Example: `OpenedTimestampPlugin`

`OpenedTimestampPlugin` stores the time each instance was first opened, in
epoch milliseconds, in the instance's metadata.

<!-- snippet: plugins/opened-timestamp -->
```kotlin
object OpenedAt : NavigationKey.MetadataKey<Long?>(default = null)

class OpenedTimestampPlugin : NavigationPlugin() {
    override fun onOpened(navigationHandle: NavigationHandle<*>) {
        val instance = navigationHandle.instance
        if (instance.metadata.get(OpenedAt) == null) {
            instance.metadata.set(OpenedAt, Clock.System.now().toEpochMilliseconds())
        }
    }
}
```

A destination reads the `OpenedAt` value from the instance metadata:

<!-- snippet: plugins/read -->
```kotlin
val openedAt = navigation.instance.metadata.get(OpenedAt)
```

The recipes app installs `OpenedTimestampPlugin` in `RecipesComponent`, and
the plugin sets `OpenedAt` on every destination instance in the recipes
app. The [OpenedTimestampPlugin recipe][timestamp-recipe] is the runnable
version.

`OpenedAt` is [instance
metadata](../core-concepts/navigation-keys.md#instance-metadata): data on
an instance that is not a property of the `NavigationKey`.

## What a plugin should not do

- **Do not navigate from a plugin.** To call `open` or `close` in response
  to an operation, or to change an operation, use an
  [interceptor](../core-concepts/navigation-containers.md#interceptors).
- **Do not put behaviour for one destination in a plugin.** Put behaviour
  for one destination in the destination or in the destination's
  ViewModel.
- **Do not depend on the order of plugins.** When two plugins set the same
  instance metadata key, choose one plugin to own the key.

## See also

- [Navigation Keys → Instance metadata](../core-concepts/navigation-keys.md#instance-metadata)
  — the instance metadata API.
- [Navigation Containers → Interceptors](../core-concepts/navigation-containers.md#interceptors)
  — interceptors change or cancel navigation operations.
- [OpenedTimestampPlugin recipe][timestamp-recipe] — the runnable
  `OpenedTimestampPlugin`.

[timestamp-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/plugins/OpenedTimestampPlugin.kt
