package nav.enro.core.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import nav.enro.core.NavigationKey
import kotlin.reflect.KClass

internal class FragmentHost(
    internal val containerId: Int,
    internal val fragmentManager: FragmentManager
)
