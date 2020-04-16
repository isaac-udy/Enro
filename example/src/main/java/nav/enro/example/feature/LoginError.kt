package nav.enro.example.feature

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LoginErrorKey(val errorUser: String): NavigationKey

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