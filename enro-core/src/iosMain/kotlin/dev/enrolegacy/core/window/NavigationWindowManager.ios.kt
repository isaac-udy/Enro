@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enrolegacy.core.window

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.enroNavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.plugins.EnroPlugin
import dev.enro.destination.ios.UIViewControllerNavigationBinding
import dev.enro.destination.ios.UIWindowNavigationBinding
import dev.enro.destination.ios.UIWindowScope
import dev.enro.destination.ios.navigationInstruction
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UISceneActivationStateForegroundInactive
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject

@OptIn(ExperimentalForeignApi::class)
private val NavigationContextPropertyKey = kotlinx.cinterop.staticCFunction<Unit> {}

@OptIn(ExperimentalForeignApi::class)
public var NSObject.navigationContext: NavigationContext<*>?
    get() {
        return objc_getAssociatedObject(
            this,
            NavigationContextPropertyKey
        ) as? NavigationContext<*>
    }
    internal set(value) {
        objc_setAssociatedObject(
            `object` = this,
            key = NavigationContextPropertyKey,
            value = value,
            policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
    }

public actual class NavigationWindowManager actual constructor(
    private val controller: NavigationController,
) : EnroPlugin() {

    private var pendingInstruction: AnyOpenInstruction? = null

    private val observer = WindowSceneObserver(
        onSceneActivated = { scene ->
            val owners = object : SavedStateRegistryOwner, ViewModelStoreOwner {
                private val savedStateRegistryController = SavedStateRegistryController.create(this)
                override val savedStateRegistry: SavedStateRegistry =
                    savedStateRegistryController.savedStateRegistry
                override val viewModelStore: ViewModelStore = ViewModelStore()
                private val lifecycleRegistry = LifecycleRegistry(this)
                override val lifecycle: Lifecycle get() = lifecycleRegistry

                init {
                    enableSavedStateHandles()
                    savedStateRegistryController.performRestore(null)
                    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                }
            }
            scene.navigationContext = NavigationContext(
                contextReference = scene,
                getController = { controller },
                getParentContext = { null },
                getContextInstruction = { null },
                getViewModelStoreOwner = { owners },
                getSavedStateRegistryOwner = { owners },
                getLifecycleOwner = { owners },
                onBoundToNavigationHandle = { }
            ).apply {
                controller.dependencyScope.get<OnNavigationContextCreated>()
                    .invoke(this, null)
            }
            pendingInstruction?.let {
                pendingInstruction = null
                open(it)
            }
        },
        onSceneDeactivated = { scene -> }
    )

    override fun onAttached(navigationController: NavigationController) {
        observer.attach()
    }

    override fun onDetached(navigationController: NavigationController) {
        observer.detatch()
    }

    @OptIn(ExperimentalForeignApi::class)
    public actual fun open(instruction: AnyOpenInstruction) {
        if (UIApplication.sharedApplication.connectedScenes.isEmpty()) {
            pendingInstruction = instruction
            return
        }
        // TODO handle scenes
//        val scenes = UIApplication
//            .sharedApplication
//            .connectedScenes
//            .mapNotNull { it as? UIWindowScene }
        val windowScene = UIApplication.sharedApplication
            .connectedScenes
            .filterIsInstance<UIWindowScene>()
            .first {
                // TODO need to choose more wisely here, and possibly handle background states
                it.activationState == UISceneActivationStateForegroundActive ||
                        it.activationState == UISceneActivationStateForegroundInactive
            }

        val navigationController = UIApplication
            .sharedApplication
            .enroNavigationController

        val hostedInstruction = navigationController
            .dependencyScope
            .get<HostInstructionAs>()
            .invoke(
                hostType = UIWindow::class,
                navigationContext = windowScene.navigationContext as NavigationContext<*>,
                instruction = instruction,
            )

        val binding = navigationController.bindingForInstruction(hostedInstruction)

        requireNotNull(binding) {
            "NavigationWindowManager.open failed: NavigationKey '${instruction.navigationKey::class.simpleName}'" +
                    " does not have a registered NavigationBinding."
        }
        require(binding is UIWindowNavigationBinding<*, *>) {
            "NavigationWindowManager.open failed: NavigationBinding '${binding::class.simpleName}' is not a" +
                    " UIWindowNavigationBinding."
        }
        @Suppress("UNCHECKED_CAST")
        binding as UIWindowNavigationBinding<NavigationKey, UIWindow>

        val existingEmptyWindow = windowScene.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.rootViewController == null }

        val windowProviderScope = UIWindowScope<NavigationKey>(
            navigationKey = hostedInstruction.navigationKey,
            controller = navigationController,
            instruction = hostedInstruction,
        )
        val window = binding.windowProvider.createUIWindow(windowProviderScope)
        window.rootViewController?.navigationInstruction = hostedInstruction
        window.windowScene = windowScene
        window.hidden = true
        window.alpha = 0.0
        window.makeKeyAndVisible()
        UIView.animateWithDuration(0.125) {
            window.alpha = 1.0
        }
        if (existingEmptyWindow != null) {
            existingEmptyWindow.windowScene = null
        }
    }

    public actual fun close(context: NavigationContext<*>, andOpen: AnyOpenInstruction?) {
        val toClose = UIApplication
            .sharedApplication
            .connectedScenes
            .mapNotNull { it as? UIWindowScene }
            .flatMap { it.windows.filterIsInstance<UIWindow>() }
            .mapNotNull { it.rootViewController }
            .firstOrNull { it.navigationInstruction?.instructionId == context.instruction.instructionId }

        val window = toClose?.view?.window ?: return
        val scene = window.windowScene ?: return
        val isLastWindow = scene.windows.filterIsInstance<UIWindow>()
            .filter { !it.isSystemWindow }
            .size == 1

        if (isLastWindow && andOpen == null) {
            scene.requestDestruction()
        } else {
            window.hidden = false
            window.alpha = 1.0
            UIView.animateWithDuration(
                duration = 0.125,
                animations = {
                    window.alpha = 0.0
                },
                completion = {
                    window.windowScene = null
                }
            )
            if (andOpen != null) {
                open(andOpen)
            }
        }
    }

    internal actual fun isExplicitWindowInstruction(instruction: AnyOpenInstruction): Boolean {
        val binding = controller.bindingForInstruction(instruction)
        return instruction.isOpenInWindow()
                || binding is UIWindowNavigationBinding<*, *>
                || binding is UIViewControllerNavigationBinding<*, *>
    }

    public actual companion object
}

private val systemWindowNames = setOf(
    "UITextEffectsWindow",
    "UIPredictionViewController",
    "UIInputWindowController",
    "UISystemKeyboardDockController",
    "Overlay",
)
private val UIWindow.isSystemWindow: Boolean get() {
    return this::class.simpleName in systemWindowNames
}
