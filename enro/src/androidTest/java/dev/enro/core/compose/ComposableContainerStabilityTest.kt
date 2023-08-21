package dev.enro.core.compose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.destination.compose.container.rememberNavigationContainerGroup
import dev.enro.core.container.*
import dev.enro.destination.compose.navigationHandle
import dev.enro.destination.compose.rememberNavigationContainer
import kotlinx.coroutines.isActive
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.*

class ComposableContainerStabilityTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @get:Rule
    val composeContentRule = createComposeRule()

    @Test
    fun givenSingleComposableDestination_whenActivityIsRecreated_thenStabilitySnapshotIsStable() {
        val scenario = ActivityScenario.launch(ComposeStabilityActivity::class.java)
        val activity = expectContext<ComposeStabilityActivity, ComposeStabilityRootKey>()

        val first = ComposeStabilityContentKey()
        activity.navigation.push(first)
        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == first }

        val firstSnapshot = composeContentRule.getSnapshotFor(first)
        scenario.recreate()
        val firstSnapshotRecreated = composeContentRule.getSnapshotFor(first)

        assertEquals(firstSnapshot, firstSnapshotRecreated)
    }

    @Test
    fun givenSingleComposableDestination_whenContainerIsSavedAndRestored_thenStabilitySnapshotIsStableExceptViewModel() {
        val scenario = ActivityScenario.launch(ComposeStabilityActivity::class.java)
        val activity = expectContext<ComposeStabilityActivity, ComposeStabilityRootKey>()

        val first = ComposeStabilityContentKey()
        activity.navigation.push(first)
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == first }
        refreshCompose()
        val firstSnapshot = composeContentRule.getSnapshotFor(first)

        val savedState = saveContainer(ComposeStabilityActivity.primaryContainer)
        activity.navigation.onContainer(ComposeStabilityActivity.primaryContainer) { setBackstack(emptyBackstack()) }
        refreshCompose()
        expectNoComposableContext<ComposeStabilityContentKey>()

        restoreContainer(ComposeStabilityActivity.primaryContainer, savedState)
        refreshCompose()

        val firstSnapshotRestored = composeContentRule.getSnapshotFor(first)

        assertEquals(firstSnapshot.withoutViewModel(), firstSnapshotRestored.withoutViewModel())
    }

    @Test
    fun givenNestedComposableDestinations_whenActivityIsRecreated_thenStabilitySnapshotIsStable() {
        val scenario = ActivityScenario.launch(ComposeStabilityActivity::class.java)
        val activity = expectContext<ComposeStabilityActivity, ComposeStabilityRootKey>()

        val first = ComposeStabilityContentKey()
        val second = ComposeStabilityContentKey()
        val third = ComposeStabilityContentKey()

        activity.navigation.push(first)
        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == first }
            .navigation
            .push(second)

        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == second }
            .navigation
            .push(third)

        val firstSnapshot = composeContentRule.getSnapshotFor(first)
        val secondSnapshot = composeContentRule.getSnapshotFor(second)
        val thirdSnapshot = composeContentRule.getSnapshotFor(third)

        scenario.recreate()
        val firstSnapshotRecreated = composeContentRule.getSnapshotFor(first)
        val secondSnapshotRecreated = composeContentRule.getSnapshotFor(second)
        val thirdSnapshotRecreated = composeContentRule.getSnapshotFor(third)

        assertEquals(firstSnapshot, firstSnapshotRecreated)
        assertEquals(secondSnapshot, secondSnapshotRecreated)
        assertEquals(thirdSnapshot, thirdSnapshotRecreated)
    }


    @Test
    fun givenNestedComposableDestinations_whenContainerIsSavedAndRestored_thenStabilitySnapshotIsStableExceptViewModel() {
        val scenario = ActivityScenario.launch(ComposeStabilityActivity::class.java)
        val activity = expectContext<ComposeStabilityActivity, ComposeStabilityRootKey>()

        val first = ComposeStabilityContentKey()
        val second = ComposeStabilityContentKey()
        val third = ComposeStabilityContentKey()

        activity.navigation.push(first)
        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == first }
            .navigation
            .push(second)

        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == second }
            .navigation
            .push(third)

        val firstSnapshot = composeContentRule.getSnapshotFor(first)
        val secondSnapshot = composeContentRule.getSnapshotFor(second)
        val thirdSnapshot = composeContentRule.getSnapshotFor(third)

        val savedState = saveContainer(ComposeStabilityActivity.primaryContainer)
        activity.navigation.onContainer(ComposeStabilityActivity.primaryContainer) { setBackstack(emptyBackstack()) }
        refreshCompose()
        expectNoComposableContext<ComposeStabilityContentKey>()

        restoreContainer(ComposeStabilityActivity.primaryContainer, savedState)
        refreshCompose()

        val firstSnapshotRecreated = composeContentRule.getSnapshotFor(first)
        val secondSnapshotRecreated = composeContentRule.getSnapshotFor(second)
        val thirdSnapshotRecreated = composeContentRule.getSnapshotFor(third)

        assertEquals(firstSnapshot.withoutViewModel(), firstSnapshotRecreated.withoutViewModel())
        assertEquals(secondSnapshot.withoutViewModel(), secondSnapshotRecreated.withoutViewModel())
        assertEquals(thirdSnapshot.withoutViewModel(), thirdSnapshotRecreated.withoutViewModel())
    }

    @Test
    fun givenContainerGroups_whenActiveContainerIsChanged_thenStabilitySnapshotIsCompletelyDifferent() {
        val scenario = ActivityScenario.launch(ComposeStabilityGroupsActivity::class.java)
        val activity = expectContext<ComposeStabilityGroupsActivity, ComposeStabilityRootKey>()

        val first = ComposeStabilityContentKey()
        val second = ComposeStabilityContentKey()
        val third = ComposeStabilityContentKey()

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setBackstack { it.push(first) } }
        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setBackstack { it.push(second) } }
        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setBackstack { it.push(third) } }

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        val firstSnapshot = composeContentRule.getSnapshotFor(first)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        refreshCompose()
        val secondSnapshot = composeContentRule.getSnapshotFor(second)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setActive() }
        refreshCompose()
        val thirdSnapshot = composeContentRule.getSnapshotFor(third)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        val firstSnapshotReselected = composeContentRule.getSnapshotFor(first)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        refreshCompose()
        val secondSnapshotReselected = composeContentRule.getSnapshotFor(second)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setActive() }
        refreshCompose()
        val thirdSnapshotReselected = composeContentRule.getSnapshotFor(third)

        assertTrue(firstSnapshot.isCompletelyNotEqualTo(secondSnapshot))
        assertTrue(firstSnapshot.isCompletelyNotEqualTo(thirdSnapshot))
        assertTrue(secondSnapshot.isCompletelyNotEqualTo(thirdSnapshot))

        assertEquals(firstSnapshot, firstSnapshotReselected)
        assertEquals(secondSnapshot, secondSnapshotReselected)
        assertEquals(thirdSnapshot, thirdSnapshotReselected)
    }

    @Test
    fun givenContainerGroups_whenActiveContainerIsChanged_andActivityIsRecreated_thenStabilitySnapshotIsCompletelyDifferent() {
        val scenario = ActivityScenario.launch(ComposeStabilityGroupsActivity::class.java)
        val activity = expectContext<ComposeStabilityGroupsActivity, ComposeStabilityRootKey>()

        val first = ComposeStabilityContentKey()
        val second = ComposeStabilityContentKey()
        val third = ComposeStabilityContentKey()

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setBackstack { it.push(first) } }
        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setBackstack { it.push(second) } }
        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setBackstack { it.push(third) } }

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        val firstSnapshot = composeContentRule.getSnapshotFor(first)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        refreshCompose()
        val secondSnapshot = composeContentRule.getSnapshotFor(second)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setActive() }
        refreshCompose()
        val thirdSnapshot = composeContentRule.getSnapshotFor(third)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        val firstSnapshotReselected = composeContentRule.getSnapshotFor(first)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        refreshCompose()
        val secondSnapshotReselected = composeContentRule.getSnapshotFor(second)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setActive() }
        refreshCompose()
        val thirdSnapshotReselected = composeContentRule.getSnapshotFor(third)

        scenario.recreate()
        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        val firstSnapshotRecreated = composeContentRule.getSnapshotFor(first)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        refreshCompose()
        val secondSnapshotRecreated = composeContentRule.getSnapshotFor(second)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setActive() }
        refreshCompose()
        val thirdSnapshotRecreated = composeContentRule.getSnapshotFor(third)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        val firstSnapshotReselectedRecreated = composeContentRule.getSnapshotFor(first)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        refreshCompose()
        val secondSnapshotReselectedRecreated = composeContentRule.getSnapshotFor(second)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.tertiaryContainer) { setActive() }
        refreshCompose()
        val thirdSnapshotReselectedRecreated = composeContentRule.getSnapshotFor(third)

        assertTrue(firstSnapshot.isCompletelyNotEqualTo(secondSnapshot))
        assertTrue(firstSnapshot.isCompletelyNotEqualTo(thirdSnapshot))
        assertTrue(secondSnapshot.isCompletelyNotEqualTo(thirdSnapshot))

        assertEquals(firstSnapshot, firstSnapshotReselected)
        assertEquals(secondSnapshot, secondSnapshotReselected)
        assertEquals(thirdSnapshot, thirdSnapshotReselected)

        assertTrue(firstSnapshotRecreated.isCompletelyNotEqualTo(secondSnapshotRecreated))
        assertTrue(firstSnapshotRecreated.isCompletelyNotEqualTo(thirdSnapshotRecreated))
        assertTrue(secondSnapshotRecreated.isCompletelyNotEqualTo(thirdSnapshotRecreated))

        assertEquals(firstSnapshot, firstSnapshotRecreated)
        assertEquals(secondSnapshot, secondSnapshotRecreated)
        assertEquals(thirdSnapshot, thirdSnapshotRecreated)

        assertEquals(firstSnapshotRecreated, firstSnapshotReselectedRecreated)
        assertEquals(secondSnapshotRecreated, secondSnapshotReselectedRecreated)
        assertEquals(thirdSnapshotRecreated, thirdSnapshotReselectedRecreated)
    }

    @Test
    fun givenContainerGroupsWithNestedContainers_whenActiveContainerIsChanged_thenStabilitySnapshotIsStableForNestedKeys() {
        val scenario = ActivityScenario.launch(ComposeStabilityGroupsActivity::class.java)
        val activity = expectContext<ComposeStabilityGroupsActivity, ComposeStabilityRootKey>()

        val first = ComposeStabilityContentKey()
        val firstNested = ComposeStabilityContentKey()
        val second = ComposeStabilityContentKey()
        val secondNested = ComposeStabilityContentKey()

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) {
            setBackstack { it.push(first) }
            setActive()
        }
        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == first }
            .navigation
            .push(firstNested)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) {
            setBackstack { it.push(second) }
            setActive()
        }
        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == second }
            .navigation
            .push(secondNested)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == first }
        val firstSnapshot = composeContentRule.getSnapshotFor(first)
        val firstSnapshotNested = composeContentRule.getSnapshotFor(firstNested)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        expectComposableContext<ComposeStabilityContentKey> { it.navigation.key == second }
        refreshCompose()
        val secondSnapshot = composeContentRule.getSnapshotFor(second)
        val secondSnapshotNested = composeContentRule.getSnapshotFor(secondNested)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.primaryContainer) { setActive() }
        refreshCompose()
        val firstSnapshotReselected = composeContentRule.getSnapshotFor(first)
        val firstSnapshotNestedReselected = composeContentRule.getSnapshotFor(firstNested)

        activity.navigation.onContainer(ComposeStabilityGroupsActivity.secondaryContainer) { setActive() }
        refreshCompose()
        val secondSnapshotReselected = composeContentRule.getSnapshotFor(second)
        val secondSnapshotNestedReselected = composeContentRule.getSnapshotFor(secondNested)

        assertTrue(firstSnapshot.isCompletelyNotEqualTo(secondSnapshot))

        assertEquals(firstSnapshot, firstSnapshotReselected)
        assertEquals(firstSnapshotNested, firstSnapshotNestedReselected)
        assertEquals(secondSnapshot, secondSnapshotReselected)
        assertEquals(secondSnapshotNested, secondSnapshotNestedReselected)
    }

    private fun saveContainer(containerKey: NavigationContainerKey): Bundle {
        var savedState: Bundle? = null
        expectActivity<ComposeStabilityActivity>()
            .navigation
            .onContainer(containerKey) {
                savedState = save()
            }

        return waitOnMain { savedState }
    }

    private fun restoreContainer(containerKey: NavigationContainerKey, savedState: Bundle) {
        var wasRestored = false
        expectActivity<ComposeStabilityActivity>()
            .navigation
            .onContainer(containerKey) {
                restore(savedState)
                wasRestored = true
            }

        return waitFor { wasRestored }
    }

    // It appears that when using a ComposeContentRule that the Compose content doesn't actually update
    // unless you fetch a semantics node. This is a problem for these tests, because we need Compose to update
    // to trigger various onDispose effects and other similar actions, so here we're just grabbing the root node
    // and fetching it, so that Compose updates and triggers everything that it should
    private fun refreshCompose() {
        composeContentRule.onRoot().fetchSemanticsNode()
    }
}

@Parcelize
object ComposeStabilityRootKey: NavigationKey.SupportsPresent

@NavigationDestination(ComposeStabilityRootKey::class)
class ComposeStabilityActivity : AppCompatActivity() {
    val navigation by navigationHandle { defaultKey(ComposeStabilityRootKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(primaryContainer)
            Column {
                Text("ComposeStabilityActivity")
                Box {
                     container.Render()
                }
            }
        }
    }

    companion object {
        val primaryContainer = NavigationContainerKey.FromName("SaveHierarchyActivity.primaryContainer")
    }
}

@Parcelize
object ComposeStabilityGroupsRootKey: NavigationKey.SupportsPresent

@NavigationDestination(ComposeStabilityGroupsRootKey::class)
class ComposeStabilityGroupsActivity : AppCompatActivity() {
    val navigation by navigationHandle { defaultKey(ComposeStabilityRootKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val containerGroup = rememberNavigationContainerGroup(
                rememberNavigationContainer(primaryContainer),
                rememberNavigationContainer(secondaryContainer),
                rememberNavigationContainer(tertiaryContainer),
            )
            Column {
                Text("ComposeStabilityGroupsActivity")
                Box {
                    containerGroup.activeContainer.Render()
                }
            }
        }
    }

    companion object {
        val primaryContainer = NavigationContainerKey.FromName("SaveHierarchyActivity.primaryContainer")
        val secondaryContainer = NavigationContainerKey.FromName("SaveHierarchyActivity.secondaryContainer")
        val tertiaryContainer = NavigationContainerKey.FromName("SaveHierarchyActivity.tertiaryContainer")
    }
}

data class ComposeStabilitySnapshot(
    val navigationId: String,
    val navigationKeyId: String,
    val navigationHashCode: String,
    val viewModelId: String,
    val viewModelHashCode: String,
    val viewModelSavedStateId: String,
    val viewModelStoreHashCode: String,
    val viewModelScopeActive: String,
    val rememberSaveableId: String,
) {
    // This function provides the stability snapshot without any instance state related to ViewModels or ViewModelStoreOwners,
    // which is to say that we'd expect that the saved state content of the ComposeStabilitySnapshot is the same, such
    // as the state saved to a ViewModel's SavedStateHandle, or the content of a rememberSaveable, but that we'd expect/allow
    // the ViewModel instances themselves to be different.
    fun withoutViewModel() = copy(
        navigationHashCode = "", // NavigationHandles are stored as ViewModels, so if we expect the ViewModel to change, we also expect the NavigationHandle to change too
        viewModelId = "",
        viewModelHashCode = "",
        viewModelStoreHashCode = "",
        viewModelScopeActive = "",
    )

    fun isCompletelyNotEqualTo(other: ComposeStabilitySnapshot) : Boolean {
        return navigationId != other.navigationId &&
            navigationKeyId != other.navigationKeyId &&
            navigationHashCode != other.navigationHashCode &&
            viewModelId != other.viewModelId &&
            viewModelHashCode != other.viewModelHashCode &&
            viewModelSavedStateId != other.viewModelSavedStateId &&
            viewModelStoreHashCode != other.viewModelStoreHashCode &&
            rememberSaveableId != other.rememberSaveableId
    }
}

@Parcelize
data class ComposeStabilityContentKey(
    val id: String = UUID.randomUUID().toString()
) : NavigationKey.SupportsPush {
    val childContainerKey get() = NavigationContainerKey.FromName(id)

    val testTag get() = "ComposeStabilityContent@$id"
}

class ComposeStabilityContentViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val id: String = UUID.randomUUID().toString()
    val saveStateHandleId = savedStateHandle.getStateFlow("savedStateId", UUID.randomUUID().toString())
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
@NavigationDestination(ComposeStabilityContentKey::class)
fun ComposeStabilityContentScreen() {
    val rawNavigationHandle = navigationHandle()
    val typedNavigationHandle = navigationHandle<ComposeStabilityContentKey>()
    val rememberSaveable = rememberSaveable { UUID.randomUUID().toString() }
    val viewModel = viewModel<ComposeStabilityContentViewModel>()
    val viewModelStore = LocalViewModelStoreOwner.current?.viewModelStore

    val stabilityContent = buildString {
        appendLine("navigationId: ${rawNavigationHandle.id}")
        appendLine("navigationKeyId: ${typedNavigationHandle.key.id}")
        appendLine("navigationHashCode: ${rawNavigationHandle.hashCode()}")
        appendLine("viewModelId: ${viewModel.id}")
        appendLine("viewModelHashCode: ${viewModel.hashCode()}")
        appendLine("viewModelSavedStateId: ${viewModel.saveStateHandleId.value}")
        appendLine("viewModelStoreHashCode: ${viewModelStore.hashCode()}")
        appendLine("viewModelStoreHashCode: ${viewModelStore.hashCode()}")
        appendLine("viewModelScopeActive: ${viewModel.viewModelScope.isActive}")
        appendLine("rememberSaveableId: $rememberSaveable")
    }

    val childContainer = rememberNavigationContainer(typedNavigationHandle.key.childContainerKey)
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(8.dp)
    ) {
        Text(
            text = stabilityContent,
            modifier = Modifier.semantics {
                testTag = typedNavigationHandle.key.testTag
            }
        )
        childContainer.Render()
    }
}

fun ComposeContentTestRule.getSnapshotFor(key: ComposeStabilityContentKey) : ComposeStabilitySnapshot {
    val text = onNodeWithTag(key.testTag)
        .fetchSemanticsNode()
        .config[SemanticsProperties.Text]
        .first()
        .text
    val content = text
        .split("\n")
        .filter { it.isNotBlank() }
        .associate {
            val split = it.split(":")
            split[0].trim() to split[1].trim()
        }
    return ComposeStabilitySnapshot(
        navigationId = content["navigationId"]!!,
        navigationKeyId = content["navigationKeyId"]!!,
        navigationHashCode = content["navigationHashCode"]!!,
        viewModelId = content["viewModelId"]!!,
        viewModelHashCode = content["viewModelHashCode"]!!,
        viewModelSavedStateId = content["viewModelSavedStateId"]!!,
        viewModelStoreHashCode = content["viewModelStoreHashCode"]!!,
        viewModelScopeActive = content["viewModelScopeActive"]!!,
        rememberSaveableId = content["rememberSaveableId"]!!,
    )
}
