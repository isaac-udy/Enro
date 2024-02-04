package dev.enro.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.R
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptKey
import dev.enro.core.container.doNotAccept
import dev.enro.core.container.key
import dev.enro.core.directParentContainer
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.parentContainer
import dev.enro.core.present
import dev.enro.expectActivity
import dev.enro.expectActivityHostForAnyInstruction
import dev.enro.expectComposableContext
import dev.enro.expectFragmentContext
import dev.enro.expectFragmentHostForComposable
import dev.enro.expectFragmentHostForPresentableFragment
import dev.enro.navigationContext
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.parcelize.Parcelize
import org.junit.Test
import java.util.UUID

@OptIn(AdvancedEnroApi::class)
class PresentationTests {

    // Fragments
    @Test
    fun givenActivityWithContainer_whenContainerSupportsKey_andKeyPresentsFragment_thenFragmentIsPresentedOnTopOfExistingContent() {
        val expectedKey = FragmentKey()
        ActivityScenario.launch(ActivityWithFragmentContainer::class.java)
        expectActivity<ActivityWithFragmentContainer>()
            .getNavigationHandle()
            .present(expectedKey)

        val context = expectFragmentContext<FragmentKey>()
        assertEquals(expectedKey, context.navigation.key)
        assertEquals(ActivityWithFragmentContainer.containerId, context.context.id)
    }

    @Test
    fun givenActivityWithContainer_whenContainerSupportsKey_andKeyPresentsDialogFragment_thenDialogFragmentIsShown() {
        val expectedKey = DialogFragmentKey()
        ActivityScenario.launch(ActivityWithFragmentContainer::class.java)
        val activity = expectActivity<ActivityWithFragmentContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val fragment = expectFragmentContext<DialogFragmentKey>()
        assertEquals(expectedKey, fragment.navigation.key)

        // Not in a layout, is presented as dialog, so the fragment id should be 0
        assertEquals(0, fragment.context.id)
        fragment.context as DialogFragment
        assertTrue(fragment.context.showsDialog)
        assertNotNull(fragment.context.dialog)
        assertEquals(activity.supportFragmentManager, fragment.context.parentFragmentManager)
        assertEquals(activity, fragment.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(activity, fragment.navigationContext.parentContainer()?.context?.contextReference)
        assertEquals(activity, fragment.navigationContext.parentContext?.contextReference)
    }

    @Test
    fun givenActivityWithContainer_whenContainerDoesNotSupportKey_andKeyPresentsFragment_thenFragmentIsPresentedOnTopOfExistingContent() {
        val expectedKey = NotSupportedFragmentKey()
        ActivityScenario.launch(ActivityWithFragmentContainer::class.java)
        val activity = expectActivity<ActivityWithFragmentContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val fragment = expectFragmentContext<NotSupportedFragmentKey>()
        assertEquals(expectedKey, fragment.navigation.key)
        assertEquals(ActivityWithFragmentContainer.containerId, fragment.context.id)
        assertEquals(activity.supportFragmentManager, fragment.context.parentFragmentManager)

        assertEquals(activity, fragment.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(activity, fragment.navigationContext.parentContext?.contextReference)
        assertEquals(activity, fragment.navigationContext.parentContainer()?.context?.contextReference)
    }

    @Test
    fun givenActivityWithNoContainer_whenKeyPresentsFragment_thenFragmentIsOpenedAsDialogFragment() {
        val expectedKey = FragmentKey()
        ActivityScenario.launch(ActivityWithNoContainer::class.java)
        val activity = expectActivity<ActivityWithNoContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val host = expectFragmentHostForPresentableFragment()
        val fragment = expectFragmentContext<FragmentKey>()
        assertEquals(expectedKey, fragment.navigation.key)
        assertEquals(R.id.enro_internal_single_fragment_frame_layout, fragment.context.id)
        assertEquals(host.childFragmentManager, fragment.context.parentFragmentManager)

        assertEquals(host, fragment.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(host, fragment.navigationContext.parentContext?.contextReference)
        assertEquals(activity, fragment.navigationContext.parentContainer()?.context?.contextReference)
    }

    @Test
    fun givenActivityWithNoContainerAndNoFragmentManager_whenKeyPresentsFragment_thenFragmentIsOpenedAsActivity() {
        val expectedKey = FragmentKey()
        ActivityScenario.launch(ActivityWithNoContainerAndNoFragments::class.java)
        val activity = expectActivity<ActivityWithNoContainerAndNoFragments>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val host = expectActivityHostForAnyInstruction()
        val fragment = expectFragmentContext<FragmentKey>()
        assertEquals(expectedKey, fragment.navigation.key)
        assertEquals(R.id.enro_internal_single_fragment_frame_layout, fragment.context.id)
        assertEquals(host.supportFragmentManager, fragment.context.parentFragmentManager)

        assertEquals(host, fragment.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(host, fragment.navigationContext.parentContext?.contextReference)
        assertEquals(host, fragment.navigationContext.parentContainer()?.context?.contextReference)
    }


    // Composables inside Fragments
    @Test
    fun givenActivityWithFragmentContainer_whenContainerSupportsKey_andKeyPresentsComposable_thenComposableIsPresentedOnTopOfExistingContent() {
        val expectedKey = ComposeKey()
        ActivityScenario.launch(ActivityWithFragmentContainer::class.java)
        val activity = expectActivity<ActivityWithFragmentContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val fragment = expectFragmentHostForComposable()
        assertEquals(ActivityWithFragmentContainer.containerId, fragment.id)

        val composable = expectComposableContext<ComposeKey>()
        assertEquals(expectedKey, composable.navigation.key)
        assertEquals(fragment, composable.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(fragment, composable.navigationContext.parentContext?.contextReference)
        assertEquals(activity, composable.navigationContext.parentContainer()?.context?.contextReference)
    }

    @Test
    fun givenActivityWithFragmentContainer_whenContainerDoesNotSupportKey_andKeyPresentsComposable_thenComposableIsOpenedAsFragmentOnTopOfExistingContent() {
        val expectedKey = NotSupportedComposeKey()
        ActivityScenario.launch(ActivityWithFragmentContainer::class.java)
        val activity = expectActivity<ActivityWithFragmentContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val fragment = expectFragmentHostForComposable()
        assertEquals(activity, fragment.navigationContext.parentContext?.contextReference)
        assertEquals(activity.supportFragmentManager, fragment.parentFragmentManager)
        assertEquals(ActivityWithFragmentContainer.containerId, fragment.id)

        val composable = expectComposableContext<NotSupportedComposeKey>()
        assertEquals(expectedKey, composable.navigation.key)
        assertEquals(fragment, composable.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(fragment, composable.navigationContext.parentContext?.contextReference)
        assertEquals(activity, composable.navigationContext.parentContainer()?.context?.contextReference)
    }

    @Test
    fun givenActivityWithNoContainer_whenKeyPresentsComposable_thenComposableIsOpenedAsDialogFragment() {
        val expectedKey = ComposeKey()
        ActivityScenario.launch(ActivityWithNoContainer::class.java)
        val activity = expectActivity<ActivityWithNoContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val dialogFragment = expectFragmentHostForPresentableFragment()
        assertEquals(0, dialogFragment.id)

        val fragment = expectFragmentHostForComposable()
        assertEquals(dialogFragment, fragment.navigationContext.parentContext?.contextReference)
        assertEquals(dialogFragment.childFragmentManager, fragment.parentFragmentManager)
        assertEquals(R.id.enro_internal_single_fragment_frame_layout, fragment.id)

        val composable = expectComposableContext<ComposeKey>()
        assertEquals(expectedKey, composable.navigation.key)
        assertEquals(fragment, composable.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(fragment, composable.navigationContext.parentContext?.contextReference)
        assertEquals(activity, composable.navigationContext.parentContainer()?.context?.contextReference)
    }

    // Composables
    @Test
    fun givenActivityWithComposeContainer_whenContainerSupportsKey_andKeyPresentsComposable_thenComposableIsPresentedOnTopOfExistingContent() {
        val expectedKey = ComposeKey()
        ActivityScenario.launch(ActivityWithComposeContainer::class.java)
        val activity = expectActivity<ActivityWithComposeContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val composable = expectComposableContext<ComposeKey>()
        assertEquals(expectedKey, composable.navigation.key)
        assertEquals(activity, composable.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(activity, composable.navigationContext.parentContext?.contextReference)
        assertEquals(activity, composable.navigationContext.parentContainer()?.context?.contextReference)
    }

    @Test
    fun givenActivityWithComposeContainer_whenContainerDoesNotSupportKey_andKeyPresentsComposable_thenComposableIsPresentedOnTopOfExistingContent() {
        val expectedKey = NotSupportedComposeKey()
        ActivityScenario.launch(ActivityWithComposeContainer::class.java)
        val activity = expectActivity<ActivityWithComposeContainer>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val composable = expectComposableContext<NotSupportedComposeKey>()
        assertEquals(expectedKey, composable.navigation.key)
        assertEquals(activity, composable.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(activity, composable.navigationContext.parentContext?.contextReference)
        assertEquals(activity, composable.navigationContext.parentContainer()?.context?.contextReference)
    }

    @Test
    fun givenActivityWithNoContainerAndNoFragmentManager_whenKeyPresentsComposable_thenComposableIsOpenedAsActivity() {
        val expectedKey = ComposeKey()
        ActivityScenario.launch(ActivityWithNoContainerAndNoFragments::class.java)
        val activity = expectActivity<ActivityWithNoContainerAndNoFragments>()
        activity.getNavigationHandle()
            .present(expectedKey)

        val activityHost = expectActivityHostForAnyInstruction()
        val fragmentHost = expectFragmentHostForComposable()
        assertEquals(activityHost, fragmentHost.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(activityHost, fragmentHost.navigationContext.parentContext?.contextReference)
        assertEquals(activityHost, fragmentHost.navigationContext.parentContainer()?.context?.contextReference)

        val compose = expectComposableContext<ComposeKey>()

        assertEquals(expectedKey, compose.navigation.key)
        assertEquals(R.id.enro_internal_single_fragment_frame_layout, fragmentHost.id)
        assertEquals(activityHost.supportFragmentManager, fragmentHost.parentFragmentManager)

        assertEquals(fragmentHost, compose.navigationContext.directParentContainer()?.context?.contextReference)
        assertEquals(fragmentHost, compose.navigationContext.parentContext?.contextReference)
        assertEquals(activityHost, fragmentHost.navigationContext.parentContainer()?.context?.contextReference)
    }


    // Keys
    @Parcelize
    data class FragmentKey(val id: String = UUID.randomUUID().toString()) : NavigationKey.SupportsPresent

    @Parcelize
    data class DialogFragmentKey(val id: String = UUID.randomUUID().toString()) : NavigationKey.SupportsPresent

    @Parcelize
    data class NotSupportedFragmentKey(val id: String = UUID.randomUUID().toString()) : NavigationKey.SupportsPresent

    @Parcelize
    data class NotSupportedLegacyFragmentKey(val id: String = UUID.randomUUID().toString()) : NavigationKey

    @Parcelize
    data class ComposeKey(val id: String = UUID.randomUUID().toString()) : NavigationKey.SupportsPresent

    @Parcelize
    data class NotSupportedComposeKey(val id: String = UUID.randomUUID().toString()) : NavigationKey.SupportsPresent

    @Parcelize
    data class NotSupportedLegacyComposeKey(val id: String = UUID.randomUUID().toString()) : NavigationKey

}

class ActivityWithFragmentContainer : FragmentActivity() {

    val container by navigationContainer(
        containerId = containerId,
        filter = acceptKey {
            it !is PresentationTests.NotSupportedFragmentKey && it !is PresentationTests.NotSupportedComposeKey
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            FrameLayout(this).apply {
                id = containerId
            }
        )
    }

    companion object {
        val containerId = View.generateViewId()
    }
}

class ActivityWithComposeContainer : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(
                emptyBehavior = EmptyBehavior.AllowEmpty,
                filter = doNotAccept { key<PresentationTests.NotSupportedComposeKey>() }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                container.Render()
            }
        }
    }
}

class ActivityWithNoContainer : FragmentActivity()

class ActivityWithNoContainerAndNoFragments : ComponentActivity()

@NavigationDestination(PresentationTests.FragmentKey::class)
class PresentationTestFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return View(requireContext()).apply {
            setBackgroundColor(0xFF00FF00.toInt())
        }
    }
}

@NavigationDestination(PresentationTests.DialogFragmentKey::class)
class PresentationTestDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return View(requireContext()).apply {
            setBackgroundColor(0xFF00FF00.toInt())
        }
    }
}

@NavigationDestination(PresentationTests.NotSupportedFragmentKey::class)
class PresentationTestNotSupportedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return View(requireContext()).apply {
            setBackgroundColor(0xFFFF0000.toInt())
        }
    }
}

@Composable
@NavigationDestination(PresentationTests.ComposeKey::class)
fun PresentationTestsComposableDestination() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Green))
}

@Composable
@NavigationDestination(PresentationTests.NotSupportedComposeKey::class)
fun PresentationTestsNotSupportedComposableDestination() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Red))
}