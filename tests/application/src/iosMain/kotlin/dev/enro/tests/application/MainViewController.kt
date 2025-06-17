@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.tests.application

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import dev.enro.controller.NavigationModuleAction
import dev.enro.controller.internalCreateEnroController
import dev.enro.platform.EnroUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UISceneActivationStateForegroundInactive
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

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

fun CreateMainViewController(): UIViewController = EnroUIViewController {
    MainViewController()
}

@Composable
fun MainViewController() {
    Text("Hello, iOS!")
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
