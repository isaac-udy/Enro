// Snippets for docs/ghpages/docs/troubleshooting.md
package dev.enro.docs.troubleshooting

import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.test.createTestNavigationHandle
import dev.enro.test.runEnroTest
import dev.enro.ui.destinations.syntheticDestination
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.test.Test

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data object BadSynthetic : NavigationKey

@Serializable
data object MyKey : NavigationKey

@Serializable
class MyCustomType

private suspend fun someAsyncWork() {}

// region docs:troubleshooting/handle-scope
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()  // ✅ matches the annotation
    // ...
}

@Composable
fun SomeRandomComposable() {
    val navigation = navigationHandle<NavigationKey>() // ❌ not inside a destination
}
// endregion

// region docs:troubleshooting/async-synthetic
@NavigationDestination(BadSynthetic::class)
val badSynthetic = syntheticDestination<BadSynthetic> {
    context.lifecycleScope.launch {
        someAsyncWork()
        complete()      // ← block has long since returned; throws "already finished"
    }
}
// endregion

// region docs:troubleshooting/serializers-module
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        serializersModule(SerializersModule {
            contextual(MyCustomType.serializer())
        })
    }
)
// endregion

// region docs:troubleshooting/test-controller
@Test
fun example() = runEnroTest {
    val handle = createTestNavigationHandle(MyKey)
    // ...
}
// endregion
