package dev.enro.serialization

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import kotlinx.serialization.KSerializer

@AdvancedEnroApi
public actual inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T> {
    return defaultSerializerForNavigationKey()
}