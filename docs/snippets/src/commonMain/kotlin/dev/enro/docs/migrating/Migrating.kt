// Snippets for docs/ghpages/docs/migrating-from-v2.md
package dev.enro.docs.migrating

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import dev.enro.plugin.NavigationPlugin
import dev.enro.test.assertOpened
import dev.enro.test.createTestNavigationHandle
import dev.enro.test.runEnroTest
import dev.enro.ui.decorators.navigationDestinationDecorator
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.dialog
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.test.Test

class MyPlugin : NavigationPlugin()

@Serializable
data object Settings : NavigationKey

private val myProviderVal = navigationDestination<Settings> { }

private val MyDeepLinkPathBinding = NavigationPathBinding.createPathBinding(
    pattern = "/settings",
    constructor = { Settings },
)

private val myKotlinxSerializersModule = EmptySerializersModule()

private val otherFeatureModule = createNavigationModule { }

// region docs:migrating/keys
@Serializable data class ShowProfile(val userId: String) : NavigationKey
@Serializable data class SelectDate(val min: LocalDate?) : NavigationKey.WithResult<LocalDate>
// endregion

// region docs:migrating/dialog-metadata
@NavigationDestination(SelectDate::class)
val selectDateDestination = navigationDestination<SelectDate>(
    metadata = { dialog() }
) {
    // ...
}
// endregion

private val module = // docs:hide
    // region docs:migrating/module
    createNavigationModule {
        plugin(MyPlugin())
        interceptor { /* NavigationInterceptorBuilder */ }
        decorator { navigationDestinationDecorator { /* wrap every destination */ } }
        destination(myProviderVal)              // for navigationDestination val { } providers
        path(MyDeepLinkPathBinding)
        serializersModule(myKotlinxSerializersModule)
        module(otherFeatureModule)              // compose modules from other libraries
    }
    // endregion

// region docs:migrating/view-model
class ProfileViewModel : ViewModel() {
    val navigation by navigationHandle<ShowProfile>()
}

@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val viewModel = viewModel { createEnroViewModel { ProfileViewModel() } }
    // ...
}
// endregion

// region docs:migrating/run-enro-test
@Test fun example() = runEnroTest {
    val handle = createTestNavigationHandle(ShowProfile("user-123"))
    handle.open(SelectDate(min = null))
    handle.assertOpened<SelectDate>()
}
// endregion

// region docs:migrating/worked-example
@Serializable
data class GreetUser(val name: String) : NavigationKey.WithResult<String>

@NavigationDestination(GreetUser::class)
val greetUserDestination = navigationDestination<GreetUser>(
    metadata = { dialog() }   // <-- presentation moves here
) {
    Column {
        Text("Hello, ${navigation.key.name}!")
        Button(onClick = { navigation.complete("greeted") }) {
            Text("Done")
        }
    }
}
// endregion
