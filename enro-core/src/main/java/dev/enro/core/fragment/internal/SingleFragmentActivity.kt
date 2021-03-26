package dev.enro.core.fragment.internal

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.R
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SingleFragmentKey(
    val instruction: NavigationInstruction.Open
) : NavigationKey

internal abstract class AbstractSingleFragmentActivity : AppCompatActivity() {
    private val handle by navigationHandle<SingleFragmentKey> {
        container(R.id.enro_internal_single_fragment_frame_layout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            id = R.id.enro_internal_single_fragment_frame_layout
        })

        if(savedInstanceState == null) {
            handle.executeInstruction(handle.key.instruction)
        }
    }
}
internal class SingleFragmentActivity : AbstractSingleFragmentActivity()

@AndroidEntryPoint
internal class HiltSingleFragmentActivity : AbstractSingleFragmentActivity()