package nav.enro.example.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nav.enro.example.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.user.*
import nav.enro.core.NavigationKey
import nav.enro.core.forward
import nav.enro.core.navigationHandle
import nav.enro.core.replaceRoot

@Parcelize
class UserKey(
    val userId: String
) : NavigationKey

class UserActivity : AppCompatActivity() {

    private val navigation by navigationHandle<UserKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user)

        subtitle.text = navigation.key.userId

        logOutButton.setOnClickListener {
            navigation.replaceRoot(LoginKey())
        }
    }
}

class UserFragment : Fragment() {

    private val navigation by navigationHandle<UserKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user, container, false).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subtitle.text = navigation.key.userId
        logOutButton.setOnClickListener {
            navigation.forward(UserKey("ASD"))
        }
    }
}