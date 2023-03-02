package dev.enro.core.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.container.*
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.fragment.container.navigationContainer
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import java.util.*

class FragmentContainerSaveStateTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenSavingContainerStateToBundle_thenHierarchyIsRestoredCorrectly_andViewModelsAreClearedCorrectly() {
        val scenario = ActivityScenario.launch(SaveHierarchyActivity::class.java)
        val activity = expectContext<SaveHierarchyActivity, SavedHierarchyRootKey>()

        val instruction = NavigationInstruction.Push(SaveHierarchyKey())
        val instruction2 = NavigationInstruction.Push(SaveHierarchyKey())
        activity.navigation.onContainer(SaveHierarchyActivity.primaryContainerKey) {
            setBackstack { backstackOf(instruction) }
        }
        expectFragmentContext<SaveHierarchyKey>() {
            it.navigation.instruction.instructionId == instruction.instructionId
        }.apply {
            navigation.onContainer(TestFragment.primaryFragmentContainerKey) {
                setBackstack { backstackOf(instruction2) }
            }
            navigation.onContainer(TestFragment.secondaryFragmentContainerKey) {
                setBackstack { it.push(dev.enro.core.compose.SaveHierarchyKey()) }
            }
        }

        Thread.sleep(3000)

        var savedState: List<Pair<AnyOpenInstruction, Fragment.SavedState>> = emptyList()
        activity.navigation.onContainer(SaveHierarchyActivity.primaryContainerKey) {
            this as FragmentNavigationContainer
            savedState = save()
            setBackstack { emptyBackstack() }
        }

        Thread.sleep(3000)
        activity.navigation.onContainer(SaveHierarchyActivity.primaryContainerKey) {
            this as FragmentNavigationContainer
            restore(savedState)
            setBackstack { backstackOf(instruction) }
        }

        Thread.sleep(5000)
    }
}

@Parcelize
object SavedHierarchyRootKey: NavigationKey.SupportsPresent


@NavigationDestination(SavedHierarchyRootKey::class)
class SaveHierarchyActivity : TestActivity() {
    val navigation by navigationHandle { defaultKey(SavedHierarchyRootKey) }
    val primaryContainer by navigationContainer(primaryFragmentContainer)

    companion object {
        val primaryContainerKey = NavigationContainerKey.FromId(primaryFragmentContainer)
    }
}

@Parcelize
data class SaveHierarchyKey(
    val id: String = UUID.randomUUID().toString()
) : NavigationKey.SupportsPush

class SavedStateHierarchyViewModel : ViewModel() {
    val id: String = UUID.randomUUID().toString()
}

@NavigationDestination(SaveHierarchyKey::class)
class SaveHierarchyFragment() : Fragment() {
    val navigation by navigationHandle<SaveHierarchyKey>()
    val viewModel by viewModels<SavedStateHierarchyViewModel>()

    val primaryContainer by navigationContainer(TestFragment.primaryFragmentContainer)
    val secondaryContainer by navigationContainer(TestFragment.secondaryFragmentContainer)
    var savedState = UUID.randomUUID().toString()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        savedState = savedInstanceState?.getString("savedState", savedState) ?: savedState

        return LinearLayout(requireContext().applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFFFFFFFF.toInt())

            addView(TextView(context).apply {
                text = "SaveHierarchyFragment"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 32.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(TextView(context).apply {
                text = "Navigation ${navigation.id}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(TextView(context).apply {
                text = "ViewModel ${viewModel.id}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(TextView(context).apply {
                text = "SavedState $savedState"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(FrameLayout(context).apply {
                id = TestFragment.primaryFragmentContainer
                setPadding(50)
                setBackgroundColor(0x22FF0000)
            })

            addView(FrameLayout(context).apply {
                id = TestFragment.secondaryFragmentContainer
                setPadding(50)
                setBackgroundColor(0x220000FF)
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("savedState", savedState)
    }
}

