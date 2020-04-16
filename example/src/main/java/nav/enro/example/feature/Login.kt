package nav.enro.example.feature

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import nav.enro.example.R
import nav.enro.example.base.NavigationViewModelFactory
import nav.enro.example.base.SingleStateViewModel
import nav.enro.example.data.UserRepository
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.login.*
import nav.enro.core.*

@Parcelize
class LoginKey : NavigationKey

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
) : SingleStateViewModel<LoginState>() {

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
            null -> navigationHandle.forward(LoginErrorKey(state.username))
            else -> navigationHandle.replaceRoot(DashboardKey(user))
        }
    }
}