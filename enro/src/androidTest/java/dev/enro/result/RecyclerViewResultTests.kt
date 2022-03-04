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
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationHandle
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.ManagedResultChannel
import dev.enro.core.result.registerForNavigationResult
import dev.enro.getNavigationHandle
import kotlinx.parcelize.Parcelize
import org.hamcrest.Matchers
import org.junit.Before
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

    val adapter: ListAdapter<RecyclerViewItem, *> by lazy {
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

    var items: List<RecyclerViewItem>
        get() = adapter.currentList
        private set(value) = adapter.submitList(value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(recyclerView)
    }

    fun setupItems(size: Int) {
        items = List(size) { index ->
            RecyclerViewItem(
                id = UUID.randomUUID().toString(),
                onResultUpdated = { updateItem(index, it) }
            )
        }
    }

    fun updateItem(index: Int, result: String) {
        items = items.mapIndexed { i, item ->
            if(index == i) {
                item.copy(
                    result = result
                )
            }
            else item
        }
    }

    companion object {
        val recyclerViewId = View.generateViewId()
    }
}

data class RecyclerViewItem(
    val id: String,
    val result: String = "EMPTY",
    val onResultUpdated: (String) -> Unit,
)

private class ResultTestAdapter(
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

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private class ResultViewHolder(
    val textView: TextView,
    val navigationHandle: NavigationHandle
) : RecyclerView.ViewHolder(textView) {

    private var channel: EnroResultChannel<String>? = null

    fun bind(item: RecyclerViewItem) {
        channel = navigationHandle.registerForNavigationResult<String> {
            item.onResultUpdated(it)
        }

        textView.contentDescription = item.id
        textView.text = "${item.id}@${item.result}"
        textView.setOnClickListener {
            channel?.open(ImmediateSyntheticResultKey(item.id))
        }
    }
}