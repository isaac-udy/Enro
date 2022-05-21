package dev.enro.core.destinations

import dev.enro.TestDialogFragment
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.core.result.registerForNavigationResult
import kotlinx.parcelize.Parcelize
import java.util.*

object FragmentDestinations {
    @Parcelize
    data class Root(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent

    @Parcelize
    data class Presentable(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent.WithResult<TestResult>

    @Parcelize
    data class PresentableDialog(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent.WithResult<TestResult>

    @Parcelize
    data class PushesToPrimary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>, TestDestination.IntoPrimaryContainer

    @Parcelize
    data class PushesToSecondary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>, TestDestination.IntoSecondaryContainer

    @Parcelize
    data class PushesToChildAsPrimary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>, TestDestination.IntoPrimaryChildContainer

    @Parcelize
    data class PushesToChildAsSecondary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>, TestDestination.IntoSecondaryChildContainer

    abstract class Fragment(
        primaryContainerAccepts: (NavigationKey) -> Boolean,
        secondaryContainerAccepts: (NavigationKey) -> Boolean,
    ) : TestFragment() {
        private val primaryContainer by navigationContainer(primaryFragmentContainer, accept = primaryContainerAccepts)
        private val secondaryContainer by navigationContainer(secondaryFragmentContainer, accept = secondaryContainerAccepts)
        val navigation by navigationHandle<NavigationKey>()
        val resultChannel by registerForNavigationResult<TestResult> {
            navigation.registerTestResult(it)
        }
    }

    abstract class DialogFragment(
        primaryContainerAccepts: (NavigationKey) -> Boolean,
        secondaryContainerAccepts: (NavigationKey) -> Boolean,
    ) : TestDialogFragment() {
        private val primaryContainer by navigationContainer(primaryFragmentContainer, accept = primaryContainerAccepts)
        private val secondaryContainer by navigationContainer(secondaryFragmentContainer, accept = secondaryContainerAccepts)
        val navigation by navigationHandle<NavigationKey>()
        val resultChannel by registerForNavigationResult<TestResult> {
            navigation.registerTestResult(it)
        }
    }
}

@NavigationDestination(FragmentDestinations.Root::class)
class FragmentDestinationRoot : FragmentDestinations.Fragment(
    primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
    secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
)

@NavigationDestination(FragmentDestinations.Presentable::class)
class FragmentDestinationPresentable : FragmentDestinations.Fragment(
    primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
    secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
)

@NavigationDestination(FragmentDestinations.PresentableDialog::class)
class FragmentDestinationPresentableDialog: FragmentDestinations.DialogFragment(
    primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
    secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
)

@NavigationDestination(FragmentDestinations.PushesToPrimary::class)
class FragmentDestinationPushesToPrimary : FragmentDestinations.Fragment(
    primaryContainerAccepts = { it is TestDestination.IntoPrimaryChildContainer },
    secondaryContainerAccepts = { it is TestDestination.IntoSecondaryChildContainer }
)

@NavigationDestination(FragmentDestinations.PushesToSecondary::class)
class FragmentDestinationPushesToSecondary : FragmentDestinations.Fragment(
    primaryContainerAccepts = { it is TestDestination.IntoPrimaryChildContainer },
    secondaryContainerAccepts = { it is TestDestination.IntoSecondaryChildContainer }
)

@NavigationDestination(FragmentDestinations.PushesToChildAsPrimary::class)
class FragmentDestinationPushesToChildAsPrimary : FragmentDestinations.Fragment(
    primaryContainerAccepts = { false },
    secondaryContainerAccepts = { false }
)

@NavigationDestination(FragmentDestinations.PushesToChildAsSecondary::class)
class FragmentDestinationPushesToChildAsSecondary : FragmentDestinations.Fragment(
    primaryContainerAccepts = { false },
    secondaryContainerAccepts = { false }
)