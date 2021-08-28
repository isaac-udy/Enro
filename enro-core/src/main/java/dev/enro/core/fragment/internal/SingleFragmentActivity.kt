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

internal abstract class AbstractSingleFragmentKey : NavigationKey {
    abstract val instruction: NavigationInstruction.Open
}

@Parcelize
internal data class SingleFragmentKey(
    override val instruction: NavigationInstruction.Open
) : AbstractSingleFragmentKey()

@Parcelize
internal data class HiltSingleFragmentKey(
    override val instruction: NavigationInstruction.Open
) : AbstractSingleFragmentKey()

internal abstract class AbstractSingleFragmentActivity : AppCompatActivity() {
    private val handle by navigationHandle<AbstractSingleFragmentKey> {
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