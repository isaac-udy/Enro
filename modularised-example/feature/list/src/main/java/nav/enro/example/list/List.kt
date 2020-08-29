package nav.enro.example.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationHandle
import nav.enro.example.core.data.SimpleData
import nav.enro.example.core.data.SimpleDataRepository
import nav.enro.example.core.navigation.DetailKey
import nav.enro.example.core.navigation.ListFilterType
import nav.enro.example.core.navigation.ListKey
import nav.enro.result.closeWithResult
import nav.enro.result.registerForNavigationResult
import nav.enro.viewmodel.enroViewModels
import nav.enro.viewmodel.navigationHandle


@NavigationDestination(ListKey::class)
class ListFragment : Fragment() {
    private val viewModel by enroViewModels<ListViewModel>()
    private val adapter = SimpleDataAdapter {
        call.open(DetailKey(userId = viewModel.state.userId, id = it))
    }

    private val call by registerForNavigationResult<Boolean> {
        viewModel.setResult(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.observableState.observe(viewLifecycleOwner) {
            adapter.submitList(it.items)
        }
        return RecyclerView(requireContext()).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@ListFragment.adapter
            }
    }
}

data class ListState(
    val userId: String,
    val filter: ListFilterType,
    val items: List<SimpleData>,
    val result: Boolean = true
)

class ListViewModel() : nav.enro.example.core.base.SingleStateViewModel<ListState>() {

    private val repo = SimpleDataRepository()
    private val navigation by navigationHandle<ListKey>()

    init {
        val userId = navigation.key.userId
        state = ListState(
            userId = userId,
            filter = navigation.key.filter,
            items = repo.getList(userId)
                .filter {
                    when (navigation.key.filter) {
                        ListFilterType.ALL -> true
                        ListFilterType.MY_PUBLIC -> it.ownerId == userId && it.isPublic
                        ListFilterType.MY_PRIVATE -> it.ownerId == userId && !it.isPublic
                        ListFilterType.ALL_PUBLIC -> it.isPublic
                        ListFilterType.NOT_MY_PUBLIC -> it.ownerId != userId && it.isPublic
                    }
                }
        )

        navigation.onCloseRequested {
            navigation.closeWithResult(state.result)
        }
    }

    fun setResult(it: Boolean) {
        state = state.copy(
            result = it
        )
    }
}

private class SimpleDataAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<SimpleData, SimpleDataAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<SimpleData>() {
        override fun areItemsTheSame(oldItem: SimpleData, newItem: SimpleData): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SimpleData, newItem: SimpleData): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private inner class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val title = view.findViewById<TextView>(R.id.itemTitle)

        fun bind(data: SimpleData) {
            title.text = data.title
            itemView.setOnClickListener { onClick(data.id) }
        }
    }
}