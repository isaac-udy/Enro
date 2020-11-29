package nav.enro
import android.R
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle

@Parcelize
data class DefaultActivityKey(val id: String) : NavigationKey
val defaultKey = DefaultActivityKey("default")
class DefaultActivity : TestActivity() {
    private val navigation by navigationHandle<DefaultActivityKey> {
        defaultKey(defaultKey)
    }
}

@Parcelize
data class GenericActivityKey(val id: String) : NavigationKey
class GenericActivity : TestActivity()

@Parcelize
data class ActivityWithFragmentsKey(val id: String) : NavigationKey
class ActivityWithFragments : TestActivity() {
    private val navigation by navigationHandle<ActivityWithFragmentsKey> {
        defaultKey(ActivityWithFragmentsKey("default"))
        container(R.id.content) {
            it is ActivityChildFragmentKey || it is ActivityChildFragmentTwoKey
        }
    }
}

@Parcelize
data class ActivityChildFragmentKey(val id: String) : NavigationKey
class ActivityChildFragment : TestFragment()

@Parcelize
data class ActivityChildFragmentTwoKey(val id: String) : NavigationKey
class ActivityChildFragmentTwo : TestFragment()

@Parcelize
data class GenericFragmentKey(val id: String) : NavigationKey
class GenericFragment : TestFragment()