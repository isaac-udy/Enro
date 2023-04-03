@file:Suppress("DEPRECATION")
package dev.enro.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.fragment.container.navigationContainer
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class NavigationContainerTests {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenActivityHasFragmentContainersThatAcceptTheSameKey_thenContainerThatIsActiveReceivesNavigationEvents() {
        ActivityScenario.launch(MultipleFragmentContainerActivity::class.java)
        val activity = expectActivity<MultipleFragmentContainerActivity>()

        activity.getNavigationHandle().forward(GenericFragmentKey("First"))
        val firstContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Second"))
        val secondContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Third"))
        val thirdContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == thirdContext.context }

        activity.onBackPressed()
        expectActivity<MultipleFragmentContainerActivity>()
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        expectActivity<MultipleFragmentContainerActivity>()
        waitFor { activity.primaryContainer.childContext?.contextReference == null }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }
        waitFor { !activity.primaryContainer.isActive }
        waitFor { !activity.secondaryContainer.isActive }

        activity.onBackPressed()
        expectNoActivity()
    }

    @Test
    fun whenActivityHasComposableContainersThatAcceptTheSameKey_thenContainerThatIsActiveReceivesNavigationEvents() {
        ActivityScenario.launch(MultipleComposableContainerActivity::class.java)
        val activity = expectActivity<MultipleComposableContainerActivity>()

        activity.getNavigationHandle().forward(GenericComposableKey("First"))
        val firstContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Second"))
        val secondContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Third"))
        val thirdContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == thirdContext.context }

        activity.onBackPressed()
        expectActivity<MultipleComposableContainerActivity>()
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        expectActivity<MultipleComposableContainerActivity>()
        waitFor { activity.primaryContainer.childContext?.contextReference == null }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }
        assertFalse(activity.primaryContainer.isActive)
        assertFalse(activity.secondaryContainer.isActive)

        activity.onBackPressed()
        expectNoActivity()
    }

    @Test
    fun whenActivityIsRecreated_andHasSingleFragmentNavigationContainer_thenFragmentNavigationContainerIsRestored() {
        val scenario = ActivityScenario.launch(SingleFragmentContainerActivity::class.java)
        var activity = expectActivity<SingleFragmentContainerActivity>()

        activity.getNavigationHandle().forward(GenericFragmentKey("First"))
        val firstContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }

        activity.getNavigationHandle().forward(GenericFragmentKey("Second"))
        val secondContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == secondContext.context }

        scenario.recreate()
        activity = expectActivity()
        val secondContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == secondContextRecreated.context }

        activity.onBackPressed()
        val firstContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContextRecreated.context }

        activity.onBackPressed()
        waitFor { activity.primaryContainer.childContext?.contextReference == null }

        activity.onBackPressed()
        expectNoActivity()
    }

    @Test
    fun whenActivityIsRecreated_andHasMultipleFragmentNavigationContainers_thenAllFragmentNavigationContainersAreRestored() {
        val scenario = ActivityScenario.launch(MultipleFragmentContainerActivity::class.java)
        var activity = expectActivity<MultipleFragmentContainerActivity>()

        activity.getNavigationHandle().forward(GenericFragmentKey("First"))
        val firstContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Second"))
        val secondContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Third"))
        val thirdContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == thirdContext.context }

        activity.secondaryContainer.setActive()
        scenario.recreate()
        activity = expectActivity()

        val secondContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContextRecreated.context }
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        waitFor { activity.secondaryContainer.childContext?.contextReference == null }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Fourth"))
        val fourthContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Fourth"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == fourthContext.context }
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        val thirdContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == thirdContextRecreated.context }

        activity.onBackPressed()
        val firstContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContextRecreated.context }

        activity.onBackPressed()
        waitFor { (activity.primaryContainer.childContext?.contextReference == null) }

        activity.onBackPressed()
        expectNoActivity()
    }

    @Test
    fun whenActivityIsRecreated_andHasSingleComposableNavigationContainer_thenComposableNavigationContainerIsRestored() {
        val scenario = ActivityScenario.launch(SingleComposableContainerActivity::class.java)
        var activity = expectActivity<SingleComposableContainerActivity>()

        activity.getNavigationHandle().forward(GenericComposableKey("First"))
        val firstContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }

        activity.getNavigationHandle().forward(GenericComposableKey("Second"))
        val secondContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == secondContext.context }

        scenario.recreate()
        activity = expectActivity()
        val secondContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == secondContextRecreated.context }

        activity.onBackPressed()
        val firstContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContextRecreated.context }

        activity.onBackPressed()
        waitFor { activity.primaryContainer.childContext?.contextReference == null }

        activity.onBackPressed()
        expectNoActivity()
    }

    @Test
    fun whenActivityIsRecreated_andHasMultipleComposableNavigationContainers_thenAllComposableNavigationContainersAreRestored() {
        val scenario = ActivityScenario.launch(MultipleComposableContainerActivity::class.java)
        var activity = expectActivity<MultipleComposableContainerActivity>()

        activity.getNavigationHandle().forward(GenericComposableKey("First"))
        val firstContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContext.context }

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Second"))
        val secondContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContext.context }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Third"))
        val thirdContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == thirdContext.context }

        activity.secondaryContainer.setActive()
        scenario.recreate()
        activity = expectActivity()

        val secondContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        waitFor { activity.secondaryContainer.childContext?.contextReference == secondContextRecreated.context }
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        waitFor { activity.secondaryContainer.childContext?.contextReference == null }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Fourth"))
        val fourthContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Fourth"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == fourthContext.context }
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        val thirdContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == thirdContextRecreated.context }

        activity.onBackPressed()
        val firstContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "First"
        }
        waitFor { activity.primaryContainer.childContext?.contextReference == firstContextRecreated.context }

        activity.onBackPressed()
        waitFor { (activity.primaryContainer.childContext?.contextReference == null) }

        activity.onBackPressed()
        expectNoActivity()
    }

    @Test
    fun whenMultipleFragmentsAreOpenedIntoContainersThatAcceptDifferentKeys_andAreLaterClosed_thenTheActiveContainerStateIsRememberedAndSetCorrectly() {
        ActivityScenario.launch(MultipleFragmentContainerActivityWithAccept::class.java)
        val activity = expectActivity<MultipleFragmentContainerActivityWithAccept>()

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("One"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "One" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Two"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Two" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Three"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Three" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Four"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Four" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Five"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Five" }

        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Five" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Four" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Three" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Two" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "One" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }
    }

    @Test
    fun whenMultipleFragmentsAreOpenedIntoContainersThatAcceptDifferentKeys_andAreClosedAfterRecreation_thenTheActiveContainerStateIsRememberedAndSetCorrectly() {
        val scenario = ActivityScenario.launch(MultipleFragmentContainerActivityWithAccept::class.java)
        var activity = expectActivity<MultipleFragmentContainerActivityWithAccept>()

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("One"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "One" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Two"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Two" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Three"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Three" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Four"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Four" }

        activity.getNavigationHandle()
            .forward(GenericFragmentKey("Five"))
        expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Five" }

        scenario.recreate()
        activity = expectActivity()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Five" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Four" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Three" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "Two" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectFragmentContext<GenericFragmentKey> { it.navigation.key.id == "One" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }
    }


    @Test
    fun whenMultipleComposablesAreOpenedIntoContainersThatAcceptDifferentKeys_andAreLaterClosed_thenTheActiveContainerStateIsRememberedAndSetCorrectly() {
        ActivityScenario.launch(MultipleComposableContainerActivityWithAccept::class.java)
        val activity = expectActivity<MultipleComposableContainerActivityWithAccept>()

        activity.getNavigationHandle()
            .forward(GenericComposableKey("One"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "One" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Two"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Two" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Three"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Three" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Four"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Four" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Five"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Five" }

        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Five" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Four" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Three" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Two" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "One" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }
    }

    @Test
    fun whenMultipleComposablesAreOpenedIntoContainersThatAcceptDifferentKeys_andAreClosedAfterRecreation_thenTheActiveContainerStateIsRememberedAndSetCorrectly() {
        val scenario = ActivityScenario.launch(MultipleComposableContainerActivityWithAccept::class.java)
        var activity = expectActivity<MultipleComposableContainerActivityWithAccept>()

        activity.getNavigationHandle()
            .forward(GenericComposableKey("One"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "One" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Two"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Two" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Three"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Three" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Four"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Four" }

        activity.getNavigationHandle()
            .forward(GenericComposableKey("Five"))
        expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Five" }

        scenario.recreate()
        activity = expectActivity()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Five" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Four" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Three" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "Two" }.context,
            activity.secondaryContainer.childContext?.contextReference
        )
        waitFor { activity.secondaryContainer.isActive }

        activity.onBackPressed()
        assertEquals(
            expectComposableContext<GenericComposableKey> { it.navigation.key.id == "One" }.context,
            activity.primaryContainer.childContext?.contextReference
        )
        waitFor { activity.primaryContainer.isActive }
    }

}

@Parcelize
object SingleFragmentContainerActivityKey: NavigationKey

@NavigationDestination(SingleFragmentContainerActivityKey::class)
class SingleFragmentContainerActivity : TestActivity() {
    private val navigation by navigationHandle<SingleFragmentContainerActivityKey> {
        defaultKey(SingleFragmentContainerActivityKey)
    }
    val primaryContainer by navigationContainer(primaryFragmentContainer)
}

@Parcelize
object MultipleFragmentContainerActivityKey: NavigationKey

@NavigationDestination(MultipleFragmentContainerActivityKey::class)
class MultipleFragmentContainerActivity : TestActivity() {
    private val navigation by navigationHandle<MultipleFragmentContainerActivityKey> {
        defaultKey(MultipleFragmentContainerActivityKey)
    }
    val primaryContainer by navigationContainer(primaryFragmentContainer)
    val secondaryContainer by navigationContainer(secondaryFragmentContainer)
}

@Parcelize
object MultipleFragmentContainerActivityWithAcceptKey: NavigationKey

@NavigationDestination(MultipleFragmentContainerActivityWithAcceptKey::class)
class MultipleFragmentContainerActivityWithAccept : TestActivity() {
    private val navigation by navigationHandle<MultipleFragmentContainerActivityWithAcceptKey> {
        defaultKey(MultipleFragmentContainerActivityWithAcceptKey)
    }

    private val primaryContainerKeys = listOf("One", "Three", "Five")
    val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is GenericFragmentKey && primaryContainerKeys.contains(it.id)
    }

    private val secondaryContainerKeys = listOf("Two", "Four", "Six")
    val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
        it is GenericFragmentKey && secondaryContainerKeys.contains(it.id)
    }
}

@Parcelize
object SingleComposableContainerActivityKey: NavigationKey

@NavigationDestination(SingleComposableContainerActivityKey::class)
class SingleComposableContainerActivity : ComponentActivity() {
    private val navigation by navigationHandle<SingleComposableContainerActivityKey> {
        defaultKey(SingleComposableContainerActivityKey)
    }
    lateinit var primaryContainer: ComposableNavigationContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            primaryContainer = rememberNavigationContainer()

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "SingleComposableContainerActivity", fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                Text(text = dev.enro.core.compose.navigationHandle().key.toString(), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                EnroContainer(
                    container = primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .background(Color(0x22FF0000))
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Parcelize
object MultipleComposableContainerActivityKey: NavigationKey

@NavigationDestination(MultipleComposableContainerActivityKey::class)
class MultipleComposableContainerActivity : ComponentActivity() {
    private val navigation by navigationHandle<MultipleComposableContainerActivityKey> {
        defaultKey(MultipleComposableContainerActivityKey)
    }
    lateinit var primaryContainer: ComposableNavigationContainer
    lateinit var secondaryContainer: ComposableNavigationContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            primaryContainer = rememberNavigationContainer()
            secondaryContainer = rememberNavigationContainer()

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "MultipleComposableContainerActivity", fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                Text(text = dev.enro.core.compose.navigationHandle().key.toString(), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                EnroContainer(
                    container = primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .weight(1f)
                        .background(Color(0x22FF0000))
                        .padding(horizontal = 20.dp)
                )
                EnroContainer(
                    container = secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .weight(1f)
                        .background(Color(0x220000FF))
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }
}


@Parcelize
object MultipleComposableContainerActivityWithAcceptKey: NavigationKey

@NavigationDestination(MultipleComposableContainerActivityWithAcceptKey::class)
class MultipleComposableContainerActivityWithAccept : ComponentActivity() {
    private val navigation by navigationHandle<MultipleComposableContainerActivityWithAcceptKey> {
        defaultKey(MultipleComposableContainerActivityWithAcceptKey)
    }
    private val primaryContainerKeys = listOf("One", "Three", "Five")
    lateinit var primaryContainer: ComposableNavigationContainer

    private val secondaryContainerKeys = listOf("Two", "Four", "Six")
    lateinit var secondaryContainer: ComposableNavigationContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            primaryContainer = rememberNavigationContainer {
                it is GenericComposableKey && primaryContainerKeys.contains(it.id)
            }
            secondaryContainer = rememberNavigationContainer {
                it is GenericComposableKey && secondaryContainerKeys.contains(it.id)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "MultipleComposableContainerActivity", fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                Text(text = dev.enro.core.compose.navigationHandle().key.toString(), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                EnroContainer(
                    container = primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .weight(1f)
                        .background(Color(0x22FF0000))
                        .padding(horizontal = 20.dp)
                )
                EnroContainer(
                    container = secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .weight(1f)
                        .background(Color(0x220000FF))
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }
}