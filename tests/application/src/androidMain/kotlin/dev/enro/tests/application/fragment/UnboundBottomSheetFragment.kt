package dev.enro.tests.application.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.destination.synthetic.syntheticDestination
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object UnboundBottomSheet : Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(UnboundBottomSheet::class)
val unboundBottomSheet = syntheticDestination<UnboundBottomSheet> {
    TODO("Synthetics")
//    val activity = navigationContext.activity as FragmentActivity
//    UnboundBottomSheetFragment()
//        .show(activity.supportFragmentManager, UnboundBottomSheetFragment.tag)
}

class UnboundBottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TitledColumn(title = "Unbound BottomSheet Fragment") {
                        Button(onClick = { dismiss() }) {
                            Text("Close with Enro")
                        }
                        Button(onClick = { dismiss() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val tag = "UnboundBottomSheetFragment"
    }
}