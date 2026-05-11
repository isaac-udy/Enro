---
title: Plugins
parent: Advanced Topics
nav_order: 5
---

# Plugins

A `NavigationPlugin` is a way to attach behaviour to *every* destination in
your app, without touching the destinations themselves. Plugins observe
navigation events as they happen — instances being opened, becoming active,
being closed — and can react by tagging metadata, dispatching analytics,
running diagnostics, or modifying the way destinations are rendered.

Plugins are the right tool when:

- You want to do something for **every** destination (or every destination
  that matches a type), uniformly.
- The behaviour is cross-cutting — it doesn't belong inside a specific
  screen.
- You want the destinations themselves to stay unaware that the behaviour
  exists.

Examples include analytics ("log a screen-view for every opened
destination"), origin tagging ("stamp every instance with the timestamp it
was opened at"), or diagnostics ("warn if a screen stays composed for more
than N seconds").

## The NavigationPlugin interface

Subclass `NavigationPlugin` and override whichever hooks you care about:

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

| Hook | Fires when |
|---|---|
| `onAttached(controller)` | The plugin is installed on a controller. Typical setup point. |
| `onDetached(controller)` | The plugin is removed from a controller. Typical teardown point. |
| `onOpened(handle)` | A new destination instance has just been opened. The handle's lifecycle is at `CREATED` or later. |
| `onActive(handle)` | A destination instance has become the active (visible/top) destination. |
| `onClosed(handle)` | A destination instance is closed and about to be removed from the backstack. |
| `onDestinationCreated(destination, additionalMetadata)` | (Advanced) A destination is being assembled for rendering. You can add or override rendering metadata here — for example, to force a key to render as an overlay without modifying its destination definition. |

`onOpened`, `onActive`, and `onClosed` are the common hooks. `onDestinationCreated`
is marked `@AdvancedEnroApi` because changing rendering metadata can break
how destinations are presented.

## Installing a plugin

Plugins are installed in the `NavigationComponent`'s module DSL:

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        plugin(MyAnalyticsPlugin())
        plugin(OpenedTimestampPlugin())
    }
)
```

A plugin is installed once per controller. Multiple plugins can be
installed; their hooks fire in installation order.

## A worked example

Below is `OpenedTimestampPlugin` — a small plugin that stamps every
instance with the epoch-millisecond time it was first opened, via the
instance's `Metadata`.

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

Any destination can then read its own opened-at timestamp:

```kotlin
val openedAt = navigation.instance.metadata.get(OpenedAt)
```

Installed in `RecipesComponent`, the plugin stamps every destination in the
recipes app, regardless of where the key lives or who opens it. The runnable
version is the [OpenedTimestampPlugin recipe][timestamp-recipe].

This is the canonical example of using **[instance
metadata](../core-concepts/navigation-keys.md#instance-metadata)** to carry
data that any destination might want but that isn't part of any key's
contract.

## A note on what plugins shouldn't do

- **Plugins shouldn't perform navigation.** If you find yourself wanting to
  call `open` or `close` from a plugin hook, you probably want an
  [interceptor](../core-concepts/navigation-containers.md#interceptors)
  instead — those are designed to observe and rewrite operations as they
  flow.
- **Don't put per-destination behaviour in a plugin.** If the behaviour
  applies to one specific destination, put it on the destination itself
  (or in its ViewModel). Plugins are for cross-cutting concerns.
- **Don't rely on hook ordering across plugins.** If two plugins both
  modify the same instance metadata, decide on a clear ownership rule —
  installation order is not a stable contract.

## See also

- [Navigation Keys → Instance metadata](../core-concepts/navigation-keys.md#instance-metadata)
  for what `Metadata` is and isn't.
- [Navigation Containers → Interceptors](../core-concepts/navigation-containers.md#interceptors)
  for the close cousin of plugins, used for rewriting operations.
- [OpenedTimestampPlugin recipe][timestamp-recipe] — the worked example
  above.

[timestamp-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/plugins/OpenedTimestampPlugin.kt
