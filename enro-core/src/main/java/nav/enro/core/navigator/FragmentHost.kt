package nav.enro.core.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.reflect.KClass

class FragmentHostDefinition internal constructor(
    internal val containerView: Int,
    private val acceptFunction: (fragmentType: KClass<out Fragment>) -> Boolean
) {
    fun accepts(fragmentType: KClass<out Fragment>) = acceptFunction(fragmentType)

    internal fun createFragmentHost(fragmentManager: FragmentManager) =
        FragmentHost(containerView, fragmentManager)
}

internal class FragmentHost(
    internal val containerView: Int,
    internal val fragmentManager: FragmentManager
)
