@file:Suppress("DEPRECATION")
package dev.enro.result

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
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
import dev.enro.clearAllEnroResultChannels
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.core.requireNavigationHandle
import dev.enro.core.result.managedByViewHolderItem
import dev.enro.core.result.registerForNavigationResult
import dev.enro.getActiveEnroResultChannels
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.*


class RecyclerViewResultTests {

    @get:Rule
    val rule = kotlin.run {
        // It appears there's a false positive leak on SDK 23 with this test class,
        // so we're going to ignore the leak rule for SDK 23
        if (Build.VERSION.SDK_INT == 23) return@run TestRule { base, _ -> base }
        DetectLeaksAfterTestSuccess()
    }

    @Before
    fun before() {
        // TODO: There's something not quite right on CI, these tests pass on local machines, but
        // something on CI causes previous tests to leave hanging result channels. This needs to be cleaned up.
        clearAllEnroResultChannels()
    }

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

        // TODO: On very fast emulated devices (i.e. those hosted by an M1 MacBook),
        // these tests run too fast and fail because the click event is handled before
        // the activity can actually do anything about it. For now, this sleep will
        // make sure the test runs on these fast devices, but there should be a nicer
        // way to do this in the future.
        Thread.sleep(1000)

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
class RecyclerViewResultActivityKey : Parcelable, NavigationKey

@NavigationDestination(RecyclerViewResultActivityKey::class)
class RecyclerViewResultActivity : AppCompatActivity() {
    private val navigation by navigationHandle<RecyclerViewResultActivityKey> {
        defaultKey(RecyclerViewResultActivityKey())
    }

    val adapter = ResultTestAdapter()

    val recyclerView by lazy {
        RecyclerView(this).apply {
            id = recyclerViewId
            adapter = this@RecyclerViewResultActivity.adapter
            layoutManager = LinearLayoutManager(this@RecyclerViewResultActivity)
            itemAnimator = null

            val dip = 32f
            val r = resources
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.displayMetrics
            ).toInt()
            setPadding(px)
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
        recyclerView.invalidate()
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

class ResultTestAdapter() : ListAdapter<RecyclerViewItem, ResultViewHolder>(
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
            navigationHandle = parent.requireNavigationHandle()
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

    fun bind(item: RecyclerViewItem) {
        val channel = navigationHandle
            .registerForNavigationResult<String>(item.id) {
                item.onResultUpdated(item, it)
            }
            .managedByViewHolderItem(this)

        textView.contentDescription = item.id
        textView.text = "${item.id}@${item.result}"
        textView.setOnClickListener {
            channel.open(ImmediateSyntheticResultKey(item.id))
        }
    }

}