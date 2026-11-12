package dev.enro.recipes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.close

/**
 * The scaffold for a recipe screen. The top app bar has a back button that calls
 * `close()` on [navigation], which returns to the SelectRecipe list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : NavigationKey> RecipeScaffold(
    title: String,
    navigation: NavigationHandle<T>,
    topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = { navigation.close() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to recipes",
                    )
                }
            },
        )
    },
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        topBar = topBar,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}
