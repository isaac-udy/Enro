package dev.enro.core.destinations

import dev.enro.TestActivity
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.core.result.registerForNavigationResult
import kotlinx.parcelize.Parcelize
import java.util.*

object ActivityDestinations {
    @Parcelize
    data class Root(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent

    @Parcelize
    data class Presentable(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent.WithResult<TestResult>

    abstract class Activity : TestActivity() {
        private val navigation by navigationHandle<NavigationKey>()
        private val primaryContainer by navigationContainer(primaryFragmentContainer) {
            it is TestDestination.IntoPrimaryContainer
        }
        private val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
            it is TestDestination.IntoSecondaryContainer
        }
        val resultChannel by registerForNavigationResult<TestResult> {
            navigation.registerTestResult(it)
        }
    }
}

@NavigationDestination(ActivityDestinations.Root::class)
class ActivityDestinationsRootActivity : ActivityDestinations.Activity()

@NavigationDestination(ActivityDestinations.Presentable::class)
class ActivityDestinationsPresentableActivity : ActivityDestinations.Activity()