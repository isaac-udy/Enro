package dev.enro.example.destinations.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.example.EnroExampleTheme
import dev.enro.example.core.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class DialogFragmentKey : Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(DialogFragmentKey::class)
class DialogFragmentDestination : DialogFragment() {

    private val navigation by navigationHandle<DialogFragmentKey>()

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