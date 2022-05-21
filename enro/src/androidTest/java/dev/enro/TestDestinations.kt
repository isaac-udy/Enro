package dev.enro

import androidx.compose.runtime.Composable
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
data class GenericFragmentKey(val id: String) : NavigationKey

@NavigationDestination(GenericFragmentKey::class)
class GenericFragment : TestFragment()

@Parcelize
data class GenericComposableKey(val id: String) : NavigationKey

@Composable
@NavigationDestination(GenericComposableKey::class)
fun GenericComposableDestination() = TestComposable(name = "GenericComposableDestination")

class UnboundActivity : TestActivity()

class UnboundFragment : TestFragment()