package dev.enro.example.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nav.enro.annotations.NavigationDestination
import nav.enro.core.navigationHandle
import nav.enro.example.core.navigation.DetailKey
import nav.enro.core.result.closeWithResult

class DetailActivity : AppCompatActivity() {
    private val navigation by navigationHandle<DetailKey> {
        onCloseRequested {
            closeWithResult(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(TextView(this).apply {
            text = "Detail View ${navigation.key.id}"
            setBackgroundColor(0xFFFFFFFF.toInt())
        })
    }
}

@NavigationDestination(DetailKey::class)
class DetailFragment : Fragment() {
    private val navigation by navigationHandle<DetailKey> {
        onCloseRequested {
            closeWithResult(false)
        }
    }

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
    }
}
