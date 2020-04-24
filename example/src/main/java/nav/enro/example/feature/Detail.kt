package nav.enro.example.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle
import kotlinx.android.parcel.Parcelize
import nav.enro.example.base.NavigationViewModelFactory

@Parcelize
data class DetailKey(
    val userId: String,
    val id: String
) : NavigationKey

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
}
