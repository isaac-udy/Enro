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
class ExampleFragmentKey : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

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
                                val next = ExampleFragmentKey()
                                navigation.forward(next)
                            },
                            "Forward (Compose)" to {
                                val next = ExampleComposableKey()
                                navigation.forward(next)
                            },
                            "Replace" to {
                                val next = ExampleFragmentKey()
                                navigation.replace(next)
                            },

                            "Replace Root" to {
                                val next = ExampleFragmentKey()
                                navigation.replaceRoot(next)
                            },
                            "Dialog" to {
                                val next = ExampleFragmentKey()
                                navigation.present(ExampleDialogKey())
                            },
                        )
                    )
                }
            }
        }
    }
}