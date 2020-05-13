package nav.enro.example.login

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import nav.enro.core.NavigationKey
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.navigationHandle
import nav.enro.example.core.navigation.LoginErrorKey

@NavigationDestination(LoginErrorKey::class)
class LoginErrorFragment : DialogFragment() {

    private val navigation by navigationHandle<LoginErrorKey>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val key = navigation.key
        return AlertDialog.Builder(requireContext())
            .setTitle("Whoops!")
            .setMessage("\"${key.errorUser}\" isn't a valid user!")
            .create()
    }
}