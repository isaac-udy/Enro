---
title: Deep Links
parent: Advanced Topics
nav_order: 9
---

# Deep links

A deep link is a URL that opens a specific screen. Handling a deep link
takes two steps:

1. Resolve the URL's path to a `NavigationKey`.
2. Add the `NavigationKey` to a container's backstack.

[Path bindings](path-bindings.md) perform the first step. This page covers
the second step, and how each platform delivers the URL to the app.

## Open a key from a path

`getNavigationKeyFromPath` resolves a path against the path bindings
registered on the `NavigationComponent`. `getNavigationKeyFromPath` returns
`null` when no path binding matches. The `openDeepLink` function below
resolves a path and adds the resolved key to a container's backstack:

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

Declare an intent filter for the links on the Activity. Read the link from
the launch intent and from `onNewIntent`. The Activity below stores the
path in Compose state and opens the path after the root container is
created:

<!-- snippet: deep-links/android-activity -->
```kotlin
class RecipesActivity : AppCompatActivity() {
    // The path of an `enrorecipes://app/...` link that has not been opened yet.
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // After a configuration change the Activity receives the launch intent
        // again; the link in the launch intent has already been handled.
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

    // Called for a link that is opened while the Activity is already running.
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

Send a test link with `adb`:

```
adb shell am start -a android.intent.action.VIEW -d "enrorecipes://app/users/alice"
```

## Web

`rememberInitialBackstackFromUrl` reads the browser's URL when the app
loads and resolves the URL's path against the path bindings. See
[Web](../platform/web.md#cold-loading-from-a-url).

## A backstack for the link

A backstack that contains only the linked screen has no screen for Back to
show. To provide one, set the whole backstack: the linked screen last,
after the screens a user would open on the way to the linked screen.

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

Back from the article shows the article's category. Back from the category
shows the home screen.

## See also

- [Path Bindings](path-bindings.md) — binding paths to keys.
- [Handling a Deep Link recipe][handle-recipe] — `openDeepLink`, with buttons
  that simulate a deep link.
- [Advanced Deep Link recipe][advanced-recipe] — setting a backstack for a
  deep link.

[handle-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/HandleDeepLink.kt
[advanced-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/deeplink/AdvancedDeepLink.kt
