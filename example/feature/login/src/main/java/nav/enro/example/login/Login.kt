package nav.enro.example.login

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import nav.enro.viewmodel.NavigationViewModelFactory
import nav.enro.example.core.data.UserRepository
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.login.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.*
import nav.enro.example.core.navigation.DashboardKey
import nav.enro.example.core.navigation.LoginErrorKey
import nav.enro.example.core.navigation.LoginKey

@NavigationDestination(
    key = LoginKey::class,
    allowDefault = true
)
class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel> { NavigationViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        viewModel.observableState.observe(this) {
            if(userInput.text.toString() != it.username) {
                userInput.setTextKeepState(it.username)
            }
        }

        userInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onUserNameUpdated(text?.toString() ?: "")
        }

        loginButton.setOnClickListener {
            viewModel.onLogin()
        }
    }
}

data class LoginState(
    val username: String = ""
)

class LoginViewModel(
    private val navigationHandle: NavigationHandle<Nothing>
) : nav.enro.example.core.base.SingleStateViewModel<LoginState>() {

    private val userRepo = UserRepository()

    init {
        state = LoginState()
    }

    fun onUserNameUpdated(username: String) {
        state = state.copy(
            username = username
        )
    }

    fun onLogin() {
        val user = userRepo.getUsers().firstOrNull {
            it.equals(state.username, ignoreCase = true)
        }
        when(user) {
            null -> navigationHandle.forward(
                LoginErrorKey(
                    state.username
                )
            )
            else -> navigationHandle.replaceRoot(
                DashboardKey(user)
//                DashboardKey("Second!"),
//                ListKey("Isaac", ListFilterType.ALL),
//                DetailKey("Isaac","12211221"),
//                DashboardKey("Third!"),
//                UserKey("Isaac 1"),
//                UserKey("Isaac 2"),
//                DashboardKey("Last")
                )
        }
    }
}