package dev.enro.core.fragment.internal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SingleFragmentKey(
    val instruction: NavigationInstruction.Open
) : NavigationKey

internal abstract class AbstractSingleFragmentActivity : AppCompatActivity() {
    private val handle by navigationHandle<SingleFragmentKey> {
        container(android.R.id.content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) {
            handle.executeInstruction(handle.key.instruction)
        }
    }
}
internal class SingleFragmentActivity : AbstractSingleFragmentActivity()

@AndroidEntryPoint
internal class HiltSingleFragmentActivity : AbstractSingleFragmentActivity()