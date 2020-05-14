package nav.enro.example.multistack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.multistack.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.navigationHandle
import nav.enro.example.core.navigation.MultiStackKey
import nav.enro.example.core.navigation.UserKey
import nav.enro.multistack.MultistackContainer
import nav.enro.multistack.multistackController

@NavigationDestination(MultiStackKey::class)
class MultiStackActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MultiStackKey> {
        container(R.id.redFrame)
        container(R.id.greenFrame)
        container(R.id.blueFrame)
    }

    private val multistack by multistackController(
        MultistackContainer(R.id.redFrame, UserKey("Red")),
        MultistackContainer(R.id.greenFrame, UserKey("Green")),
        MultistackContainer(R.id.blueFrame, UserKey("Blue"))
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