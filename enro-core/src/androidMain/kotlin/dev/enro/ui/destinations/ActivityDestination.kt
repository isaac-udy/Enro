package dev.enro.ui.destinations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.platform.getNavigationKeyInstance
import dev.enro.platform.isResultFromEnro
import dev.enro.platform.putNavigationKeyInstance
import dev.enro.result.setDelegatedResult
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import kotlinx.coroutines.delay
import kotlin.reflect.KClass

public inline fun <reified T : NavigationKey, reified A : Activity> activityDestination(
    metadata: Map<String, Any> = emptyMap(),
): NavigationDestinationProvider<T> {
    return activityDestination(T::class, A::class, metadata)
}

public fun <T : NavigationKey, A : Activity> activityDestination(
    keyType: KClass<T>,
    activityType: KClass<A>,
    metadata: Map<String, Any> = emptyMap(),
): NavigationDestinationProvider<T> {
    return navigationDestination(
        metadata + mapOf(DirectOverlaySceneStrategy.overlay()),
    ) {
        val navigation = navigationHandle(keyType)
        val context = LocalContext.current

        val wasLaunched = rememberSaveable {
            mutableStateOf(false)
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.data?.isResultFromEnro() == true) {
                // If the result.data is considered to be a result from Enro, that
                // means that we've already delivered the result and we don't need to do
                // it again, so we close here without setting any result
                navigation.instance.setDelegatedResult(navigation.instance)
                navigation.execute(NavigationOperation { it - navigation.instance })
                return@rememberLauncherForActivityResult
            }
            when (result.resultCode) {
                Activity.RESULT_OK -> navigation.complete()
                else -> navigation.close()
            }
        }

        val intent = rememberSaveable {
            Intent(context, activityType.java)
                .putNavigationKeyInstance(navigation.instance)
        }

        LaunchedEffect(Unit) {
            if (wasLaunched.value) {
                delay(1000)
                error("Activity destination was not completed or closed within one second after resuming the destination from the back stack.")
            }
            wasLaunched.value = true
            launcher.launch(intent)
        }
    }
}

private const val IntentInstanceKey = "dev.enro.ui.destinations.ActivityDestination.IntentInstanceKey"

public fun Intent.putNavigationKeyInstance(instance: NavigationKey.Instance<*>): Intent {
    return putExtra(
        IntentInstanceKey, Bundle().putNavigationKeyInstance(
            instance.copy(
                metadata = instance.metadata.copy()
            )
        )
    )
}

public fun Intent.getNavigationKeyInstance(): NavigationKey.Instance<NavigationKey>? {
    return getBundleExtra(IntentInstanceKey)?.getNavigationKeyInstance()
}
