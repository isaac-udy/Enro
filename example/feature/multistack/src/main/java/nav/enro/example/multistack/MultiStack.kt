package nav.enro.example.multistack

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.multistack.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.example.core.navigation.MultiStackKey
import nav.enro.example.core.navigation.UserKey
import nav.enro.multistack.MultiStackContainer
import nav.enro.multistack.MultistackControllerProperty
import nav.enro.multistack.multistackController

@NavigationDestination(MultiStackKey::class)
class MultiStackActivity : AppCompatActivity() {

    private val multistack by multistackController(
        MultiStackContainer(R.id.redFrame, UserKey("Red")),
        MultiStackContainer(R.id.greenFrame, UserKey("Green")),
        MultiStackContainer(R.id.blueFrame, UserKey("Blue"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multistack)

        redNavigationButton.setOnClickListener {
            multistack.openStack(R.id.redFrame)
        }

        greenNavigationButton.setOnClickListener {
            multistack.openStack(R.id.greenFrame)
        }

        blueNavigationButton.setOnClickListener {
            multistack.openStack(R.id.blueFrame)
        }
    }
}