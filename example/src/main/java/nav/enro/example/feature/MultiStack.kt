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

        redNavigationButton.setOnClickListener {
            redFrame.visibility = View.VISIBLE
        }

        greenNavigationButton.setOnClickListener {
            greenFrame.visibility = View.VISIBLE
        }

        blueNavigationButton.setOnClickListener {
            blueFrame.visibility = View.VISIBLE
        }
    }
}