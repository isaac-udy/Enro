package nav.enro.example.feature

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.multistack.*
import nav.enro.core.NavigationDirection
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.addOpenInstruction
import nav.enro.example.R

@Parcelize
class MultiStackKey : NavigationKey

class MultiStackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multistack)
        redFrame.visibility = View.VISIBLE
        greenFrame.visibility = View.GONE
        blueFrame.visibility = View.GONE
        
        val redFragment = UserFragment().apply {
            arguments = Bundle().addOpenInstruction(NavigationInstruction.Open(NavigationDirection.FORWARD, UserKey("Red")))
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.redFrame, redFragment)
            .add(R.id.greenFrame, UserFragment().apply {
                arguments = Bundle().addOpenInstruction(NavigationInstruction.Open(NavigationDirection.FORWARD, UserKey("Green")))
            })
            .add(R.id.blueFrame, UserFragment().apply {
                arguments = Bundle().addOpenInstruction(NavigationInstruction.Open(NavigationDirection.FORWARD, UserKey("Blue")))
            })
            .setPrimaryNavigationFragment(redFragment)
            .commitNow()

        redNavigationButton.setOnClickListener {
            redFrame.visibility = View.VISIBLE
            greenFrame.visibility = View.GONE
            blueFrame.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(supportFragmentManager.findFragmentById(R.id.redFrame))
                .commitNow()
        }

        greenNavigationButton.setOnClickListener {
            redFrame.visibility = View.GONE
            greenFrame.visibility = View.VISIBLE
            blueFrame.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(supportFragmentManager.findFragmentById(R.id.greenFrame))
                .commitNow()
        }

        blueNavigationButton.setOnClickListener {
            redFrame.visibility = View.GONE
            greenFrame.visibility = View.GONE
            blueFrame.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(supportFragmentManager.findFragmentById(R.id.blueFrame))
                .commitNow()
        }
    }
}