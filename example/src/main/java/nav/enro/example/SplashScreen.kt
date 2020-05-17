package nav.enro.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle
import nav.enro.core.replaceRoot

@Parcelize
class SplashScreenKey : NavigationKey

@NavigationDestination(SplashScreenKey::class, allowDefault = true)
class SplashScreenActivity : AppCompatActivity() {

    private val navigation by navigationHandle<SplashScreenKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(View(this).apply {
            setBackgroundResource(R.color.colorPrimary)
        })
    }

    override fun onResume() {
        super.onResume()
        navigation.replaceRoot(MainKey())
    }
}