package dev.enro.tests.application.samples.recipes

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.directOverlay

@NavigationDestination.PlatformOverride(CreateRecipe::class)
val createRecipeDesktopDestination = navigationDestination<CreateRecipe>(
    metadata = {
        directOverlay()
    }
) {
    val destination = remember { createRecipeDestination.create(navigation.instance) }
    Window(
        title = "Create Recipe",
        onCloseRequest = { navigation.close() },
    ) {
        destination.content()
    }
}