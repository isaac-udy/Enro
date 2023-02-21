package dev.enro.core.compose

import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import dev.enro.core.*
import dev.enro.core.container.present
import dev.enro.core.container.push
import dev.enro.core.destinations.*
import dev.enro.expectComposableContext
import dev.enro.setBackstackOnMain
import dev.enro.waitFor
import junit.framework.TestCase.assertEquals
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.util.*

class ComposableDestinationPresent {
    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()

        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
    }

    @Test
    @Ignore("This test appears to be somewhat flaky due to the window randomly losing focus in a way that can't be reproduced on an actual device")
    fun givenComposableDestination_whenExecutingPresent_andPresent_andPush_thenCorrectDestinationIsOpened_andBackButtonWorksCorrectly() {
        val root = launchComposableRoot()

        val presented = ComposableDestinations.Presentable()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>(presented)
            .assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>(presented)
            .navigation
            .push(ComposableDestinations.Pushable())

        expectComposableContext<ComposableDestinations.Pushable>()
        Espresso.pressBack()
        expectComposableContext<ComposableDestinations.Presentable> { it.navigation.key == presented }

        root.navigation.push(ComposableDestinations.Pushable())
        expectComposableContext<ComposableDestinations.Pushable>().navigation.close()
        expectComposableContext<ComposableDestinations.Presentable>()
    }

    @OptIn(AdvancedEnroApi::class)
    @Test
    @Ignore("This test appears to be somewhat flaky due to the window randomly losing focus in a way that can't be reproduced on an actual device")    fun givenComposableDestination_whenManuallyPresentingAndPushingBackstack_thenBacstackIsUpdatedCorrectly() {
        val root = launchComposableRoot()

        val rootContainer = root.navigationContext.directParentContainer()!!
        rootContainer.setBackstackOnMain {
            it.present(ComposableDestinations.Presentable("1"))
                .present(ComposableDestinations.Presentable("2"))
        }

        expectComposableContext<ComposableDestinations.Presentable> { it.navigation.key.id == "2" }
        rootContainer.setBackstackOnMain {
            it.push(ComposableDestinations.Pushable("3"))
        }
        expectComposableContext<ComposableDestinations.Pushable> { it.navigation.key.id == "3" }
            .let {
                waitFor { it.navigation.lifecycle.currentState == Lifecycle.State.RESUMED }
            }
        Espresso.pressBack()
        expectComposableContext<ComposableDestinations.Presentable> { it.navigation.key.id == "2" }
        rootContainer.setBackstackOnMain {
            it.push(ComposableDestinations.Pushable("4"))
        }
        expectComposableContext<ComposableDestinations.Pushable> { it.navigation.key.id == "4" }
            .navigation
            .close()

        expectComposableContext<ComposableDestinations.Presentable> { it.navigation.key.id == "2" }
        Espresso.pressBack()
        expectComposableContext<ComposableDestinations.Presentable> { it.navigation.key.id == "1" }
        Espresso.pressBack()
        expectComposableContext<ComposableDestinations.Root>()
    }

    @Parcelize
    data class ParcelableForTest(
        val parcelableId: String
    ) : Parcelable

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsGenericComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        val expectedKey = ComposableDestinations.Generic(ParcelableForTest(UUID.randomUUID().toString()))

        val context = root.assertPresentsTo<ComposableDestination, ComposableDestinations.Generic<ParcelableForTest>>(expectedKey)
        assertEquals(expectedKey, context.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()

        root.assertPresentsTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsActivityDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()

        root.assertPresentsTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }
}