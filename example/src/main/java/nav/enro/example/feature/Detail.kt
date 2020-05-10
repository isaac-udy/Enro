package nav.enro.example.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.navigationHandle
import nav.enro.result.ResultNavigationKey
import nav.enro.result.closeWithResult

@Parcelize
data class DetailKey(
    val userId: String,
    val id: String
) : ResultNavigationKey<Boolean>

@NavigationDestination(DetailKey::class)
class DetailActivity : AppCompatActivity() {
    private val navigation by navigationHandle<DetailKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(TextView(this).apply {
            text = "Detail View ${navigation.key.id}"
            setBackgroundColor(0xFFFFFFFF.toInt())
        })
    }
}


class DetailFragment : Fragment() {
    private val navigation by navigationHandle<DetailKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TextView(requireContext()).apply {
            text = "Detail View ${navigation.key.id}"
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigation.onCloseRequested {
            navigation.closeWithResult(false)
        }

    }
}
