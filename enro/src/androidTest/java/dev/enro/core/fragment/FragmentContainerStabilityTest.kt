package dev.enro.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.container.*
import dev.enro.core.fragment.container.navigationContainer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.*

class FragmentContainerStabilityTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenSingleFragmentDestination_whenActivityIsRecreated_thenStabilitySnapshotIsStable() {
        val scenario = ActivityScenario.launch(FragmentStabilityActivity::class.java)
        val activity = expectContext<FragmentStabilityActivity, FragmentStabilityRootKey>()

        val first = FragmentStabilityContentKey()
        activity.navigation.push(first)
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == first }

        val firstSnapshot = getSnapshotFor(first)
        scenario.recreate()
        val firstSnapshotRecreated = getSnapshotFor(first)

        assertEquals(firstSnapshot, firstSnapshotRecreated)
    }

    @Test
    fun givenSingleFragmentDestination_whenContainerIsSavedAndRestored_thenStabilitySnapshotIsStableExceptViewModel() {
        val scenario = ActivityScenario.launch(FragmentStabilityActivity::class.java)
        val activity = expectContext<FragmentStabilityActivity, FragmentStabilityRootKey>()

        val first = FragmentStabilityContentKey()
        activity.navigation.push(first)
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == first }
        val firstSnapshot = getSnapshotFor(first)

        val savedState = saveContainer(FragmentStabilityActivity.primaryContainer)
        activity.navigation.onContainer(FragmentStabilityActivity.primaryContainer) { setBackstack(emptyBackstack()) }
        expectNoComposableContext<FragmentStabilityContentKey>()

        restoreContainer(FragmentStabilityActivity.primaryContainer, savedState)

        val firstSnapshotRestored = getSnapshotFor(first)

        assertEquals(firstSnapshot.withoutViewModel(), firstSnapshotRestored.withoutViewModel())
    }

    @Test
    fun givenNestedFragmentDestinations_whenActivityIsRecreated_thenStabilitySnapshotIsStable() {
        val scenario = ActivityScenario.launch(FragmentStabilityActivity::class.java)
        val activity = expectContext<FragmentStabilityActivity, FragmentStabilityRootKey>()

        val first = FragmentStabilityContentKey()
        val second = FragmentStabilityContentKey()
        val third = FragmentStabilityContentKey()

        activity.navigation.push(first)
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == first }
            .navigation
            .push(second)

        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == second }
            .navigation
            .push(third)

        val firstSnapshot = getSnapshotFor(first)
        val secondSnapshot = getSnapshotFor(second)
        val thirdSnapshot = getSnapshotFor(third)

        scenario.recreate()
        val firstSnapshotRecreated = getSnapshotFor(first)
        val secondSnapshotRecreated = getSnapshotFor(second)
        val thirdSnapshotRecreated = getSnapshotFor(third)

        assertEquals(firstSnapshot, firstSnapshotRecreated)
        assertEquals(secondSnapshot, secondSnapshotRecreated)
        assertEquals(thirdSnapshot, thirdSnapshotRecreated)
    }


    @Test
    fun givenNestedFragmentDestinations_whenContainerIsSavedAndRestored_thenStabilitySnapshotIsStableExceptViewModel() {
        val scenario = ActivityScenario.launch(FragmentStabilityActivity::class.java)
        val activity = expectContext<FragmentStabilityActivity, FragmentStabilityRootKey>()

        val first = FragmentStabilityContentKey()
        val second = FragmentStabilityContentKey()
        val third = FragmentStabilityContentKey()

        activity.navigation.push(first)
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == first }
            .navigation
            .push(second)

        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == second }
            .navigation
            .push(third)

        val firstSnapshot = getSnapshotFor(first)
        val secondSnapshot = getSnapshotFor(second)
        val thirdSnapshot = getSnapshotFor(third)

        val savedState = saveContainer(FragmentStabilityActivity.primaryContainer)
        activity.navigation.onContainer(FragmentStabilityActivity.primaryContainer) { setBackstack(emptyBackstack()) }
        expectNoComposableContext<FragmentStabilityContentKey>()

        restoreContainer(FragmentStabilityActivity.primaryContainer, savedState)

        val firstSnapshotRecreated = getSnapshotFor(first)
        val secondSnapshotRecreated = getSnapshotFor(second)
        val thirdSnapshotRecreated = getSnapshotFor(third)

        assertEquals(firstSnapshot.withoutViewModel(), firstSnapshotRecreated.withoutViewModel())
        assertEquals(secondSnapshot.withoutViewModel(), secondSnapshotRecreated.withoutViewModel())
        assertEquals(thirdSnapshot.withoutViewModel(), thirdSnapshotRecreated.withoutViewModel())
    }

    @Test
    fun givenContainerGroups_whenActiveContainerIsChanged_thenStabilitySnapshotIsCompletelyDifferent() {
        val scenario = ActivityScenario.launch(FragmentStabilityGroupsActivity::class.java)
        val activity = expectContext<FragmentStabilityGroupsActivity, FragmentStabilityRootKey>()

        val first = FragmentStabilityContentKey()
        val second = FragmentStabilityContentKey()
        val third = FragmentStabilityContentKey()

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setBackstack { it.push(first) } }
        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setBackstack { it.push(second) } }
        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setBackstack { it.push(third) } }

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        val firstSnapshot = getSnapshotFor(first)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        val secondSnapshot = getSnapshotFor(second)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setActive() }
        val thirdSnapshot = getSnapshotFor(third)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        val firstSnapshotReselected = getSnapshotFor(first)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        val secondSnapshotReselected = getSnapshotFor(second)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setActive() }
        val thirdSnapshotReselected = getSnapshotFor(third)

        assertTrue(firstSnapshot.isCompletelyNotEqualTo(secondSnapshot))
        assertTrue(firstSnapshot.isCompletelyNotEqualTo(thirdSnapshot))
        assertTrue(secondSnapshot.isCompletelyNotEqualTo(thirdSnapshot))

        assertEquals(firstSnapshot, firstSnapshotReselected)
        assertEquals(secondSnapshot, secondSnapshotReselected)
        assertEquals(thirdSnapshot, thirdSnapshotReselected)
    }

    @Test
    fun givenContainerGroups_whenActiveContainerIsChanged_andActivityIsRecreated_thenStabilitySnapshotIsCompletelyDifferent() {
        val scenario = ActivityScenario.launch(FragmentStabilityGroupsActivity::class.java)
        val activity = expectContext<FragmentStabilityGroupsActivity, FragmentStabilityRootKey>()

        val first = FragmentStabilityContentKey()
        val second = FragmentStabilityContentKey()
        val third = FragmentStabilityContentKey()

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setBackstack { it.push(first) } }
        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setBackstack { it.push(second) } }
        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setBackstack { it.push(third) } }

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        val firstSnapshot = getSnapshotFor(first)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        val secondSnapshot = getSnapshotFor(second)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setActive() }
        val thirdSnapshot = getSnapshotFor(third)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        val firstSnapshotReselected = getSnapshotFor(first)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        val secondSnapshotReselected = getSnapshotFor(second)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setActive() }
        val thirdSnapshotReselected = getSnapshotFor(third)

        scenario.recreate()
        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        val firstSnapshotRecreated = getSnapshotFor(first)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        val secondSnapshotRecreated = getSnapshotFor(second)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setActive() }
        val thirdSnapshotRecreated = getSnapshotFor(third)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        val firstSnapshotReselectedRecreated = getSnapshotFor(first)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        val secondSnapshotReselectedRecreated = getSnapshotFor(second)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.tertiaryContainer) { setActive() }
        val thirdSnapshotReselectedRecreated = getSnapshotFor(third)

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
        val scenario = ActivityScenario.launch(FragmentStabilityGroupsActivity::class.java)
        val activity = expectContext<FragmentStabilityGroupsActivity, FragmentStabilityRootKey>()

        val first = FragmentStabilityContentKey()
        val firstNested = FragmentStabilityContentKey()
        val second = FragmentStabilityContentKey()
        val secondNested = FragmentStabilityContentKey()

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) {
            setBackstack { it.push(first) }
            setActive()
        }
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == first }
            .navigation
            .push(firstNested)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) {
            setBackstack { it.push(second) }
            setActive()
        }
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == second }
            .navigation
            .push(secondNested)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == first }
        val firstSnapshot = getSnapshotFor(first)
        val firstSnapshotNested = getSnapshotFor(firstNested)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        expectFragmentContext<FragmentStabilityContentKey> { it.navigation.key == second }
        val secondSnapshot = getSnapshotFor(second)
        val secondSnapshotNested = getSnapshotFor(secondNested)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.primaryContainer) { setActive() }
        val firstSnapshotReselected = getSnapshotFor(first)
        val firstSnapshotNestedReselected = getSnapshotFor(firstNested)

        activity.navigation.onContainer(FragmentStabilityGroupsActivity.secondaryContainer) { setActive() }
        val secondSnapshotReselected = getSnapshotFor(second)
        val secondSnapshotNestedReselected = getSnapshotFor(secondNested)

        assertTrue(firstSnapshot.isCompletelyNotEqualTo(secondSnapshot))

        assertEquals(firstSnapshot, firstSnapshotReselected)
        assertEquals(firstSnapshotNested, firstSnapshotNestedReselected)
        assertEquals(secondSnapshot, secondSnapshotReselected)
        assertEquals(secondSnapshotNested, secondSnapshotNestedReselected)
    }

    private fun saveContainer(containerKey: NavigationContainerKey): Bundle {
        var savedState: Bundle? = null
        expectActivity<FragmentStabilityActivity>()
            .navigation
            .onContainer(containerKey) {
                savedState = save()
            }

        return waitOnMain { savedState }
    }

    private fun restoreContainer(containerKey: NavigationContainerKey, savedState: Bundle) {
        var wasRestored = false
        expectActivity<FragmentStabilityActivity>()
            .navigation
            .onContainer(containerKey) {
                restore(savedState)
                wasRestored = true
            }

        return waitFor { wasRestored }
    }
}

@Parcelize
object FragmentStabilityRootKey: NavigationKey.SupportsPresent

@NavigationDestination(FragmentStabilityRootKey::class)
class FragmentStabilityActivity : AppCompatActivity() {
    val navigation by navigationHandle { defaultKey(FragmentStabilityRootKey) }
    val primaryContainer by navigationContainer(primaryContainerId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@FragmentStabilityActivity).apply {
                text = "FragmentStabilityActivity"
            })
            addView(FrameLayout(this@FragmentStabilityActivity).apply {
                id = primaryContainerId
            })
        })
    }

    companion object {
        val primaryContainerId = View.generateViewId()
        val primaryContainer = NavigationContainerKey.FromId(primaryContainerId)
    }
}

@Parcelize
object FragmentStabilityGroupsRootKey: NavigationKey.SupportsPresent

@NavigationDestination(FragmentStabilityGroupsRootKey::class)
class FragmentStabilityGroupsActivity : AppCompatActivity() {
    val navigation by navigationHandle { defaultKey(FragmentStabilityRootKey) }
    val primaryContainer by navigationContainer(primaryContainerId)
    val secondaryContainer by navigationContainer(secondaryContainerId)
    val tertiaryContainer by navigationContainer(tertiaryContainerId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@FragmentStabilityGroupsActivity).apply {
                text = "FragmentStabilityGroupsActivity"
            })
            addView(FrameLayout(this@FragmentStabilityGroupsActivity).apply {
                addView(FrameLayout(this@FragmentStabilityGroupsActivity).apply {
                    id = primaryContainerId
                })
                addView(FrameLayout(this@FragmentStabilityGroupsActivity).apply {
                    id = secondaryContainerId
                })
                addView(FrameLayout(this@FragmentStabilityGroupsActivity).apply {
                    id = tertiaryContainerId
                })
            })
        })

        containerManager.activeContainerFlow
            .onEach {
                primaryContainer.isVisible = primaryContainer.isActive
                secondaryContainer.isVisible = secondaryContainer.isActive
                tertiaryContainer.isVisible = tertiaryContainer.isActive
            }
            .launchIn(lifecycleScope)
    }

    companion object {
        val primaryContainerId = View.generateViewId()
        val primaryContainer = NavigationContainerKey.FromId(primaryContainerId)
        val secondaryContainerId = View.generateViewId()
        val secondaryContainer = NavigationContainerKey.FromId(secondaryContainerId)
        val tertiaryContainerId = View.generateViewId()
        val tertiaryContainer = NavigationContainerKey.FromId(tertiaryContainerId)
    }
}

data class FragmentStabilitySnapshot(
    val navigationId: String,
    val navigationKeyId: String,
    val navigationHashCode: String,
    val viewModelId: String,
    val viewModelHashCode: String,
    val viewModelSavedStateId: String,
    val viewModelStoreHashCode: String,
    val savedInstanceStateId: String,
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
    )

    fun isCompletelyNotEqualTo(other: FragmentStabilitySnapshot) : Boolean {
        return navigationId != other.navigationId &&
            navigationKeyId != other.navigationKeyId &&
            navigationHashCode != other.navigationHashCode &&
            viewModelId != other.viewModelId &&
            viewModelHashCode != other.viewModelHashCode &&
            viewModelSavedStateId != other.viewModelSavedStateId &&
            viewModelStoreHashCode != other.viewModelStoreHashCode &&
            savedInstanceStateId != other.savedInstanceStateId
    }
}

@Parcelize
data class FragmentStabilityContentKey(
    val id: String = UUID.randomUUID().toString()
) : NavigationKey.SupportsPush

class FragmentStabilityContentViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val id: String = UUID.randomUUID().toString()
    val saveStateHandleId = savedStateHandle.getStateFlow("savedStateId", UUID.randomUUID().toString())
}

@NavigationDestination(FragmentStabilityContentKey::class)
class FragmentStabilityFragment : Fragment() {

    private val typedNavigationHandle by navigationHandle<FragmentStabilityContentKey>()
    private val viewModel by viewModels<FragmentStabilityContentViewModel>()
    private val container by navigationContainer(nestedContainerId)
    private lateinit var savedInstanceStateId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        savedInstanceStateId = savedInstanceState?.getString("savedInstanceStateId") ?: UUID.randomUUID().toString()
        val rawNavigationHandle = getNavigationHandle()

        val stabilityContent = buildString {
            appendLine("navigationId: ${rawNavigationHandle.id}")
            appendLine("navigationKeyId: ${typedNavigationHandle.key.id}")
            appendLine("navigationHashCode: ${rawNavigationHandle.hashCode()}")
            appendLine("viewModelId: ${viewModel.id}")
            appendLine("viewModelHashCode: ${viewModel.hashCode()}")
            appendLine("viewModelSavedStateId: ${viewModel.saveStateHandleId.value}")
            appendLine("viewModelStoreHashCode: ${viewModelStore.hashCode()}")
            appendLine("savedInstanceStateId: $savedInstanceStateId")
        }
        return LinearLayout(requireContext()).apply {
            setPadding(16, 16, 16, 16)
            addView(TextView(requireContext()).apply {
                id = stabilityContentId
                text = stabilityContent
            })
            addView(FrameLayout(requireContext()).apply {
                id = nestedContainerId
                setPadding(16, 16, 16, 16)
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("savedInstanceStateId", savedInstanceStateId)
    }

    companion object {
        val stabilityContentId = View.generateViewId()
        val nestedContainerId = View.generateViewId()
    }
}

private fun getSnapshotFor(key: FragmentStabilityContentKey) : FragmentStabilitySnapshot {
    val fragment = expectFragment<FragmentStabilityFragment> { it.getNavigationHandle().key == key }
    val text = fragment
        .requireView()
        .findViewById<TextView>(FragmentStabilityFragment.stabilityContentId)
        .text

    val content = text
        .split("\n")
        .filter { it.isNotBlank() }
        .associate {
            val split = it.split(":")
            split[0].trim() to split[1].trim()
        }
    return FragmentStabilitySnapshot(
        navigationId = content["navigationId"]!!,
        navigationKeyId = content["navigationKeyId"]!!,
        navigationHashCode = content["navigationHashCode"]!!,
        viewModelId = content["viewModelId"]!!,
        viewModelHashCode = content["viewModelHashCode"]!!,
        viewModelSavedStateId = content["viewModelSavedStateId"]!!,
        viewModelStoreHashCode = content["viewModelStoreHashCode"]!!,
        savedInstanceStateId = content["savedInstanceStateId"]!!,
    )
}
