
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.enro.annotations.NavigationComponent
import dev.enro.core.DesktopApplicationContext
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.controller.createNavigationController
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import dev.enro.tests.application.SelectDestination

@NavigationComponent
object EnroExampleApp

fun main() = application {
    val context = DesktopApplicationContext(
        this,
        createNavigationController {
            EnroExampleAppNavigation().invoke(this)
        }
    )

    CompositionLocalProvider(
        EnroLocalSavedStateRegistryOwner provides context.savedStateRegistryOwner,
        LocalNavigationHandle provides context.navigationHandle,
        LocalSaveableStateRegistry provides SaveableStateRegistry(
            restoredValues = mapOf(),
            canBeSaved = { key -> true },
        )
    ) {
        Window(
            onKeyEvent = {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    context.leafContext().navigationHandle.requestClose()
                    true
                } else {
                    false
                }
            },
            onCloseRequest = ::exitApplication,
            title = "Enro Test Application",
        ) {
            val saveable = LocalSaveableStateRegistry.current
            LaunchedEffect(saveable) {
                println("saveable: ${saveable?.canBeSaved(0)}")
            }
            val container = rememberNavigationContainer(
                root = SelectDestination,
                emptyBehavior = EmptyBehavior.Action {
                    exitApplication()
                    true
                },
            )

            Box(
                modifier = Modifier
                    .background(Color.LightGray)
                    .fillMaxSize()
            ) {
                container.Render()
            }
        }
    }
}
