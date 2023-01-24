package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.example.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExampleFragmentKey(
    val name: String,
    val launchedFrom: String,
    val backstack: List<String> = emptyList()
) : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@NavigationDestination(ExampleFragmentKey::class)
class ExampleFragment : Fragment() {

    private val navigation by navigationHandle<ExampleFragmentKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EnroExampleTheme {
                    ExampleScreenTemplate(
                        title = "Fragment",
                        buttons = listOf(
                            "Forward" to {
                                val next = ExampleFragmentKey(
                                    name = navigation.key.getNextDestinationName(),
                                    launchedFrom = navigation.key.name,
                                    backstack = navigation.key.backstack + navigation.key.name
                                )
                                navigation.forward(next)
                            },
                            "Forward (Compose)" to {
                                val next = ExampleComposableKey(
                                    name = navigation.key.getNextDestinationName(),
                                    launchedFrom = navigation.key.name,
                                    backstack = navigation.key.backstack + navigation.key.name
                                )
                                navigation.forward(next)
                            },
                            "Replace" to {
                                val next = ExampleFragmentKey(
                                    name = navigation.key.getNextDestinationName(),
                                    launchedFrom = navigation.key.name,
                                    backstack = navigation.key.backstack
                                )
                                navigation.replace(next)
                            },

                            "Replace Root" to {
                                val next = ExampleFragmentKey(
                                    name = navigation.key.getNextDestinationName(),
                                    launchedFrom = navigation.key.name,
                                    backstack = emptyList()
                                )
                                navigation.replaceRoot(next)
                            },
                        )
                    )
                }
            }
        }
    }
}

private fun ExampleFragmentKey.getNextDestinationName(): String {
    if (name.length != 1) return "A"
    return (name[0] + 1).toString()
}