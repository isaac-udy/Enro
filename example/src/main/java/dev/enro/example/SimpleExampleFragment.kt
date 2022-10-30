package dev.enro.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.core.present
import dev.enro.core.push
import dev.enro.example.databinding.FragmentSimpleExampleBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class SimpleExampleFragmentKey(
    val name: String,
    val launchedFrom: String,
    val backstack: List<String> = emptyList()
) : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@NavigationDestination(SimpleExampleFragmentKey::class)
class SimpleExampleFragment() : Fragment() {

    private val navigation by navigationHandle<SimpleExampleFragmentKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_example, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentSimpleExampleBinding.bind(view).apply {
            currentDestination.text = navigation.key.name
            launchedFrom.text = navigation.key.launchedFrom
            currentStack.text = (navigation.key.backstack +  navigation.key.name).joinToString(" -> ")

            pushButton.setOnClickListener {
                val next = SimpleExampleFragmentKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack + navigation.key.name
                )
                navigation.push(next)
            }

            pushComposeButton.setOnClickListener {
                val next = SimpleExampleComposeKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack + navigation.key.name
                )
                navigation.push(next)
            }

            presentButton.setOnClickListener {
                val next = SimpleExampleFragmentKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack
                )
                navigation.present(next)
            }

            presentComposeButton.setOnClickListener {
                val next = SimpleExampleComposeKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = emptyList()
                )
                navigation.present(next)
            }
        }

    }
}

private fun SimpleExampleFragmentKey.getNextDestinationName(): String {
    if(name.length != 1) return "A"
    return (name[0] + 1).toString()
}