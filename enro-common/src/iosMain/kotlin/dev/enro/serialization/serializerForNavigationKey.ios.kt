package dev.enro.serialization

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer

@OptIn(ExperimentalSerializationApi::class)
@AdvancedEnroApi
public actual inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T> {
    return defaultSerializerForNavigationKey()
}