package nav.enro.core.internal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationKey
import nav.enro.core.forward
import nav.enro.core.navigationHandle

@Parcelize
internal data class SingleFragmentKey(
    internal val id: Int = 0
) : NavigationKey

internal class SingleFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                if(fm.fragments.size == 0) finish()
            }
        }, false)
    }
}