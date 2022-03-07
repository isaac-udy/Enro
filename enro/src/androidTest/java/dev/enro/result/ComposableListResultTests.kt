package dev.enro.result

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import dev.enro.DefaultActivity
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.getActiveEnroResultChannels
import kotlinx.coroutines.delay
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.util.*


class ComposableListResultTests {
    @get:Rule
    val composeContentRule = createAndroidComposeRule<DefaultActivity>()

    @Test
    fun whenListItemWithResultIsRenderedOnItsOwn_thenResultIsRetrievedSuccessfully() {
        val id = UUID.randomUUID().toString()
        composeContentRule.setContent {
            ListItemWithResult(id = id)
        }
        assertResultIsReceivedFor(id)
    }

    @Test
    fun whenMultipleListItemWithResultsAreRendered_thenResultIsRetrievedSuccessfullyToTheCorrectItem() {
        val ids = List(5) { UUID.randomUUID().toString() }
        composeContentRule.setContent {
            Column {
                ids.forEach {
                    ListItemWithResult(id = it)
                }
            }
        }

        assertResultIsReceivedFor(ids[0])
        assertResultIsReceivedFor(ids[2])
        assertResultIsReceivedFor(ids[4])
    }

    @Test
    fun whenMultipleListItemWithResultsAreRenderedInLazyColumn_thenResultIsRetrievedSuccessfullyToTheCorrectItem() {
        val ids = List(500) { UUID.randomUUID().toString() }
        val state = LazyListState()
        val scrollTarget = mutableStateOf(0)
        composeContentRule.setContent {
            LazyColumn(
                state = state
            ) {
                items(ids) {
                    ListItemWithResult(id = it)
                }
            }
            LaunchedEffect(scrollTarget.value) {
                state.animateScrollToItem(scrollTarget.value)
            }
        }
        scrollTarget.value = 100
        composeContentRule.waitForIdle()
        assertResultIsReceivedFor(ids[100])

        scrollTarget.value = 460
        composeContentRule.waitForIdle()
        assertResultIsReceivedFor(ids[460])

        scrollTarget.value = 10
        composeContentRule.waitForIdle()
        assertResultIsReceivedFor(ids[10])

        scrollTarget.value = 420
        composeContentRule.waitForIdle()
        assertResultIsReceivedFor(ids[420])

        scrollTarget.value = 0
        composeContentRule.waitForIdle()
        assertResultIsReceivedFor(ids[0])
    }

    @Test
    fun whenMultipleListItemWithResultsAreRendered_andActivityIsDestroyed_thenResultChannelsAreCleanedUp() {
        val ids = List(5) { UUID.randomUUID().toString() }
        composeContentRule.setContent {
            Column {
                ids.forEach {
                    ListItemWithResult(id = it)
                }
            }
        }
        Assert.assertEquals(5, getActiveEnroResultChannels().size)
        composeContentRule.activityRule.scenario.close()
        Assert.assertEquals(0, getActiveEnroResultChannels().size)
    }

    @Test
    fun whenHundredsOfListItemWithResultsAreRendered_andScreenIsScrolled_thenNonVisibleResultChannelsAreCleanedUp() {
        val ids = List(5000) { UUID.randomUUID().toString() }
        val state = LazyListState()
        var scrollFinished = false
        composeContentRule.setContent {
            val screenHeight = with(LocalDensity.current) {
                LocalConfiguration.current.screenHeightDp.dp.toPx()
            }

            LazyColumn(
                state = state
            ) {
                items(ids) {
                    ListItemWithResult(id = it)
                }
            }

            LaunchedEffect(true) {
                while(state.firstVisibleItemIndex < 500) {
                    state.animateScrollBy(screenHeight * 0.2f, tween(easing = LinearEasing))
                }
                scrollFinished = true
            }
        }
        composeContentRule.waitUntil(2 * 60 * 1000) { scrollFinished }
        composeContentRule.waitForIdle()

        val activeChannels = getActiveEnroResultChannels()
        // By the time we get to this assertion, there will still be some non-visible items
        // which have not been detached from the composition tree, and will still
        // be registered as active EnroResult channels. The important thing here is that we've
        // scrolled past ~100 items, and that the size of the active channels should be close
        // to the number of visible items, so we allow 50% wiggle room in this assertion
        // when comparing active channels to visible items in the list
        Assert.assertTrue(activeChannels.size < (state.layoutInfo.visibleItemsInfo.size * 1.5f))
    }

    private fun assertResultIsReceivedFor(id: String) {
        composeContentRule.onNodeWithTag("result@${id}").assertTextEquals("EMPTY")
        composeContentRule.onNodeWithTag("button@${id}").performClick()
        composeContentRule.onNodeWithTag("result@${id}").assertTextEquals(id.reversed())
    }
}

@Composable
private fun ListItemWithResult(
    id: String,
) {
    val title = remember { mutableStateOf("EMPTY") }
    val channel = registerForNavigationResult<String> {
        title.value = it
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("row@$id")
    ) {
        Button(
            onClick = {
                channel.open(ImmediateSyntheticResultKey(id))
             },
            content = {
                Text(text = "Get Result")
            },
            modifier = Modifier.testTag("button@$id")
        )
        Text(
            text = title.value,
            modifier = Modifier.testTag("result@$id")
        )
    }
}