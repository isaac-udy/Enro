package nav.enro.core.internal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationKey
import nav.enro.core.forward
import nav.enro.core.navigationHandle

@Parcelize
internal data class SingleFragmentKey(
    val fragmentNavigationKey: NavigationKey
) : NavigationKey

internal class SingleFragmentActivity : AppCompatActivity() {

    private val navigationHandle by navigationHandle<SingleFragmentKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        navigationHandle.forward(navigationHandle.key.fragmentNavigationKey)
    }
}