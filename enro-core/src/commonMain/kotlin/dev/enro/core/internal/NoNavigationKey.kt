package dev.enro.core.internal

import dev.enro.core.EnroInternalNavigationKey
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
internal class NoNavigationKey(
    val contextType: String,
) : NavigationKey, EnroInternalNavigationKey

internal class NoKeyNavigationBinding : NavigationBinding<NoNavigationKey, Nothing>() {
    override val keyType: KClass<NoNavigationKey> = NoNavigationKey::class
    override val keySerializer: KSerializer<NoNavigationKey> = NoNavigationKey.serializer()
    override val destinationType: KClass<Nothing> = Nothing::class
    override val baseType: KClass<in Nothing> = Nothing::class
}

internal val NavigationHandle.hasKey get() = instruction.navigationKey !is NoNavigationKey
