package dev.enro

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
data class DefaultActivityKey(val id: String) : Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(DefaultActivityKey::class)
class DefaultActivity : TestActivity() {
    private val navigation by navigationHandle<DefaultActivityKey> {
        defaultKey(defaultKey)
    }

    companion object {
        val defaultKey = DefaultActivityKey("default")
    }
}

@Serializable
data class GenericActivityKey(val id: String) : NavigationKey.SupportsPresent

@NavigationDestination(GenericActivityKey::class)
class GenericActivity : TestActivity()

@Parcelize
data class GenericFragmentKey(val id: String) : Parcelable, NavigationKey.SupportsPush, NavigationKey.SupportsPresent

@NavigationDestination(GenericFragmentKey::class)
class GenericFragment : TestFragment()

@Parcelize
data class GenericComposableKey(val id: String) : Parcelable, NavigationKey.SupportsPush

@Composable
@NavigationDestination(GenericComposableKey::class)
fun GenericComposableDestination() = TestComposable(name = "GenericComposableDestination")

class UnboundActivity : TestActivity()

class UnboundFragment : TestFragment()

class EmptyActivity : FragmentActivity()