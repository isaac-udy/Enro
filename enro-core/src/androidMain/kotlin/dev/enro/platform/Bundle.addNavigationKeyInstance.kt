package dev.enro.platform

import android.os.Bundle
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi

private const val BundleInstanceKey = "dev.enro.platform.BundleInstanceKey"

@AdvancedEnroApi
public fun Bundle.putNavigationKeyInstance(instance: NavigationKey.Instance<NavigationKey>): Bundle {
    val savedStateConfig = requireNotNull(EnroController.instance).serializers.savedStateConfiguration
    val encodedInstance = encodeToSavedState(instance, savedStateConfig)
    putBundle(BundleInstanceKey, encodedInstance)
    return this
}

@AdvancedEnroApi
public fun Bundle.getNavigationKeyInstance(): NavigationKey.Instance<NavigationKey>? {
    val encodedInstance = getBundle(BundleInstanceKey) ?: return null
    val savedStateConfig = requireNotNull(EnroController.instance).serializers.savedStateConfiguration
    return decodeFromSavedState(encodedInstance, savedStateConfig)
}