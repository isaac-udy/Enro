package dev.enro.core.fragment.internal

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import kotlinx.parcelize.Parcelize

internal abstract class AbstractSingleFragmentKey : NavigationKey {
    abstract val instruction: AnyOpenInstruction
}

@Parcelize
internal data class SingleFragmentKey(
    override val instruction: AnyOpenInstruction
) : AbstractSingleFragmentKey()

@Parcelize
internal data class HiltSingleFragmentKey(
    override val instruction: AnyOpenInstruction
) : AbstractSingleFragmentKey()

internal abstract class AbstractSingleFragmentActivity : AppCompatActivity() {

    private val container by navigationContainer(
        containerId = R.id.enro_internal_single_fragment_frame_layout,
        rootInstruction = { handle.key.instruction },
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    private val handle by navigationHandle<AbstractSingleFragmentKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            id = R.id.enro_internal_single_fragment_frame_layout
        })
    }
}

internal class SingleFragmentActivity : AbstractSingleFragmentActivity()

@AndroidEntryPoint
internal class HiltSingleFragmentActivity : AbstractSingleFragmentActivity()