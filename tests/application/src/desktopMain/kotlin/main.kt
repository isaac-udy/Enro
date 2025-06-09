
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.NavigationOperation
import dev.enro.annotations.NavigationComponent
import dev.enro.asInstance
import dev.enro.context.RootContext
import dev.enro.context.activeLeafDestination
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.tests.application.SelectDestination
import dev.enro.ui.LocalRootContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

@NavigationComponent
object ExampleApplicationNavigation : NavigationComponentConfiguration()

fun main() = application {
    val controller = ExampleApplicationNavigation.rememberNavigationController()

    val onEscape = remember { mutableStateOf<() -> Unit>({}) }
    Window(
        onCloseRequest = ::exitApplication,
        onKeyEvent = remember(onEscape) {{ event ->
            if (event.type == KeyEventType.KeyUp && event.key == Key.Escape) {
                onEscape.value.invoke()
                true
            } else false
        }}
    ) {
        val lifecycleOwner = requireNotNull(LocalLifecycleOwner.current)
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)
        val activeChildId = remember { mutableStateOf<String?>(null) }
        val rootContext = remember {
            RootContext(
                parent = this@application,
                controller = controller,
                lifecycleOwner = lifecycleOwner,
                viewModelStoreOwner = viewModelStoreOwner,
                defaultViewModelProviderFactory = (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)
                    ?: object :
                        HasDefaultViewModelProviderFactory {
                        override val defaultViewModelCreationExtras: CreationExtras = CreationExtras.Empty
                        override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                            get() = TODO("Not yet implemented")
                    },
                activeChildId = activeChildId
            )
        }
        onEscape.value = {
            val activeDestination = rootContext.activeLeafDestination()
            if (activeDestination == null) {
                exitApplication()
            } else {
                activeDestination.parent.container.execute(
                    context = rootContext,
                    operation = NavigationOperation.Close(activeDestination.instance)
                )
            }
        }
        CompositionLocalProvider(
            LocalRootContext provides rootContext
        ) {

            val container = rememberNavigationContainer(
                backstack = listOf(SelectDestination().asInstance())
            )
            NavigationDisplay(container)
        }
    }
}
