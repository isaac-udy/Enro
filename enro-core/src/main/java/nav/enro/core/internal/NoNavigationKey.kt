package nav.enro.core.internal

import android.os.Bundle
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationKey
import nav.enro.core.Navigator
import nav.enro.core.NavigatorAnimations
import kotlin.reflect.KClass

@Parcelize
internal class NoNavigationKey(
    val contextType: Class<*>,
    val arguments: Bundle?
) : NavigationKey

internal class NoKeyNavigator: Navigator<NoNavigationKey, Nothing> {
    override val keyType: KClass<NoNavigationKey> = NoNavigationKey::class
    override val contextType: KClass<Nothing> = Nothing::class
    override val animations: NavigatorAnimations = NavigatorAnimations.default
}