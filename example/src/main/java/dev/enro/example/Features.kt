package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.example.databinding.FragmentFeaturesBinding
import kotlinx.parcelize.Parcelize


@Parcelize
class Features : NavigationKey

@NavigationDestination(Features::class)
class FeaturesFragment : Fragment() {

    private val navigation by navigationHandle<Features>()
    private val adapter = FeatureAdapter {
        navigation.present(it.key)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_features, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentFeaturesBinding.bind(view).apply {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }
        adapter.submitList(features)
    }
}


data class FeatureDescription(
    val name: String,
    val iconResource: Int = 0,
    val key: NavigationKey.SupportsPresent = SimpleMessage(
        "Missing",
        "This destination hasn't been implemented yet!"
    )
)

val features = listOf(
    FeatureDescription(
        name = "Auto-generated navigation",
        iconResource = R.drawable.ic_round_autorenew_24,
        key = SimpleMessage(
            title = "Auto-generated navigation",
            message = """
                Enro uses annotation processing to automatically generate all the boilerplate code that it needs to run. 
                
                All you need to do to bind a NavigationKey to a Fragment, Activity, or SyntheticDestination is to annotation that class with '@NavigationDestination' and pass the type of your NavigationKey as an argument. Easy!
            """.trimIndent()
        )
    ),
    FeatureDescription(
        name = "Multi-module support",
        iconResource = R.drawable.ic_round_account_tree_24,
        key = SimpleMessage(
            title = "Multi-module support",
            message = """
                Enro was built with multi-module support as a key consideration. 

                To support navigation between Fragments and Activities that don't know about each other, simply define your NavigationKeys in a shared module. Enro's annotation processor takes care of the rest!
                
                For an example of this, look in the 'modularised-example' module in the Enro repository. 
            """.trimIndent()
        )
    ),
    FeatureDescription(
        name = "ViewModel integration",
        iconResource = R.drawable.ic_round_extension_24
    ),
    FeatureDescription(
        name = "Jetpack Compose",
        iconResource = R.drawable.ic_compose,
        key = SimpleMessage(
            title = "Jetpack Compose",
            message = """
                Enro supports Jetpack Compose navigation as a primary concern. 
                
                Click 'Launch' to show an example of how this works. 
                
                To see how this example is built, look at ComposeSimpleExample.kt in the examples.
            """.trimIndent(),
            positiveActionInstruction = NavigationInstruction.Forward(ComposeSimpleExampleKey(
                name = "Start",
                launchedFrom = "Features"
            ))
        )
    ),
    FeatureDescription(
        name = "Receive results from destinations",
        iconResource = R.drawable.ic_round_undo_24,
        key = SimpleMessage(
            title = "Receive results from destinations",
            message = """
                Enro supports destinations returning results (similar to startActivityForResult). This API is modelled after the AndroidX Activity 1.2.0 ActivityResultContract API, so should be reasonably familiar. 
                
                To see how this works, look at ResultExample.kt in the examples.
                
                Click the 'Launch' button to try this out.
            """.trimIndent(),
            positiveActionInstruction = NavigationInstruction.Present(ResultExampleKey())
        )
    ),
    FeatureDescription(
        name = "Deeplinking",
        iconResource = R.drawable.ic_round_link_24,
        key = SimpleMessage(
            title = "Deeplinking",
            message = """
                When you execute a navigation instruction, you can provide more than one navigation key as "child keys", or using one of the 'forward'/'replace'/'replaceRoot' extensions, provide a variable number of navigation keys. 
                
                Doing this will cause those keys to be opened in order, as an easy way to perform deeplinking. 
                
                Click the 'Launch' button to open a deeplink with the following stack:
                "Deeplink 1 -> Deeplink 2 -> Deeplink 3"
            """.trimIndent(),
            positiveActionInstruction = NavigationInstruction.Forward(
                navigationKey = SimpleExampleKey("Deeplink 1", "Features", listOf("Features")),
                children = listOf(
                    SimpleExampleKey("Deeplink 2", "Deeplink 1", listOf("Features", "Deeplink 1")),
                    SimpleExampleKey(
                        "Deeplink 3",
                        "Deeplink 2",
                        listOf("Features", "Deeplink 1", "Deeplink 2")
                    )
                )
            )
        )
    ),
    FeatureDescription(
        name = "Customisable navigation behaviour",
        iconResource = R.drawable.ic_round_tune_24
    ),
    FeatureDescription(
        name = "Synthetic destinations",
        iconResource = R.drawable.ic_round_flip_24,
        key = SimpleMessage(
            title = "Synthetic Destinations",
            message = """
                Most navigation destinations are Activities or Fragments. A synthetic destination is a navigation destination that isn't an Activity or Fragment.
                
                This dialog is being displayed through a synthetic destination. To see how this works, look at SimpleMessage.kt in the examples. 
            """.trimIndent()
        )
    ),
    FeatureDescription(
        name = "Multistack navigation",
        iconResource = R.drawable.ic_round_amp_stories_24,
        key = SimpleMessage(
            title = "Multistack navigation",
            message = """
                The Activity that you're in at the moment is using a MultistackController to keep multiple backstacks active - one for each of the tabs in the BottomNavigationView. 
                
                Each tab maintains it's own backstack, and when you press the back button, you'll go backwards only on the current tab. If you're at the 'base' level of a tab and you press the back button, you'll go back to the 'Home' tab. 
                
                To see how this works, look at Main.kt in the examples.
            """.trimIndent()
        )
    ),
    FeatureDescription(
        name = "Master/Detail navigation",
        iconResource = R.drawable.ic_round_vertical_split_24,
        key = SimpleMessage(
            title = "Master/Detail navigation",
            message = """
                Enro supports Master/Detail navigation through a component called the MasterDetailController. 
                
                Click 'Launch' to show an example of how this works. 
                
                To see how this example is built, look at MasterDetail.kt in the examples.
            """.trimIndent()
        )
    )
)

class FeatureAdapter(
    val onFeatureSelected: (FeatureDescription) -> Unit
) : ListAdapter<FeatureDescription, FeatureAdapter.ViewHolder>(FeatureDescriptionDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_feature_description, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val featureTitle = view.findViewById<TextView>(R.id.featureTitle)
        private val featureIcon = view.findViewById<ImageView>(R.id.featureIcon)

        fun bind(feature: FeatureDescription) {
            featureTitle.text = feature.name
            featureIcon.setImageResource(feature.iconResource)
            itemView.setOnClickListener {
                onFeatureSelected(feature)
            }
        }
    }
}

object FeatureDescriptionDiff : DiffUtil.ItemCallback<FeatureDescription>() {
    override fun areItemsTheSame(
        oldItem: FeatureDescription,
        newItem: FeatureDescription
    ): Boolean = oldItem.name == newItem.name

    override fun areContentsTheSame(
        oldItem: FeatureDescription,
        newItem: FeatureDescription
    ): Boolean = oldItem == newItem
}