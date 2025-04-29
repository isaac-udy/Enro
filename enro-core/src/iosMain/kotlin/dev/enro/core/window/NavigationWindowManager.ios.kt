@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.core.window

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.savedState
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.enroNavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.plugins.EnroPlugin
import dev.enro.destination.uiviewcontroller.UIViewControllerNavigationBinding
import dev.enro.destination.uiviewcontroller.isEnroViewController
import dev.enro.destination.uiviewcontroller.navigationInstruction
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UISceneActivationStateForegroundInactive
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject

// Define a key for your associated object (can be a static object or address)
@OptIn(ExperimentalForeignApi::class)
private val NavigationContextPropertyKey = kotlinx.cinterop.staticCFunction<Unit> {}

@OptIn(ExperimentalForeignApi::class)
private val Navigation = kotlinx.cinterop.staticCFunction<Unit> {}

// Extension function to set the extra property
@OptIn(ExperimentalForeignApi::class)
internal var NSObject.navigationContext: NavigationContext<UIWindowScene>?
    get() {
        return objc_getAssociatedObject(
            this,
            NavigationContextPropertyKey
        ) as? NavigationContext<UIWindowScene>
    }
    set(value) {
        objc_setAssociatedObject(
            `object` = this,
            key = NavigationContextPropertyKey,
            value = value,
            policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
    }

public actual class NavigationWindowManager actual constructor(
    controller: NavigationController,
) : EnroPlugin() {

    private var pendingInstruction: AnyOpenInstruction? = null

    private val observer = WindowSceneObserver(
        onSceneActivated = { scene ->
            println("Scene activated: $scene")
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
                getArguments = { savedState() },
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
        onSceneDeactivated = { scene ->
            println("Scene deactivated: $scene")
        }
    )

    override fun onAttached(navigationController: NavigationController) {
        observer.attach()
    }

    override fun onDetached(navigationController: NavigationController) {
        observer.detatch()
    }

    @OptIn(ExperimentalForeignApi::class)
    public actual fun open(instruction: AnyOpenInstruction) {
        if (UIApplication.sharedApplication
                .connectedScenes.isEmpty()
        ) {
            pendingInstruction = instruction
            return
        }
        // TODO handle scenes
//        val scenes = UIApplication
//            .sharedApplication
//            .connectedScenes
//            .mapNotNull { it as? UIWindowScene }
        val binding = UIApplication
            .sharedApplication
            .enroNavigationController
            .bindingForInstruction(instruction)
        requireNotNull(binding) {
            "NavigationWindowManager.open failed: NavigationKey '${instruction.navigationKey::class.simpleName}'" +
                    " does not have a registered NavigationBinding."
        }
        binding as UIViewControllerNavigationBinding<*, out UIViewController>

        val controller = binding.constructDestination()
        controller.navigationInstruction = instruction

        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        if (controller::class.simpleName == "ComposeHostingViewController" && !controller.isEnroViewController) {
            error(
                "UIViewController for NavigationKey ${instruction.navigationKey::class.simpleName} was created as a" +
                        " ComposeHostingViewController, but was not created using EnroComposeUIViewController."
            )
        }
        val windowScene = UIApplication.sharedApplication
            .connectedScenes
            .filterIsInstance<UIWindowScene>()
            .first {
                // TODO need to choose more wisely here, and possibly handle background states
                it.activationState == UISceneActivationStateForegroundActive ||
                        it.activationState == UISceneActivationStateForegroundInactive
            }
        val existingEmptyWindow = windowScene.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.rootViewController == null }

        val window = existingEmptyWindow ?: UIWindow(windowScene)
        window.rootViewController = controller
        window.hidden = true
        window.alpha = 0.0
        window.makeKeyAndVisible()
        UIView.animateWithDuration(0.125) {
            window.alpha = 1.0
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
