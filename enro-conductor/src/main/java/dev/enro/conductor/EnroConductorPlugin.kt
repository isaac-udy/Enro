package dev.enro.conductor

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.createNavigationComponent
import dev.enro.core.plugins.EnroPlugin

public class EnroConductorPlugin : EnroPlugin() {

    private val component = createNavigationComponent {  }

    override fun onAttached(navigationController: NavigationController) {
        navigationController.addComponent(component)
    }

    override fun onOpened(navigationHandle: NavigationHandle) {
    }

    override fun onActive(navigationHandle: NavigationHandle) {
    }

    override fun onClosed(navigationHandle: NavigationHandle) {
    }
}

@Composable
public fun ComposableHostForConductorController(
    controller: () -> Controller
) {
    val controllerHostId = rememberSaveable { View.generateViewId() }
    val activity = localActivity

    AndroidView(factory = { context ->
        ChangeHandlerFrameLayout(context).apply {
            id = controllerHostId
            val router = Conductor.attachRouter(activity, this, null)
            if(!router.hasRootController()) {
                router.setRoot(
                    RouterTransaction.with(controller())
                )
            }
        }
    })
}
private val localActivity @Composable get() = LocalContext.current.let {
    remember(it) {
        var ctx = it
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                break
            }
            ctx = ctx.baseContext
        }

        ctx as? Activity
            ?: throw IllegalStateException("Could not find Activity up from $it")
    }
}