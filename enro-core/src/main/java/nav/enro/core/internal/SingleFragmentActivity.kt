package nav.enro.core.internal

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle

@Parcelize
internal data class SingleFragmentKey(
    val instruction: NavigationInstruction.Open
) : NavigationKey

internal abstract class AbstractSingleFragmentActivity : AppCompatActivity() {
    private val handle by navigationHandle<SingleFragmentKey> {
        container(R.id.content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) {
            handle.executeInstruction(handle.key.instruction)
        }
    }
}
internal class SingleFragmentActivity : AbstractSingleFragmentActivity()

@AndroidEntryPoint
internal class HiltSingleFragmentActivity : AbstractSingleFragmentActivity()