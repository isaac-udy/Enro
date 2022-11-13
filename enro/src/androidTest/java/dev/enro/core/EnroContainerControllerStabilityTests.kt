@file:Suppress("DEPRECATION")

package dev.enro.core

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ActivityScenario
import dev.enro.annotations.NavigationDestination
import dev.enro.compose.EnroContainer
import dev.enro.compose.rememberEnroContainerController
import dev.enro.core.container.EmptyBehavior
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import java.util.*

class EnroContainerControllerStabilityTests {

    @get:Rule
    val composeContentRule = createComposeRule()

    @Test
    fun whenActivityIsRecreated_thenStabilitySnapshotIsStable() {
        val scenario = ActivityScenario.launch(ComposableTestActivity::class.java)
        val snapshot = getSnapshot()
        scenario.recreate()
        val secondSnapshot = getSnapshot()
        assertEquals(snapshot, secondSnapshot)
    }

    @Test
    fun whenSelectedControllerChanges_thenStabilitySnapshotIsCompletelyDifferent() {
        val scenario = ActivityScenario.launch(ComposableTestActivity::class.java)
        val snapshot = getSnapshot()
        scenario.onActivity {
            it.selectedIndex.value = 1
        }
        val secondSnapshot = getSnapshot()
        assertSnapshotsAreCompletelyDifferent(snapshot, secondSnapshot)
    }

    @Test
    fun whenSelectedControllerChanges_andThenChangesBackToOriginalController_thenStabilitySnapshotIsStable() {
        val scenario = ActivityScenario.launch(ComposableTestActivity::class.java)

        val snapshot = getSnapshot()
        scenario.onActivity {
            it.selectedIndex.value = 1
        }
        val secondSnapshot = getSnapshot()
        scenario.onActivity {
            it.selectedIndex.value = 0
        }
        val thirdSnapshot = getSnapshot()

        assertEquals(snapshot, thirdSnapshot)
        assertSnapshotsAreCompletelyDifferent(snapshot, secondSnapshot)
    }

    private fun getTextFromNode(testTag: String): String {
        return composeContentRule.onNodeWithTag(testTag)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .first()
            .text
    }

    private fun getSnapshot(): EnroStabilitySnapshot = EnroStabilitySnapshot(
        viewModelHashCode = getTextFromNode("viewModelHashCode"),
        viewModelStoreHashCode = getTextFromNode("viewModelStoreHashCode"),
        navigationId = getTextFromNode("navigationId"),
        keyId = getTextFromNode("keyId"),
        rememberSaveableItem = getTextFromNode("rememberSaveableItem"),
    )
}

class ComposableTestActivity : AppCompatActivity() {
    internal val selectedIndex = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screens = listOf(
            EnroStabilityKey(UUID.randomUUID().toString()),
            EnroStabilityKey(UUID.randomUUID().toString()),
            EnroStabilityKey(UUID.randomUUID().toString()),
        )

        setContent {
            val controllers = screens.map { key ->
                val instruction = NavigationInstruction.Forward(key)
                rememberEnroContainerController(
                    initialBackstack = listOf(instruction),
                    accept = { false },
                    emptyBehavior = EmptyBehavior.CloseParent
                )
            }
            EnroContainer(
                container = controllers[selectedIndex.value],
            )
        }
    }
}

@Parcelize
class EnroStabilityKey(
    val id: String
) : NavigationKey

class EnroStabilityViewModel : ViewModel()

@Composable
@NavigationDestination(EnroStabilityKey::class)
fun EnroStabilityScreen() {
    val navigation = navigationHandle<EnroStabilityKey>()
    val viewModelHashCode = viewModel<EnroStabilityViewModel>().hashCode().toString()
    val viewModelStoreHashCode = LocalViewModelStoreOwner.current?.viewModelStore.hashCode().toString()

    val navigationId = navigation.id
    val keyId = navigation.key.id

    val rememberSaveableItem = rememberSaveable { UUID.randomUUID().toString() }

    Column {
        Text(
            text = viewModelHashCode,
            modifier = Modifier.semantics {
                testTag = "viewModelHashCode"
            }
        )
        Text(
            text = viewModelStoreHashCode,
            modifier = Modifier.semantics {
                testTag = "viewModelStoreHashCode"
            }
        )
        Text(
            text = navigationId,
            modifier = Modifier.semantics {
                testTag = "navigationId"
            }
        )
        Text(
            text = keyId,
            modifier = Modifier.semantics {
                testTag = "keyId"
            }
        )
        Text(
            text = rememberSaveableItem,
            modifier = Modifier.semantics {
                testTag = "rememberSaveableItem"
            }
        )
    }
}

data class EnroStabilitySnapshot(
    val viewModelHashCode: String,
    val viewModelStoreHashCode: String,
    val navigationId: String,
    val keyId: String,
    val rememberSaveableItem: String,
)

fun assertSnapshotsAreCompletelyDifferent(left: EnroStabilitySnapshot, right: EnroStabilitySnapshot) {
    assertNotEquals(left.viewModelHashCode, right.viewModelHashCode)
    assertNotEquals(left.viewModelStoreHashCode, right.viewModelStoreHashCode)
    assertNotEquals(left.navigationId, right.navigationId)
    assertNotEquals(left.keyId, right.keyId)
    assertNotEquals(left.rememberSaveableItem, right.rememberSaveableItem)
}