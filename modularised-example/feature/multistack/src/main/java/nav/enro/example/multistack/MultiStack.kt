package nav.enro.example.multistack

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.multistack.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.forward
import nav.enro.core.getNavigationHandle
import nav.enro.core.navigationHandle
import nav.enro.example.core.navigation.MultiStackKey
import nav.enro.multistack.MultistackContainer
import nav.enro.multistack.multistackController

@Parcelize
class MultiStackItem(
    vararg val data: String
) : NavigationKey


@NavigationDestination(MultiStackKey::class)
class MultiStackActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MultiStackKey> {
        container(R.id.redFrame)
        container(R.id.greenFrame)
        container(R.id.blueFrame)
    }

    private val multistack by multistackController {
        container(R.id.redFrame, MultiStackItem("Red"))
        container(R.id.greenFrame, MultiStackItem("Green"))
        container(R.id.blueFrame, MultiStackItem("Blue"))
    }

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

@NavigationDestination(MultiStackItem::class)
class MultiStackFragment : Fragment() {

    private val navigation by navigationHandle<MultiStackItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LinearLayout(requireContext()).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

            addView(
                TextView(requireContext()).apply {
                    layoutParams = params
                    text = navigation.key.data.joinToString(" -> ")
                    setPadding(50)
                }
            )

            addView(
                Button(requireContext()).apply {
                    layoutParams = params
                    text = "Forward"
                    setOnClickListener {
                        val dataValue = navigation.key.data.last().toIntOrNull() ?: 0
                        val nextKey = MultiStackItem(*navigation.key.data, (dataValue + 1).toString())
                        navigation.forward(nextKey)
                    }
                }
            )
        }
    }
}