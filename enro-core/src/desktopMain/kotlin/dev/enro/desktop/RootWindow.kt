package dev.enro.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.close
import dev.enro.context.RootContext
import dev.enro.handle.RootNavigationHandle
import dev.enro.handle.getOrCreateNavigationHandleHolder
import dev.enro.ui.LocalNavigationHandle
import dev.enro.ui.LocalRootContext

@Stable
public class RootWindow<out T: NavigationKey> internal constructor(
    private val instance: NavigationKey.Instance<T>,
    windowConfiguration: RootWindow<T>.() -> WindowConfiguration = { WindowConfiguration() },
    private val content: @Composable RootWindowScope<T>.() -> Unit,
) : LifecycleOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    public val backDispatcher: EnroBackDispatcher = EnroBackDispatcher()
    private val windowConfiguration: WindowConfiguration by mutableStateOf(
        windowConfiguration()
    )

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
        id = "RootWindow(${instance.key::class.simpleName})" + "$@${hashCode()}",
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
            val lazyRootWindowScope = remember<MutableState<RootWindowScope<T>?>> {
                mutableStateOf(null)
            }
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
                            val instance = instance
                            val holder = viewModelStoreOwner.getOrCreateNavigationHandleHolder(instance)
                            val navigationHandle = RootNavigationHandle(instance)
                            holder.navigationHandle = navigationHandle
                            navigationHandle.bindContext(context)
                            holder.navigationHandle
                        }
                        val rootWindowScope = remember(navigationHandle) {
                            val scope = RootWindowScope<T>(
                                rootWindow = this,
                                navigation = navigationHandle as RootNavigationHandle<T>,
                                frameWindowScope = windowScope,
                            )
                            lazyRootWindowScope.value = scope
                            return@remember scope
                        }

                        CompositionLocalProvider(
                            LocalRootContext provides context,
                            LocalBackGestureDispatcher provides backDispatcher,
                            LocalNavigationHandle provides navigationHandle,
                        ) {
                            rootWindowScope.content()
                        }
                    }
                }
                Window(
                    state = this.windowConfiguration.state,
                    visible = this.windowConfiguration.visible,
                    title = this.windowConfiguration.title,
                    icon = this.windowConfiguration.icon,
                    transparent = this.windowConfiguration.transparent,
                    undecorated = this.windowConfiguration.undecorated,
                    resizable = this.windowConfiguration.resizable,
                    enabled = this.windowConfiguration.enabled,
                    focusable = this.windowConfiguration.focusable,
                    alwaysOnTop = this.windowConfiguration.alwaysOnTop,
                    onPreviewKeyEvent = {
                        val scope = lazyRootWindowScope.value ?: return@Window false
                        this.windowConfiguration.onPreviewKeyEvent(scope, it)
                    },
                    onKeyEvent = {
                        val scope = lazyRootWindowScope.value ?: return@Window false
                        this.windowConfiguration.onKeyEvent(scope, it)
                    },
                    onCloseRequest = {
                        val scope = lazyRootWindowScope.value ?: return@Window
                        this.windowConfiguration.onCloseRequest(scope)
                    },
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
        val onPreviewKeyEvent: RootWindowScope<NavigationKey>.(KeyEvent) -> Boolean = { false },
        val onKeyEvent: RootWindowScope<NavigationKey>.(KeyEvent) -> Boolean = { false },
        val onCloseRequest: RootWindowScope<NavigationKey>.() -> Unit = { navigation.close() },
    )
}

public class RootWindowScope<out T: NavigationKey> internal constructor(
    private val rootWindow: RootWindow<T>,
    public val navigation: NavigationHandle<T>,
    private val frameWindowScope: FrameWindowScope,
) : FrameWindowScope by frameWindowScope {

    public val instance: NavigationKey.Instance<T>
        get() = navigation.instance

    public val key: T
        get() = navigation.key

    public val backDispatcher: EnroBackDispatcher
        get() = rootWindow.backDispatcher
}