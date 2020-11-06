package nav.enro.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle
import nav.enro.multistack.MultistackContainer
import nav.enro.multistack.multistackController

@Parcelize
class MainKey : NavigationKey

@NavigationDestination(MainKey::class)
class MainActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MainKey> {
        container(R.id.homeContainer) {
            it is Home || it is SimpleExampleKey
        }
    }

    private val mutlistack by multistackController {
        container(R.id.homeContainer, Home())
        container(R.id.featuresContainer, Features())
        container(R.id.profileContainer, Profile())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> mutlistack.openStack(R.id.homeContainer)
                R.id.features -> mutlistack.openStack(R.id.featuresContainer)
                R.id.profile -> mutlistack.openStack(R.id.profileContainer)
                else -> return@setOnNavigationItemSelectedListener false
            }
            return@setOnNavigationItemSelectedListener true
        }

        mutlistack.activeContainer.observe(this, Observer { selectedContainer ->
            bottomNavigation.selectedItemId = when (selectedContainer) {
                R.id.homeContainer -> R.id.home
                R.id.featuresContainer -> R.id.features
                R.id.profileContainer -> R.id.profile
                else -> 0
            }
        })
    }
}