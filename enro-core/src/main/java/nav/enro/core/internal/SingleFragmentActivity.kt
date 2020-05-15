package nav.enro.core.internal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.getNavigationHandle

@Parcelize
internal data class SingleFragmentKey(
    internal val instruction: NavigationInstruction.Open<*>
) : NavigationKey

class SingleFragmentActivity : AppCompatActivity() {

    private val handle by getNavigationHandle<SingleFragmentKey> {
        container(android.R.id.content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) {
            handle.executeInstruction(handle.key.instruction)
        }
    }
}