package nav.enro.core
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DefaultActivityKey(val id: String) : NavigationKey
val defaultKey = DefaultActivityKey("default")
class DefaultActivity : TestActivity()

@Parcelize
data class GenericActivityKey(val id: String) : NavigationKey
class GenericActivity : TestActivity()

@Parcelize
data class ActivityWithFragmentsKey(val id: String) : NavigationKey
class ActivityWithFragments : TestActivity()

@Parcelize
data class ActivityChildFragmentKey(val id: String) : NavigationKey
class ActivityChildFragment : TestFragment()

@Parcelize
data class ActivityChildFragmentTwoKey(val id: String) : NavigationKey
class ActivityChildFragmentTwo : TestFragment()

@Parcelize
data class GenericFragmentKey(val id: String) : NavigationKey
class GenericFragment : TestFragment()