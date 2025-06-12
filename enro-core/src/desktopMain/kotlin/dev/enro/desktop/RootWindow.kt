package dev.enro.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.LocalBackGestureDispatcher
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.context.RootContext
import dev.enro.handle.RootNavigationHandle
import dev.enro.handle.getOrCreateNavigationHandleHolder
import dev.enro.platform.EnroLog
import dev.enro.ui.LocalNavigationHandle
import dev.enro.ui.LocalRootContext
import kotlinx.serialization.Serializable

// TODO need to make RootWindow accept NavigationKey type as generic arg?
@Stable
public open class RootWindow(
    private val instance: NavigationKey.Instance<NavigationKey>? = null
) : LifecycleOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    public val backDispatcher: EnroBackDispatcher = EnroBackDispatcher()
    public var windowConfiguration: WindowConfiguration by mutableStateOf(
        WindowConfiguration(
            onCloseRequest = {
                close()
            }
        )
    )
        protected set

    public val controller: EnroController = requireNotNull(EnroController.instance) {
        "EnroController instance has not been initialized yet. Make sure you have installed the EnroController before instantiating a RootWindow."
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private var windowViewModelStoreOwner: ViewModelStoreOwner? = null
    override val viewModelStore: ViewModelStore
        get() {
            return requireNotNull(windowViewModelStoreOwner) {
                "windowViewModelStoreOwner has not been initialized yet"
            }.viewModelStore
        }
    override val defaultViewModelCreationExtras: CreationExtras
        get() {
            val windowViewModelStoreOwner = requireNotNull(windowViewModelStoreOwner) {
                "windowViewModelStoreOwner has not been initialized yet"
            } as? HasDefaultViewModelProviderFactory
            return windowViewModelStoreOwner?.defaultViewModelCreationExtras ?: CreationExtras.Empty
        }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            val windowViewModelStoreOwner = requireNotNull(windowViewModelStoreOwner) {
                "windowViewModelStoreOwner has not been initialized yet"
            } as? HasDefaultViewModelProviderFactory
            return windowViewModelStoreOwner?.defaultViewModelProviderFactory ?: error(
                "windowViewModelStoreOwner does not have a defaultViewModelProviderFactory"
            )
        }

    private val activeChildId = mutableStateOf<String?>(null)

    public val context: RootContext = RootContext(
        id = (this::class.qualifiedName ?: "UnkownRootWindow") + "$@${hashCode()}",
        parent = this,
        controller = controller,
        lifecycleOwner = this,
        viewModelStoreOwner = this,
        defaultViewModelProviderFactory = this,
        activeChildId = activeChildId,
    )

    @OptIn(ExperimentalComposeUiApi::class)
    internal val movableWindowContent = movableContentOf {
        key(context.id) {
            if (controller.rootContextRegistry.getAllContexts().contains(context)) {
                val movableContent = remember {
                    movableContentOf { windowScope: FrameWindowScope ->
                        val viewModelStoreOwner = LocalViewModelStoreOwner.current
                        requireNotNull(viewModelStoreOwner) {
                            "No ViewModelStoreOwner was provided for the RootWindow."
                        }
                        windowViewModelStoreOwner = viewModelStoreOwner
                        // Get or create the NavigationHandleHolder for this destination
                        val navigationHandle = remember(viewModelStoreOwner) {
                            val instance = instance ?: DefaultRootWindowNavigationKey.asInstance()
                            val holder = viewModelStoreOwner.getOrCreateNavigationHandleHolder(instance)
                            val navigationHandle = RootNavigationHandle(instance)
                            holder.navigationHandle = navigationHandle
                            navigationHandle.bindContext(context)

                            EnroLog.error("Created RootNavigationHandle in $context")
                            holder.navigationHandle
                        }

                        CompositionLocalProvider(
                            LocalRootContext provides context,
                            LocalBackGestureDispatcher provides backDispatcher,
                            LocalNavigationHandle provides navigationHandle,
                        ) {
                            windowScope.Content()
                        }
                    }
                }
                Window(
                    state = windowConfiguration.state,
                    visible = windowConfiguration.visible,
                    title = windowConfiguration.title,
                    icon = windowConfiguration.icon,
                    transparent = windowConfiguration.transparent,
                    undecorated = windowConfiguration.undecorated,
                    resizable = windowConfiguration.resizable,
                    enabled = windowConfiguration.enabled,
                    focusable = windowConfiguration.focusable,
                    alwaysOnTop = windowConfiguration.alwaysOnTop,
                    onPreviewKeyEvent = windowConfiguration.onPreviewKeyEvent,
                    onKeyEvent = windowConfiguration.onKeyEvent,
                    onCloseRequest = windowConfiguration.onCloseRequest,
                ) {
                    val localLifecycleState = LocalLifecycleOwner.current
                        .lifecycle
                        .currentStateFlow
                        .collectAsState()
                        .value

                    lifecycleRegistry.currentState = localLifecycleState

                    movableContent.invoke(this)
                }

                DisposableEffect(Unit) {
                    onDispose {
                        if (!controller.rootContextRegistry.getAllContexts().contains(context)) {
                            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                        }
                    }
                }
            }
        }
    }

    @Composable
    protected open fun FrameWindowScope.Content() {
    }

    public fun close() {
        controller.rootContextRegistry.unregister(context)
        // De-registering the window is handled by the Disposable effect in Composition
    }


    public data class WindowConfiguration(
        val state: WindowState = WindowState(),
        val visible: Boolean = true,
        val title: String = "Untitled",
        val icon: Painter? = null,
        val undecorated: Boolean = false,
//        val decoration: WindowDecoration = WindowDecoration.SystemDefault,
        val transparent: Boolean = false,
        val resizable: Boolean = true,
        val enabled: Boolean = true,
        val focusable: Boolean = true,
        val alwaysOnTop: Boolean = false,
        val onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
        val onKeyEvent: (KeyEvent) -> Boolean = { false },
        val onCloseRequest: () -> Unit,
    )
}

@Serializable
internal object DefaultRootWindowNavigationKey : NavigationKey