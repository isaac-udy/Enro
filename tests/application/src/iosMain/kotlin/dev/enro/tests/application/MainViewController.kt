@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import dev.enro.core.NavigationKey
import dev.enro3.asInstance
import dev.enro3.controller.NavigationModuleAction
import dev.enro3.controller.internalCreateEnroController
import dev.enro3.ui.NavigationDisplay
import dev.enro3.ui.rememberNavigationContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UISceneActivationStateForegroundInactive
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@Serializable
object MainView : NavigationKey.SupportsPresent

fun install(navigationModule: NavigationModuleAction) {
    internalCreateEnroController {
        navigationModule.apply { invoke() }
    }.install(Unit)

    CoroutineScope(Dispatchers.Main).launch {
        delay(1000)
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

        val window = UIWindow().apply {
            rootViewController = CreateMainViewController()
        }
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
}

fun CreateMainViewController(): UIViewController = ComposeUIViewController {
    MainViewController()
}

@Composable
fun MainViewController() {
    runCatching {
        val container = rememberNavigationContainer(
            backstack = listOf(ListKey().asInstance()),
        )
        MaterialTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                NavigationDisplay(
                    state = container,
                )
            }
        }
    }.onFailure { it.printStackTrace() }
//    val navigation = navigationHandle()
//    val navigationContext = navigationContext
//    val container = rememberNavigationContainer(
//        root = SelectDestination,
//        emptyBehavior = EmptyBehavior.CloseParent,
//    )
//    Column(
//        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//            container.Render()
//            PredictiveBackArrow(
//                enabled = container.backstack.size > 1,
//                arrowTint = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
//                onBack = { navigationContext.leafContext().getNavigationHandle().requestClose() }
//            )
//        }
//    }
}
