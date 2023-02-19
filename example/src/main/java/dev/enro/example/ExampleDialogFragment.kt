package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.example.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleDialogFragmentKey : NavigationKey.SupportsPresent

@NavigationDestination(ExampleDialogFragmentKey::class)
class ExampleDialogFragment : DialogFragment() {

    private val navigation by navigationHandle<ExampleDialogFragmentKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EnroExampleTheme {
                    ExampleScreenTemplate("Dialog Fragment", modifier = Modifier)
                }
            }
        }
    }
}