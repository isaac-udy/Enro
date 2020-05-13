package nav.enro.core.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import nav.enro.core.NavigationKey
import kotlin.reflect.KClass

class FragmentHostDefinition internal constructor(
    internal val containerView: Int,
    private val acceptFunction: (fragmentType: KClass<out NavigationKey>) -> Boolean
) {
    fun accepts(keyType: KClass<out NavigationKey>) = acceptFunction(keyType)

    internal fun createFragmentHost(fragmentManager: FragmentManager) =
        FragmentHost(containerView, fragmentManager)
}

internal class FragmentHost(
    internal val containerView: Int,
    internal val fragmentManager: FragmentManager
)
