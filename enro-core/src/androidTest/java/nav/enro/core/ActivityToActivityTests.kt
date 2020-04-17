package nav.enro.core

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.*
import org.junit.Test
import java.util.*

class ActivityToActivityTests {

    @Test
    fun givenDefaultActivityOpenedWithoutNavigationKeySet_thenDefaultKeyIsUsed() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()
        assertEquals(defaultKey, handle.key)
    }

    @Test
    fun givenDefaultActivity_whenCloseInstructionIsExecuted_thenNoActivitiesAreOpen() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()
        handle.close()
        expectNoActivity()
    }

    @Test
    fun givenDefaultActivity_whenNavigationInstructionIsExecuted_thenCorrectActivityIsOpened() {
        val id = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()
        handle.forward(GenericActivityKey(id))

        val next = expectActivity<GenericActivity>()
        val nextHandle by next.navigationHandle<GenericActivityKey>()

        assertEquals(id, nextHandle.key.id)
    }

    @Test
    fun givenActivityOpenedWithChildren_thenFinalOpenedActivityIsLastChild() {
        val id = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()
        handle.execute(
            NavigationInstruction.Open(
                NavigationDirection.FORWARD,
                GenericActivityKey(UUID.randomUUID().toString()),
                listOf(
                    GenericActivityKey(UUID.randomUUID().toString()),
                    GenericActivityKey(UUID.randomUUID().toString()),
                    GenericActivityKey(UUID.randomUUID().toString()),
                    GenericActivityKey(id)
                )
            )
        )

        expectActivity<GenericActivity> {
            it.navigationHandle<GenericActivityKey>().value.key.id == id
        }
    }

    @Test
    fun givenDefaultActivity_whenSpecificActivityIsOpened_andThenSpecificActivityIsClosed_thenDefaultActivityIsOpen() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()
        handle.forward(GenericActivityKey("close"))

        val next = expectActivity<GenericActivity>()
        val nextHandle by next.navigationHandle<GenericActivityKey>()
        nextHandle.close()

        val activeActivity = expectActivity<DefaultActivity>()
        val activeHandle by activeActivity.navigationHandle<DefaultActivityKey>()
        assertEquals(defaultKey, activeHandle.key)
    }

    @Test(expected = Throwable::class)
    fun givenActivityDoesNotHaveDefaultKey_whenActivityOpenedWithoutNavigationKeySet_thenNavigationHandleCannotRetrieveKey() {
        val scenario = ActivityScenario.launch(GenericActivity::class.java)
        val handle = scenario.navigationHandle<GenericActivityKey>()
        assertNotNull(handle.key)
    }

    @Test
    fun whenSpecificActivityOpenedWithNavigationKeySet_thenNavigationKeyIsAvailable() {
        val id = UUID.randomUUID().toString()
        val intent =
            Intent(
                InstrumentationRegistry.getInstrumentation().context,
                GenericActivity::class.java
            )
                .addOpenInstruction(
                    NavigationInstruction.Open(
                        navigationDirection = NavigationDirection.REPLACE,
                        navigationKey = GenericActivityKey(id)
                    )
                )

        val scenario = ActivityScenario.launch<GenericActivity>(intent)
        val handle = scenario.navigationHandle<GenericActivityKey>()

        assertEquals(id, handle.key.id)
    }

    @Test
    fun whenActivityIsReplaced_andReplacementIsClosed_thenNoActivitiesAreOpen() {
        val id = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()
        handle.replace(GenericActivityKey(id))

        val next = expectActivity<GenericActivity>()
        val nextHandle by next.navigationHandle<GenericActivityKey>()

        nextHandle.close()
        expectNoActivity()
    }

    @Test
    fun whenActivityIsReplaced_andActivityHasParent_andReplacementIsClosed_thenParentIsOpen() {
        val first = UUID.randomUUID().toString()
        val second = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()
        handle.forward(GenericActivityKey(first))

        val firstActivity = expectActivity<GenericActivity> { it.navigationHandle<GenericActivityKey>().value.key.id == first }
        firstActivity.navigationHandle<GenericActivityKey>().value.replace(GenericActivityKey(second))

        val secondActivity = expectActivity<GenericActivity> { it.navigationHandle<GenericActivityKey>().value.key.id == second }
        secondActivity.navigationHandle<GenericActivityKey>().value.close()

        expectActivity<DefaultActivity>()
    }
}