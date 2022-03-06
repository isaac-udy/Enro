package dev.enro.result

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import dev.enro.annotations.NavigationDestination
import dev.enro.application
import dev.enro.callPrivate
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import dev.enro.core.navigationHandle
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.registerForNavigationResult
import kotlinx.parcelize.Parcelize
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*


class RecyclerViewResultTests {

    @Test
    fun whenListItemWithResultIsRenderedOnItsOwn_thenResultIsRetrievedSuccessfully() {
        val scenario = ActivityScenario.launch(RecyclerViewResultActivity::class.java)
        scenario.onActivity {
            it.setupItems(1)
        }
        scenario.assertResultIsReceivedFor(0)
    }

    @Test
    fun whenMultipleListItemWithResultsAreRendered_andActivityIsDestroyed_thenResultChannelsAreCleanedUp() {
        val scenario = ActivityScenario.launch(RecyclerViewResultActivity::class.java)
        scenario.onActivity {
            it.setupItems(5)
        }
        scenario.close()

        val activeChannels = getActiveEnroResultChannels()
        assertEquals(0, activeChannels.size)
    }

    @Test
    fun whenHundredsOfListItemWithResultsAreRendered_andScreenIsScrolled_thenNonVisibleResultChannelsAreCleanedUp() {
        val scenario = ActivityScenario.launch(RecyclerViewResultActivity::class.java)
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
        val scenario = ActivityScenario.launch(RecyclerViewResultActivity::class.java)
        scenario.onActivity {
            it.setupItems(5)
        }

        scenario.assertResultIsReceivedFor(0)
        scenario.assertResultIsReceivedFor(2)
        scenario.assertResultIsReceivedFor(4)
    }

    @Test
    fun whenMultipleListItemWithResultsAreRenderedInRecyclerView_thenResultIsRetrievedSuccessfullyToTheCorrectItem() {
        val scenario = ActivityScenario.launch(RecyclerViewResultActivity::class.java)
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


    private val ActivityScenario<RecyclerViewResultActivity>.items: List<RecyclerViewItem>
        get() {
            lateinit var items: List<RecyclerViewItem>
            onActivity {
                items = it.items
            }
            return items
        }

    private fun ActivityScenario<RecyclerViewResultActivity>.assertResultIsReceivedFor(index: Int) {
        val id = items[index].id
        onView(withContentDescription(Matchers.equalTo(id)))
            .check(matches(withText("$id@EMPTY")))

        onView(withContentDescription(Matchers.equalTo(id)))
            .perform(ViewActions.click())

        onView(withContentDescription(Matchers.equalTo(id)))
            .check(matches(withText("$id@${id.reversed()}")))
    }

    private fun ActivityScenario<RecyclerViewResultActivity>.scrollTo(index: Int) {
        onView(withId(RecyclerViewResultActivity.recyclerViewId))
            .perform(RecyclerViewActions.scrollToPosition<ResultViewHolder>(index))
    }
}

@Parcelize
class RecyclerViewResultActivityKey : NavigationKey

@NavigationDestination(RecyclerViewResultActivityKey::class)
class RecyclerViewResultActivity : AppCompatActivity() {
    private val navigation by navigationHandle<RecyclerViewResultActivityKey> {
        defaultKey(RecyclerViewResultActivityKey())
    }

    val adapter by lazy {
        ResultTestAdapter(navigation)
    }

    val recyclerView by lazy {
        RecyclerView(this).apply {
            id = recyclerViewId
            adapter = this@RecyclerViewResultActivity.adapter
            layoutManager = LinearLayoutManager(this@RecyclerViewResultActivity)
            itemAnimator = null
        }
    }

    lateinit var items: List<RecyclerViewItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(recyclerView)
    }

    fun setupItems(size: Int) {
        items = List(size) { index ->
            RecyclerViewItem(
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

data class RecyclerViewItem(
    val id: String,
    var result: String = "EMPTY",
    val onResultUpdated: RecyclerViewItem.(String) -> Unit,
)

class ResultTestAdapter(
    val navigationHandle: NavigationHandle
) : ListAdapter<RecyclerViewItem, ResultViewHolder>(
    object: DiffUtil.ItemCallback<RecyclerViewItem>() {
        override fun areItemsTheSame(oldItem: RecyclerViewItem, newItem: RecyclerViewItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecyclerViewItem, newItem: RecyclerViewItem): Boolean {
            return oldItem == newItem
        }
    }
) {
    var attachedViewHolderCount = 0
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        return ResultViewHolder(
            textView = TextView(parent.context).apply {
                setPadding(
                    30, 30, 30, 30
                )
            },
            navigationHandle = navigationHandle
        )
    }

    override fun onViewAttachedToWindow(holder: ResultViewHolder) {
        attachedViewHolderCount++
    }

    override fun onViewDetachedFromWindow(holder: ResultViewHolder) {
        attachedViewHolderCount--
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ResultViewHolder(
    val textView: TextView,
    val navigationHandle: NavigationHandle
) : RecyclerView.ViewHolder(textView) {

    private var channel: EnroResultChannel<String>? = null

    fun bind(item: RecyclerViewItem) {
        channel = navigationHandle.registerForNavigationResult<String>(item.id) {
            item.onResultUpdated(item, it)
        }

        textView.contentDescription = item.id
        textView.text = "${item.id}@${item.result}"
        textView.setOnClickListener {
            channel?.open(ImmediateSyntheticResultKey(item.id))
        }
    }
}