package dev.enro.platform

import android.os.Bundle
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.EnroController
import dev.enro.NavigationKey

private const val BundleInstanceKey = "dev.enro.platform.BundleInstanceKey"

@PublishedApi
internal fun Bundle.putNavigationKeyInstance(instance: NavigationKey.Instance<NavigationKey>): Bundle {
    val savedStateConfig = requireNotNull(EnroController.instance).serializers.savedStateConfiguration
    val encodedInstance = encodeToSavedState(instance, savedStateConfig)
    putBundle(BundleInstanceKey, encodedInstance)
    return this
}

@PublishedApi
internal fun Bundle.getNavigationKeyInstance(): NavigationKey.Instance<NavigationKey>? {
    val encodedInstance = getBundle(BundleInstanceKey) ?: return null
    val savedStateConfig = requireNotNull(EnroController.instance).serializers.savedStateConfiguration
    return decodeFromSavedState(encodedInstance, savedStateConfig)
}