package dev.enro.tests.application.fragment

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationHandle
import dev.enro.core.result.NavigationResultChannel
import dev.enro.core.result.managedByViewHolderItem
import dev.enro.core.result.registerForNavigationResult
import dev.enro.test.application.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
object RecyclerViewResults : NavigationKey.SupportsPresent {
    @Parcelize
    class RootFragment : NavigationKey.SupportsPush

    @Parcelize
    class ResultFragment : NavigationKey.SupportsPush.WithResult<String>
}

@NavigationDestination(RecyclerViewResults::class)
class RecyclerViewResultsActivity : AppCompatActivity() {

    private val container by navigationContainer(
        containerId = R.id.fragment_container,
        root = { RecyclerViewResults.RootFragment() },
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_results)
    }
}

@Parcelize
sealed class RecyclerViewItem : Parcelable {
    data class Compose(val index: Int, var result: String? = null) : RecyclerViewItem()
    data class ComposeWithRememberSaveableResult(val index: Int) : RecyclerViewItem()

    data class ComposeWithExternalResultChannel(val index: Int, var result: String? = null) : RecyclerViewItem() {
        @IgnoredOnParcel
        lateinit var resultChannel: NavigationResultChannel<String, *>

        fun bindResultChannel(
            navigationHandle: NavigationHandle,
            viewHolder: RecyclerView.ViewHolder,
            invalidate: (Int) -> Unit,
        ) {
            resultChannel = navigationHandle
                .registerForNavigationResult<String>(id = index.toString()) {
                    result = it
                    invalidate(index)
                }
                .managedByViewHolderItem(viewHolder)
        }

        fun launchResult() {
            if (!::resultChannel.isInitialized) return
            resultChannel.push(RecyclerViewResults.ResultFragment())
        }
    }

    data class ViewWithInternalResultChannel(val index: Int, var result: String? = null) : RecyclerViewItem()
    data class ViewWithExternalResultChannel(val index: Int, var result: String? = null) : RecyclerViewItem() {
        @IgnoredOnParcel
        lateinit var resultChannel: NavigationResultChannel<String, *>

        fun bindResultChannel(
            navigationHandle: NavigationHandle,
            viewHolder: RecyclerView.ViewHolder,
            invalidate: (Int) -> Unit,
        ) {
            resultChannel = navigationHandle
                .registerForNavigationResult<String>(id = index.toString()) {
                    result = "External($it)"
                    invalidate(index)
                }
                .managedByViewHolderItem(viewHolder)
        }

        fun launchResult() {
            if (!::resultChannel.isInitialized) return
            resultChannel.push(RecyclerViewResults.ResultFragment())
        }
    }

    companion object {
        fun fromIndex(index: Int): RecyclerViewItem {
            return when (index % 5) {
                0 -> Compose(index)
                1 -> ComposeWithRememberSaveableResult(index)
                2 -> ComposeWithExternalResultChannel(index)
                3 -> ViewWithInternalResultChannel(index)
                4 -> ViewWithExternalResultChannel(index)
                else -> error("Invalid index")
            }
        }

        fun itemTypeFor(item: RecyclerViewItem): Int {
            return when (item) {
                is Compose -> 0
                is ComposeWithRememberSaveableResult -> 1
                is ComposeWithExternalResultChannel -> 2
                is ViewWithInternalResultChannel -> 3
                is ViewWithExternalResultChannel -> 4
            }
        }
    }
}


@NavigationDestination(RecyclerViewResults.RootFragment::class)
class RecyclerViewResultsRootFragment : Fragment() {

    private lateinit var items: List<RecyclerViewItem>
    private val ITEMS_KEY = "RecyclerViewResultsRootFragment.items"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items = when (savedInstanceState) {
            null -> List(200) { index -> RecyclerViewItem.fromIndex(index) }
            else -> {
                BundleCompat.getParcelableArrayList(savedInstanceState, ITEMS_KEY, RecyclerViewItem::class.java)!!
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_recycler_view_results, container, false)
            .apply {
                findViewById<RecyclerView>(R.id.recycler_view).apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = RecyclerViewResultsAdapter(
                        savedStateRegistryOwner = this@RecyclerViewResultsRootFragment,
                        viewModelStoreOwner = this@RecyclerViewResultsRootFragment,
                        items = items,
                    )
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(ITEMS_KEY, ArrayList(items))
    }
}


class RecyclerViewResultsAdapter(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val savedStateRegistryOwner: SavedStateRegistryOwner,
    private val items: List<RecyclerViewItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        fun invalidate(index: Int) {
            viewGroup.post { notifyItemChanged(index) }
        }
        return when (viewType) {
            0 -> ComposeViewHolder(
                composeView = ComposeView(viewGroup.context),
                invalidate = ::invalidate
            )

            1 -> ComposeWithRememberSaveableResult(
                composeView = ComposeView(viewGroup.context),
            )

            2 -> ComposeWithExternalResultChannel(
                composeView = ComposeView(viewGroup.context),
                invalidate = ::invalidate
            )

            3 -> ViewWithInternalResultChannel(
                view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.viewholder_recycler_view_results, viewGroup, false),
                invalidate = ::invalidate
            )

            4 -> ViewWithExternalResultChannel(
                view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.viewholder_recycler_view_results, viewGroup, false),
                invalidate = ::invalidate
            )

            else -> error("Invalid view type")
        }.apply {
            itemView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
            itemView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
            itemView.setViewTreeLifecycleOwner(savedStateRegistryOwner)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, index: Int) {
        Log.e("RecyclerViewResults", "onBindViewHolder: ${items[index]}")
        when (viewHolder) {
            is ComposeViewHolder -> {
                viewHolder.bind(items[index] as RecyclerViewItem.Compose)
            }

            is ComposeWithRememberSaveableResult -> {
                viewHolder.bind(items[index] as RecyclerViewItem.ComposeWithRememberSaveableResult)
            }

            is ComposeWithExternalResultChannel -> {
                viewHolder.bind(items[index] as RecyclerViewItem.ComposeWithExternalResultChannel)
            }

            is ViewWithInternalResultChannel -> {
                viewHolder.bind(items[index] as RecyclerViewItem.ViewWithInternalResultChannel)
            }

            is ViewWithExternalResultChannel -> {
                viewHolder.bind(items[index] as RecyclerViewItem.ViewWithExternalResultChannel)
            }

            else -> error("Invalid view holder")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return RecyclerViewItem.itemTypeFor(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ComposeViewHolder(
        private val composeView: ComposeView,
        private val invalidate: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(composeView) {
        fun bind(item: RecyclerViewItem.Compose) {
            composeView.setContent {
                key(item.index) {
                    val recomposer = currentRecomposeScope
                    val oddResultChannel = registerForNavigationResult<String>(
                        id = item.index.toString(),
                    ) {
                        item.result = "Odd($it)"
                        recomposer.invalidate()
                    }
                    val evenResultChannel = registerForNavigationResult<String>(
                        id = item.index.toString(),
                    ) {
                        item.result = "Even($it)"
                        recomposer.invalidate()
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(all = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface)
                                .shadow(1.dp)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    when (item.index % 2 == 0) {
                                        true -> evenResultChannel.push(RecyclerViewResults.ResultFragment())
                                        false -> oddResultChannel.push(RecyclerViewResults.ResultFragment())
                                    }
                                },
                        ) {
                            Text("Compose Item ${item.index}")
                            if (item.result == null) {
                                Text("Click to get result")
                            } else {
                                Text("Result: ${item.result}")
                            }
                        }
                    }
                }
            }
        }
    }

    class ComposeWithRememberSaveableResult(
        private val composeView: ComposeView,
    ) : RecyclerView.ViewHolder(composeView) {
        fun bind(item: RecyclerViewItem.ComposeWithRememberSaveableResult) {
            composeView.setContent {
                key(item.index) {
                    val result = rememberSaveable { mutableStateOf<String?>(null) }
                    val oddResultChannel = registerForNavigationResult<String>(
                        id = item.index.toString(),
                    ) {
                        result.value = "Odd($it)"
                    }
                    val evenResultChannel = registerForNavigationResult<String>(
                        id = item.index.toString(),
                    ) {
                        result.value = "Even($it)"
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(all = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface)
                                .shadow(1.dp)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    when (item.index % 2 == 0) {
                                        true -> evenResultChannel.push(RecyclerViewResults.ResultFragment())
                                        false -> oddResultChannel.push(RecyclerViewResults.ResultFragment())
                                    }
                                },
                        ) {
                            Text("Compose Item (rememberSaveable) ${item.index}")
                            if (result.value == null) {
                                Text("Click to get result")
                            } else {
                                Text("Result: ${result.value}")
                            }
                        }
                    }
                }
            }
        }
    }

    class ComposeWithExternalResultChannel(
        private val composeView: ComposeView,
        private val invalidate: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(composeView) {
        fun bind(item: RecyclerViewItem.ComposeWithExternalResultChannel) {
            composeView.setContent {
                key(item.index) {
                    val navigationHandle = navigationHandle()
                    val recomposer = currentRecomposeScope
                    LaunchedEffect(item) {
                        item.bindResultChannel(
                            navigationHandle = navigationHandle,
                            viewHolder = this@ComposeWithExternalResultChannel,
                            invalidate = { recomposer.invalidate() },
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(all = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface)
                                .shadow(1.dp)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    item.launchResult()
                                },
                        ) {
                            Text("Compose Item (external result channel) ${item.index}")
                            if (item.result == null) {
                                Text("Click to get result")
                            } else {
                                Text("Result: ${item.result}")
                            }
                        }
                    }
                }
            }
        }
    }

    class ViewWithInternalResultChannel(
        view: View,
        private val invalidate: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.viewholder_title)
        private val description = view.findViewById<TextView>(R.id.viewholder_result_text)

        fun bind(item: RecyclerViewItem.ViewWithInternalResultChannel) {
            val navigationHandle = itemView.findViewTreeViewModelStoreOwner()!!.getNavigationHandle()
            val resultChannel = navigationHandle.registerForNavigationResult<String>(
                id = item.index.toString(),
            ) {
                item.result = "$it"
                invalidate(item.index)
            }.managedByViewHolderItem(this@ViewWithInternalResultChannel)

            title.text = "View Item (Internal) ${item.index}"
            description.text = item.result ?: "Click to get result"
            itemView.setOnClickListener {
                resultChannel.push(RecyclerViewResults.ResultFragment())
            }
        }
    }

    class ViewWithExternalResultChannel(
        view: View,
        private val invalidate: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.viewholder_title)
        private val description = view.findViewById<TextView>(R.id.viewholder_result_text)

        fun bind(item: RecyclerViewItem.ViewWithExternalResultChannel) {
            val navigationHandle = itemView.findViewTreeViewModelStoreOwner()!!.getNavigationHandle()
            item.bindResultChannel(
                navigationHandle = navigationHandle,
                viewHolder = this@ViewWithExternalResultChannel,
                invalidate = invalidate,
            )
            title.text = "View Item (External) ${item.index}"
            description.text = item.result ?: "Click to get result"
            itemView.setOnClickListener {
                item.launchResult()
            }
        }
    }
}

@NavigationDestination(RecyclerViewResults.ResultFragment::class)
class FragmentRecyclerViewResultsResultFragment : Fragment() {
    private val navigation by navigationHandle<RecyclerViewResults.ResultFragment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Column(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = {
                            navigation.closeWithResult("A")
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Result: A")
                    }
                    Button(
                        onClick = {
                            navigation.closeWithResult("B")
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Result: B")
                    }
                    Button(
                        onClick = {
                            navigation.closeWithResult("C")
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Result: C")
                    }
                }
            }
        }
    }
}
