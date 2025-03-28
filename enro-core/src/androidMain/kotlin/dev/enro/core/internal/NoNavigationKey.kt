package dev.enro.core.internal

import android.os.Bundle
import android.os.Parcelable
import dev.enro.KClassParceler
import dev.enro.core.EnroInternalNavigationKey
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.forParcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import kotlin.reflect.KClass

@Parcelize
internal class NoNavigationKey(
    val contextType: @WriteWith<KClassParceler> KClass<*>,
    val arguments: Bundle?
) : Parcelable, NavigationKey, EnroInternalNavigationKey

internal class NoKeyNavigationBinding : NavigationBinding<NoNavigationKey, Nothing> {
    override val keyType: KClass<NoNavigationKey> = NoNavigationKey::class
    override val keySerializer: NavigationKeySerializer<NoNavigationKey> = NavigationKeySerializer.forParcelable()
    override val destinationType: KClass<Nothing> = Nothing::class
    override val baseType: KClass<in Nothing> = Nothing::class
}

internal val NavigationHandle.hasKey get() = instruction.navigationKey !is NoNavigationKey
