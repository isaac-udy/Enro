package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.forward
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationHandle
import dev.enro.core.replace
import dev.enro.core.replaceRoot
import dev.enro.example.databinding.FragmentEmbeddedBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmbeddedKey(
    val name: String,
    val launchedFrom: String,
    val backstack: List<String> = emptyList()
): NavigationKey

@NavigationDestination(EmbeddedKey::class)
class EmbeddedFragment: Fragment() {
    private val navigation by navigationHandle<EmbeddedKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_embedded, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentEmbeddedBinding.bind(view).apply {
            forwardButton.setOnClickListener {
                val next = SimpleExampleKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack + navigation.key.name
                )
                parentFragment?.getNavigationHandle()?.forward(next)
            }

            replaceButton.setOnClickListener {
                val next = SimpleExampleKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack
                )
                parentFragment?.getNavigationHandle()?.replace(next)
            }

            replaceRootButton.setOnClickListener {
                val next = SimpleExampleKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = emptyList()
                )
                parentFragment?.getNavigationHandle()?.replaceRoot(next)
            }

            forwardButtonEmbedded.setOnClickListener {
                val next = SimpleExampleKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name + " Embedded",
                    backstack = navigation.key.backstack + navigation.key.name
                )
                navigation.forward(next)
            }

            replaceButtonEmbedded.setOnClickListener {
                val next = SimpleExampleKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name + " Embedded",
                    backstack = navigation.key.backstack
                )
                navigation.replace(next)
            }

            replaceRootButtonEmbedded.setOnClickListener {
                val next = SimpleExampleKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name + " Embedded",
                    backstack = emptyList()
                )
                navigation.replaceRoot(next)
            }
        }
    }
}

private fun EmbeddedKey.getNextDestinationName(): String {
    if(name.length != 1) return "A"
    return (name[0] + 1).toString()
}