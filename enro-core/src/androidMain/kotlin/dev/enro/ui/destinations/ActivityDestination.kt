package dev.enro.ui.destinations

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.enro.NavigationKey
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.DirectOverlaySceneStrategy

public inline fun <reified T : NavigationKey, reified A : Activity> activityDestination(
    metadata: Map<String, Any> = emptyMap(),
): NavigationDestinationProvider<T> {
    val activityType = A::class.java
    return navigationDestination(
        metadata + mapOf(DirectOverlaySceneStrategy.overlay()),
    ) {
        val navigation = navigationHandle<T>()
        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> navigation.complete()
                else -> navigation.close()
            }
        }

        val intent = remember(navigation.key) {
            Intent(context, activityType)
        }

        LaunchedEffect(intent) {
            launcher.launch(intent)
        }
    }
}
