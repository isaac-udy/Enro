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
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.rememberEnroContainerController
import dev.enro.expectFragment
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.parcelize.Parcelize
import org.junit.Test

class NavigationContainerTests {

    @Test
    fun whenActivityHasFragmentContainersThatAcceptTheSameKey_thenContainerThatIsActiveReceivesNavigationEvents() {
        ActivityScenario.launch(MultipleFragmentContainerActivity::class.java)
        val activity = expectActivity<MultipleFragmentContainerActivity>()

        activity.getNavigationHandle().forward(GenericFragmentKey("First"))
        val firstContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Second"))
        val secondContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Third"))
        val thirdContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Third"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == thirdContext.context)

        activity.onBackPressed()
        expectActivity<MultipleFragmentContainerActivity>()
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)
        assertTrue(activity.primaryContainer.isActive)

        activity.onBackPressed()
        expectActivity<MultipleFragmentContainerActivity>()
        assertTrue(activity.primaryContainer.activeContext?.contextReference == null)
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)
        assertFalse(activity.primaryContainer.isActive)
        assertFalse(activity.secondaryContainer.isActive)

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
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Second"))
        val secondContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Third"))
        val thirdContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Third"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == thirdContext.context)

        activity.onBackPressed()
        expectActivity<MultipleComposableContainerActivity>()
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)
        assertTrue(activity.primaryContainer.isActive)

        activity.onBackPressed()
        expectActivity<MultipleComposableContainerActivity>()
        assertTrue(activity.primaryContainer.activeContext?.contextReference == null)
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)
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
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)

        activity.getNavigationHandle().forward(GenericFragmentKey("Second"))
        val secondContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == secondContext.context)

        scenario.recreate()
        activity = expectActivity()
        val secondContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == secondContextRecreated.context)

        activity.onBackPressed()
        val firstContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContextRecreated.context)

        activity.onBackPressed()
        waitFor { activity.primaryContainer.activeContext?.contextReference == null }

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
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Second"))
        val secondContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Third"))
        val thirdContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Third"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == thirdContext.context)

        activity.secondaryContainer.setActive()
        scenario.recreate()
        activity = expectActivity()

        val secondContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContextRecreated.context)
        assertTrue(activity.secondaryContainer.isActive)

        activity.onBackPressed()
        waitFor { activity.secondaryContainer.activeContext?.contextReference == null }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericFragmentKey("Fourth"))
        val fourthContext = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Fourth"
        }
        waitFor { activity.primaryContainer.activeContext?.contextReference == fourthContext.context }
        assertTrue(activity.primaryContainer.isActive)

        activity.onBackPressed()
        val thirdContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { (activity.primaryContainer.activeContext?.contextReference == thirdContextRecreated.context) }

        activity.onBackPressed()
        val firstContextRecreated = expectContext<GenericFragment, GenericFragmentKey> {
            it.navigation.key.id == "First"
        }
        waitFor { (activity.primaryContainer.activeContext?.contextReference == firstContextRecreated.context) }

        activity.onBackPressed()
        waitFor { (activity.primaryContainer.activeContext?.contextReference == null) }

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
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)

        activity.getNavigationHandle().forward(GenericComposableKey("Second"))
        val secondContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == secondContext.context)

        scenario.recreate()
        activity = expectActivity()
        val secondContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == secondContextRecreated.context)

        activity.onBackPressed()
        val firstContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "First"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContextRecreated.context)

        activity.onBackPressed()
        waitFor { activity.primaryContainer.activeContext?.contextReference == null }

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
        assertTrue(activity.primaryContainer.activeContext?.contextReference == firstContext.context)

        activity.secondaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Second"))
        val secondContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContext.context)

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Third"))
        val thirdContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Third"
        }
        assertTrue(activity.primaryContainer.activeContext?.contextReference == thirdContext.context)

        activity.secondaryContainer.setActive()
        scenario.recreate()
        activity = expectActivity()

        val secondContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Second"
        }
        assertTrue(activity.secondaryContainer.activeContext?.contextReference == secondContextRecreated.context)
        assertTrue(activity.secondaryContainer.isActive)

        activity.onBackPressed()
        waitFor { activity.secondaryContainer.activeContext?.contextReference == null }

        activity.primaryContainer.setActive()
        activity.getNavigationHandle().forward(GenericComposableKey("Fourth"))
        val fourthContext = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Fourth"
        }
        waitFor { activity.primaryContainer.activeContext?.contextReference == fourthContext.context }
        assertTrue(activity.primaryContainer.isActive)

        activity.onBackPressed()
        val thirdContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "Third"
        }
        waitFor { (activity.primaryContainer.activeContext?.contextReference == thirdContextRecreated.context) }

        activity.onBackPressed()
        val firstContextRecreated = expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == "First"
        }
        waitFor { (activity.primaryContainer.activeContext?.contextReference == firstContextRecreated.context) }

        activity.onBackPressed()
        waitFor { (activity.primaryContainer.activeContext?.contextReference == null) }

        activity.onBackPressed()
        expectNoActivity()
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
            primaryContainer = rememberEnroContainerController()

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "SingleComposableContainerActivity", fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                Text(text = dev.enro.core.compose.navigationHandle().key.toString(), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                EnroContainer(
                    controller = primaryContainer,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).background(Color(0x22FF0000)).padding(horizontal = 20.dp)
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
            primaryContainer = rememberEnroContainerController()
            secondaryContainer = rememberEnroContainerController()

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "MultipleComposableContainerActivity", fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                Text(text = dev.enro.core.compose.navigationHandle().key.toString(), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                EnroContainer(
                    controller = primaryContainer,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).weight(1f).background(Color(0x22FF0000)).padding(horizontal = 20.dp)
                )
                EnroContainer(
                    controller = secondaryContainer,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).weight(1f).background(Color(0x220000FF)).padding(horizontal =20.dp)
                )
            }
        }
    }
}