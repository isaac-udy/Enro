package dev.enro.core.hosts

import android.os.Bundle
import android.os.Parcelable
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroInternalNavigationKey
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationKey
import dev.enro.core.R
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

internal abstract class AbstractOpenInstructionInActivityKey :
    Parcelable,
    NavigationKey,
    EnroInternalNavigationKey {

    abstract val instruction: AnyOpenInstruction
}

@Parcelize
internal data class OpenInstructionInActivity(
    override val instruction: AnyOpenInstruction
) : AbstractOpenInstructionInActivityKey()

@Parcelize
internal data class OpenInstructionInHiltActivity(
    override val instruction: AnyOpenInstruction
) : AbstractOpenInstructionInActivityKey()

internal abstract class AbstractActivityHostForAnyInstruction : FragmentActivity(), NavigationHost {

    private val container by navigationContainer(
        containerId = R.id.enro_internal_single_fragment_frame_layout,
        rootInstruction = { handle.key.instruction.asPushInstruction() },
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    private val handle by navigationHandle<AbstractOpenInstructionInActivityKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            id = R.id.enro_internal_single_fragment_frame_layout
        })
    }
}

internal class ActivityHostForAnyInstruction : AbstractActivityHostForAnyInstruction()

@AndroidEntryPoint
internal class HiltActivityHostForAnyInstruction : AbstractActivityHostForAnyInstruction()