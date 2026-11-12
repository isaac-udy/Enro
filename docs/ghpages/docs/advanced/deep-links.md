---
title: Deep Links
parent: Advanced Topics
nav_order: 9
---

# Deep links

A deep link is a URL that opens a specific screen. Handling one takes two
steps:

1. Turn the URL's path into a `NavigationKey`.
2. Put that key on a container's backstack.

[Path bindings](path-bindings.md) do the first step. This page covers the
second, and where the URL comes from on each platform.

## Open a key from a path

`getNavigationKeyFromPath` resolves a path against the path bindings
registered on your `NavigationComponent`. It returns `null` when no binding
matches. This function resolves a path and opens the result on a container:

<!-- snippet: deep-links/open-deep-link -->
```kotlin
fun NavigationContainerState.openDeepLink(
    context: NavigationContext,
    path: String,
): Boolean {
    val key = context.getNavigationKeyFromPath(path) ?: return false
    updateBackstack { backstack -> (backstack + key.asInstance()).asBackstack() }
    return true
}
```

`path` is the path and query of the URL, such as `/users/alice` or
`/products/shoe-1?source=email`. Get the `NavigationContext` in a Composable
with `LocalNavigationContext.current`.

## Android

Declare an intent filter for your links on the Activity, then read the link
from the launch intent and from `onNewIntent`. This Activity keeps the path
in state and opens it once the root container exists:

<!-- snippet: deep-links/android-activity -->
```kotlin
class RecipesActivity : AppCompatActivity() {
    // The path of an `enrorecipes://app/...` link that has not been opened yet.
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // After a configuration change the launch intent is delivered again;
        // the link it carries has already been handled.
        if (savedInstanceState == null) {
            pendingDeepLink = intent.deepLinkPath()
        }
        setContent {
            MaterialTheme {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(SelectRecipe.asInstance()),
                )
                val context = LocalNavigationContext.current
                LaunchedEffect(pendingDeepLink) {
                    val path = pendingDeepLink ?: return@LaunchedEffect
                    pendingDeepLink = null
                    // See the Handling a Deep Link recipe.
                    container.openDeepLink(context, path)
                }
                NavigationDisplay(state = container)
            }
        }
    }

    // A link that arrives while the Activity is already running.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLink = intent.deepLinkPath()
    }
}

/** `enrorecipes://app/users/alice?tab=1` becomes `/users/alice?tab=1`. */
private fun Intent.deepLinkPath(): String? {
    val uri = data?.takeIf { it.scheme == "enrorecipes" } ?: return null
    val path = uri.path?.takeIf { it.isNotEmpty() } ?: "/"
    return uri.query?.let { "$path?$it" } ?: path
}
```

Test it with:

```
adb shell am start -a android.intent.action.VIEW -d "enrorecipes://app/users/alice"
```

## Web

`rememberInitialBackstackFromUrl` reads the address bar when the app loads
and resolves it the same way. See [Web](../platform/web.md#cold-loading-from-a-url).

## A backstack for the link

Opening only the linked screen leaves Back with nowhere sensible to go. To
give the link a history, set the whole backstack: the linked screen, with
the screens the user would have passed through underneath it.

<!-- snippet: deep-links/synthetic-backstack -->
```kotlin
container.updateBackstack {
    backstackOf(
        AppHome.asInstance(),
        Category("tech").asInstance(),
        Article("deep-link-article", "tech").asInstance(),
    )
}
```

Back from the article now goes to its category, then home.

## See also

- [Path Bindings](path-bindings.md) — declaring which paths map to which keys.
- [Handling a Deep Link recipe][handle-recipe] — `openDeepLink`, with buttons
  that simulate a link arriving.
- [Advanced Deep Link recipe][advanced-recipe] — the synthetic backstack.

[handle-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/HandleDeepLink.kt
[advanced-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/AdvancedDeepLink.kt
