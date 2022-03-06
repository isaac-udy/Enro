package dev.enro.result

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.navigationHandle
import dev.enro.getActiveEnroResultChannels
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.*


class ComposableRecyclerViewResultTests {
    @get:Rule
    val composeContentRule = createAndroidComposeRule<ComposeRecyclerViewResultActivity>()

    @Test
    fun whenListItemWithResultIsRenderedOnItsOwn_thenResultIsRetrievedSuccessfully() {
        val scenario = composeContentRule.activityRule.scenario
        scenario.onActivity {
            it.setupItems(1)
        }
        scenario.assertResultIsReceivedFor(0)
    }

    @Test
    fun whenMultipleListItemWithResultsAreRendered_andActivityIsDestroyed_thenResultChannelsAreCleanedUp() {
        val scenario = composeContentRule.activityRule.scenario
        scenario.onActivity {
            it.setupItems(5)
        }
        Thread.sleep(1000)
        assertEquals(5, getActiveEnroResultChannels().size)
        scenario.close()
        assertEquals(0, getActiveEnroResultChannels().size)
    }

    @Test
    fun whenHundredsOfListItemWithResultsAreRendered_andScreenIsScrolled_thenNonVisibleResultChannelsAreCleanedUp() {
        val scenario = composeContentRule.activityRule.scenario
        scenario.onActivity {
            it.setupItems(5000)
        }
        repeat(200) {
            scenario.scrollTo(it * 10)
        }
        var maximumExpectedItems = 0
        scenario.onActivity {
            maximumExpectedItems = it.adapter.attachedViewHolderCount
        }

        val activeChannels = getActiveEnroResultChannels()
        assertEquals(maximumExpectedItems, activeChannels.size)
    }

    @Test
    fun whenMultipleListItemWithResultsAreRendered_thenResultIsRetrievedSuccessfullyToTheCorrectItem() {
        val scenario = composeContentRule.activityRule.scenario
        scenario.onActivity {
            it.setupItems(5)
        }

        scenario.assertResultIsReceivedFor(0)
        scenario.assertResultIsReceivedFor(2)
        scenario.assertResultIsReceivedFor(4)
    }

    @Test
    fun whenMultipleListItemWithResultsAreRenderedInRecyclerView_thenResultIsRetrievedSuccessfullyToTheCorrectItem() {
        val scenario = composeContentRule.activityRule.scenario
        scenario.onActivity {
            it.setupItems(500)
        }
        scenario.scrollTo(100)
        scenario.assertResultIsReceivedFor(100)

        scenario.scrollTo(460)
        scenario.assertResultIsReceivedFor(460)

        scenario.scrollTo(10)
        scenario.assertResultIsReceivedFor(10)

        scenario.scrollTo(420)
        scenario.assertResultIsReceivedFor(420)

        scenario.scrollTo(0)
        scenario.assertResultIsReceivedFor(0)
    }


    private val ActivityScenario<ComposeRecyclerViewResultActivity>.items: List<ComposeRecyclerViewItem>
        get() {
            lateinit var items: List<ComposeRecyclerViewItem>
            onActivity {
                items = it.items
            }
            return items
        }

    private fun ActivityScenario<ComposeRecyclerViewResultActivity>.assertResultIsReceivedFor(index: Int) {
        val id = items[index].id
        composeContentRule.onNodeWithTag("result@${id}").assertTextEquals("EMPTY")
        composeContentRule.onNodeWithTag("button@${id}").performClick()
        composeContentRule.onNodeWithTag("result@${id}").assertTextEquals(id.reversed())
    }

    private fun ActivityScenario<ComposeRecyclerViewResultActivity>.scrollTo(index: Int) {
        onView(withId(ComposeRecyclerViewResultActivity.recyclerViewId))
            .perform(RecyclerViewActions.scrollToPosition<ResultViewHolder>(index))
    }
}

@Parcelize
class ComposeRecyclerViewResultActivityKey : NavigationKey

@NavigationDestination(ComposeRecyclerViewResultActivityKey::class)
class ComposeRecyclerViewResultActivity : AppCompatActivity() {
    private val navigation by navigationHandle<RecyclerViewResultActivityKey> {
        defaultKey(RecyclerViewResultActivityKey())
    }

    val adapter by lazy {
        ComposeResultTestAdapter(navigation)
    }

    val recyclerView by lazy {
        RecyclerView(this).apply {
            id = recyclerViewId
            adapter = this@ComposeRecyclerViewResultActivity.adapter
            layoutManager = LinearLayoutManager(this@ComposeRecyclerViewResultActivity)
            itemAnimator = null
        }
    }

    lateinit var items: List<ComposeRecyclerViewItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(recyclerView)
    }

    fun setupItems(size: Int) {
        items = List(size) { index ->
            ComposeRecyclerViewItem(
                id = UUID.randomUUID().toString(),
                onResultUpdated = {
                    result = it
                    adapter.notifyItemChanged(index)
                }
            )
        }
        adapter.submitList(items)
    }

    companion object {
        val recyclerViewId = View.generateViewId()
    }
}

data class ComposeRecyclerViewItem(
    val id: String,
    var result: String = "EMPTY",
    val onResultUpdated: ComposeRecyclerViewItem.(String) -> Unit,
)

class ComposeResultTestAdapter(
    val navigationHandle: NavigationHandle
) : ListAdapter<ComposeRecyclerViewItem, ComposeResultViewHolder>(
    object: DiffUtil.ItemCallback<ComposeRecyclerViewItem>() {
        override fun areItemsTheSame(oldItem: ComposeRecyclerViewItem, newItem: ComposeRecyclerViewItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ComposeRecyclerViewItem, newItem: ComposeRecyclerViewItem): Boolean {
            return oldItem == newItem
        }
    }
) {
    var attachedViewHolderCount = 0
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeResultViewHolder {
        return ComposeResultViewHolder(
            ComposeView(parent.context)
        )
    }

    override fun onViewAttachedToWindow(holder: ComposeResultViewHolder) {
        attachedViewHolderCount++
    }

    override fun onViewDetachedFromWindow(holder: ComposeResultViewHolder) {
        attachedViewHolderCount--
    }

    override fun onBindViewHolder(holder: ComposeResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ComposeResultViewHolder(
    val composeView: ComposeView,
) : RecyclerView.ViewHolder(composeView) {

    fun bind(item: ComposeRecyclerViewItem) {
        composeView.setContent {
            ListItemWithResult(
                id = item.id,
                result = item.result,
                onResultUpdated = {
                    item.onResultUpdated(item, it)
                }
            )
        }
    }
}

@Composable
private fun ListItemWithResult(
    id: String,
    result: String,
    onResultUpdated: (String) -> Unit,
) {
    val channel = registerForNavigationResult<String> {
        onResultUpdated(it)
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
            text = result,
            modifier = Modifier.testTag("result@$id")
        )
    }
}