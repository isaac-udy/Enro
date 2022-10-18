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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import dev.enro.DefaultActivity
import dev.enro.clearAllEnroResultChannels
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.getActiveEnroResultChannels
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


class ComposableListResultTests {
    @get:Rule
    val composeContentRule = createAndroidComposeRule<DefaultActivity>()

    @Before
    fun before() {
        // TODO: There's something not quite right on CI, these tests pass on local machines, but
        // something on CI causes previous tests to leave hanging result channels. This needs to be cleaned up.
        clearAllEnroResultChannels()
    }

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
        val activeItems = AtomicInteger(0)
        composeContentRule.setContent {
            val screenHeight = with(LocalDensity.current) {
                LocalConfiguration.current.screenHeightDp.dp.toPx()
            }

            LazyColumn(
                state = state
            ) {
                items(ids) {
                    ListItemWithResult(id = it)
                    DisposableEffect(true) {
                        activeItems.incrementAndGet()
                        onDispose {
                            activeItems.decrementAndGet()
                        }
                    }
                }
            }

            LaunchedEffect(true) {
                while(state.firstVisibleItemIndex < 500) {
                    state.animateScrollBy(screenHeight * 0.2f, tween(easing = LinearEasing))
                }
                scrollFinished = true
            }
        }
        composeContentRule.mainClock.advanceTimeUntil(2 * 60 * 1000) { scrollFinished }
        composeContentRule.waitForIdle()

        // The number of items, as recorded by a DisposableEffect, should match the number of active ResultChannels
        Assert.assertEquals(activeItems.get(), getActiveEnroResultChannels().size)
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