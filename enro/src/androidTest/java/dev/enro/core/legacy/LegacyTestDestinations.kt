@file:Suppress("DEPRECATION")
package dev.enro.core.legacy
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dev.enro.TestActivity
import dev.enro.TestComposable
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.accept
import dev.enro.core.container.acceptNone
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActivityWithFragmentsKey(val id: String) : NavigationKey

@NavigationDestination(ActivityWithFragmentsKey::class)
class ActivityWithFragments : TestActivity() {
    private val primary by navigationContainer(
        containerId = primaryFragmentContainer,
        filter = accept {
            key<ActivityChildFragmentKey>()
            key<ActivityChildFragmentTwoKey>()
        }
    )
    val navigation by navigationHandle<ActivityWithFragmentsKey> {
        defaultKey(ActivityWithFragmentsKey("default"))
    }
}

@Parcelize
data class ActivityChildFragmentKey(val id: String) : NavigationKey

@NavigationDestination(ActivityChildFragmentKey::class)
class ActivityChildFragment : TestFragment() {
    private val primary by navigationContainer(
        containerId = TestActivity.primaryFragmentContainer,
        filter = acceptNone()
    )
    val navigation by navigationHandle<ActivityChildFragmentKey>()
}

@Parcelize data class ActivityWithComposablesKey(
    val id: String,
    val primaryContainerAccepts: List<Class<out NavigationKey>>,
    val secondaryContainerAccepts: List<Class<out NavigationKey>>
) : NavigationKey

@NavigationDestination(ActivityWithComposablesKey::class)
class ActivityWithComposables : AppCompatActivity() {

    val navigation by navigationHandle<ActivityWithComposablesKey> {
        defaultKey(
            ActivityWithComposablesKey(
                id = "default",
                primaryContainerAccepts = listOf(NavigationKey::class.java),
                secondaryContainerAccepts = emptyList()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestComposable(
                name = "ActivityWithComposablesKey(id = ${navigation.key.id})",
                primaryContainerAccepts = { key ->
                    navigation.key.primaryContainerAccepts.any {
                        it.isAssignableFrom(key::class.java)
                    }
                }
            )
        }
    }
}

@Parcelize
data class ActivityChildFragmentTwoKey(val id: String) : NavigationKey

@NavigationDestination(ActivityChildFragmentTwoKey::class)
class ActivityChildFragmentTwo : TestFragment()
