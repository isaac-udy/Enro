package dev.enro
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import dev.enro.annotations.ExperimentalComposableDestination
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationContainer
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
    val navigation by navigationHandle<ActivityWithFragmentsKey> {
        defaultKey(ActivityWithFragmentsKey("default"))
    }
    val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is ActivityChildFragmentKey || it is ActivityChildFragmentTwoKey
    }
}

@Parcelize
data class ActivityChildFragmentKey(val id: String) : NavigationKey

@NavigationDestination(ActivityChildFragmentKey::class)
class ActivityChildFragment : TestFragment() {
    val navigation by navigationHandle<ActivityChildFragmentKey>()
    val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is Nothing
    }
}

@Parcelize data class ActivityWithComposablesKey(
    val id: String,
    val primaryContainerAccepts: List<Class<out NavigationKey>>,
    val secondaryContainerAccepts: List<Class<out NavigationKey>>
) : NavigationKey

@NavigationDestination(ActivityWithComposablesKey::class)
class ActivityWithComposables : AppCompatActivity() {

    val navigation by navigationHandle<ActivityWithComposablesKey> {
        defaultKey(ActivityWithComposablesKey(
            id = "default",
            primaryContainerAccepts = listOf(NavigationKey::class.java),
            secondaryContainerAccepts = emptyList()
        ))
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

@Parcelize
data class GenericFragmentKey(val id: String) : NavigationKey

@NavigationDestination(GenericFragmentKey::class)
class GenericFragment : TestFragment()

@Parcelize
data class GenericComposableKey(val id: String) : NavigationKey

@Composable
@ExperimentalComposableDestination
@NavigationDestination(GenericComposableKey::class)
fun GenericComposableDestination() = TestComposable(name = "GenericComposableDestination")

class UnboundActivity : TestActivity()

class UnboundFragment : TestFragment()