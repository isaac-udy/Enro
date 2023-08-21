package dev.enro.core.destinations

import android.os.Parcelable
import dev.enro.TestActivity
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.destination.fragment.container.navigationContainer
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

    // This type is not actually used in any tests at present, but just exists to prove
    // that generic navigation destinations will correctly generate code
    @Parcelize
    data class Generic<Type: Parcelable>(
        val item: Type
    ) : NavigationKey.SupportsPush

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

@NavigationDestination(ActivityDestinations.Generic::class)
class ActivityDestinationsGenericActivity : ActivityDestinations.Activity()