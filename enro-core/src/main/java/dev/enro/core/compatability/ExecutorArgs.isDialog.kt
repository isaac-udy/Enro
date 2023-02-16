package dev.enro.core.compatability

import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.DialogFragment
import dev.enro.core.ExecutorArgs
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination

@OptIn(ExperimentalMaterialApi::class)
internal fun ExecutorArgs<*, *, *>.isDialog(): Boolean {
    return DialogFragment::class.java.isAssignableFrom(binding.destinationType.java) ||
                DialogDestination::class.java.isAssignableFrom(binding.destinationType.java)
                || BottomSheetDestination::class.java.isAssignableFrom(binding.destinationType.java)

}