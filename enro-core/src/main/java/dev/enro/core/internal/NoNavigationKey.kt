package dev.enro.core.internal

import android.os.Bundle
import dev.enro.core.EnroInternalNavigationKey
import dev.enro.core.NavigationKey
import dev.enro.core.Navigator
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

@Parcelize
internal class NoNavigationKey(
    val contextType: Class<*>,
    val arguments: Bundle?
) : NavigationKey, EnroInternalNavigationKey

internal class NoKeyNavigator: Navigator<NoNavigationKey, Nothing> {
    override val keyType: KClass<NoNavigationKey> = NoNavigationKey::class
    override val contextType: KClass<Nothing> = Nothing::class
}