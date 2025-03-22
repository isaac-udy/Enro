package dev.enro.tests.application.compose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationKey
import dev.enro.core.compose.container.rememberNavigationContainerGroup
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.push
import dev.enro.core.container.setBackstack
import dev.enro.core.onContainer
import dev.enro.core.push
import dev.enro.tests.application.compose.common.Stability
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize
import kotlin.uuid.Uuid

// TODO - this could have additional tests added to match those in ComposableDestinationContainerGroups.kt and/or ComposableContainerStabilityTests.kt
// generally, some tests that flick quickly between the containers and ensure that they are stable and are rendered correctly. Due to those other
// tests in the core enro suite, it's not a high priority to add these (as those will catch bugs), but it is easier to debug tests which are
// written against the test application (as compared to the core suite).
@Parcelize
object ComposeStability : NavigationKey.SupportsPush {

    internal val primaryContainer = NavigationContainerKey.FromName("primaryContainer")
    internal val secondaryContainer = NavigationContainerKey.FromName("secondaryContainer")
    internal val tertiaryContainer = NavigationContainerKey.FromName("tertiaryContainer")

    @Parcelize
    internal data class Content(
        val id: String = Uuid.random().toString()
    ) : NavigationKey.SupportsPush {
        val childContainerKey get() = NavigationContainerKey.FromName(id)
        val testTag get() = "ComposeStabilityContent@$id"
    }

}

@NavigationDestination(ComposeStability::class)
class ComposeStabilityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navigation = navigationHandle<ComposeStability>()
            val containerGroup = rememberNavigationContainerGroup(
                rememberNavigationContainer(
                    key= ComposeStability.primaryContainer,
                    emptyBehavior = EmptyBehavior.AllowEmpty,
                    root = ComposeStability.Content(),
                ),
                rememberNavigationContainer(
                    key = ComposeStability.secondaryContainer,
                    emptyBehavior = EmptyBehavior.AllowEmpty
                ),
                rememberNavigationContainer(
                    key = ComposeStability.tertiaryContainer,
                    emptyBehavior = EmptyBehavior.AllowEmpty
                ),
            )
            TitledColumn("Compose Stability") {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = { navigation.onContainer(ComposeStability.primaryContainer) { setActive() } }
                    ) { Text("One") }

                    Button(
                        onClick = { navigation.onContainer(ComposeStability.secondaryContainer) { setActive() } }
                    ) { Text("Two") }

                    Button(
                        onClick = { navigation.onContainer(ComposeStability.tertiaryContainer) { setActive() } }
                    ) { Text("Three") }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = { containerGroup.activeContainer.setBackstack { it.push(ComposeStability.Content()) } }
                    ) { Text("Push Root") }

                    Button(
                        onClick = {
                            val childContext = containerGroup.activeContainer.childContext ?: return@Button
                            childContext.navigationHandle.push(ComposeStability.Content())
                        }
                    ) { Text("Push Child") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Active: ${containerGroup.activeContainer.key.name}")
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    containerGroup.activeContainer.Render()
                }
            }
        }
    }
}

class ComposeStabilityContentViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val id: String = Uuid.random().toString()
    val saveStateHandleId = savedStateHandle.getStateFlow("savedStateId", Uuid.random().toString())
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
@NavigationDestination(ComposeStability.Content::class)
fun ComposeStabilityContentScreen() {
    val typedNavigationHandle = navigationHandle<ComposeStability.Content>()
    val childContainer = rememberNavigationContainer(
        key = typedNavigationHandle.key.childContainerKey,
        emptyBehavior = EmptyBehavior.AllowEmpty,
    )
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(8.dp)
    ) {
        Stability(
            additionalStabilityContent = listOf(
                "navigationKeyId: ${typedNavigationHandle.key.id}",
            ),
            modifier = Modifier.semantics {
                testTag = typedNavigationHandle.key.testTag
            }
        )
        childContainer.Render()
    }
}