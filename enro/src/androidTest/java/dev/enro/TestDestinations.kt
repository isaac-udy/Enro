package dev.enro
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
data class DefaultActivityKey(val id: String) : NavigationKey

@NavigationDestination(DefaultActivityKey::class)
class DefaultActivity : TestActivity() {
    private val navigation by navigationHandle<DefaultActivityKey> {
        defaultKey(defaultKey)
    }

    companion object {
        val defaultKey = DefaultActivityKey("default")
    }
}

@Parcelize
data class GenericActivityKey(val id: String) : NavigationKey

@NavigationDestination(GenericActivityKey::class)
class GenericActivity : TestActivity()

@Parcelize
data class ActivityWithFragmentsKey(val id: String) : NavigationKey

@NavigationDestination(ActivityWithFragmentsKey::class)
class ActivityWithFragments : TestActivity() {
    private val navigation by navigationHandle<ActivityWithFragmentsKey> {
        defaultKey(ActivityWithFragmentsKey("default"))
        container(primaryFragmentContainer) {
            it is ActivityChildFragmentKey || it is ActivityChildFragmentTwoKey
        }
    }
}

@Parcelize
data class ActivityChildFragmentKey(val id: String) : NavigationKey

@NavigationDestination(ActivityChildFragmentKey::class)
class ActivityChildFragment : TestFragment() {
    val navigation by navigationHandle<ActivityChildFragmentKey>() {
        container(primaryFragmentContainer) {
            it is Nothing
        }
    }
}

@Parcelize
data class ActivityChildFragmentTwoKey(val id: String) : NavigationKey

@NavigationDestination(ActivityChildFragmentTwoKey::class)
class ActivityChildFragmentTwo : TestFragment()

@Parcelize
data class GenericFragmentKey(val id: String) : NavigationKey

@NavigationDestination(GenericFragmentKey::class)
class GenericFragment : TestFragment()

class UnboundActivity : TestActivity()

class UnboundFragment : TestFragment()