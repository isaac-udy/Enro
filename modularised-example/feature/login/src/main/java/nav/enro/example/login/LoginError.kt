package nav.enro.example.login

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationContext
import nav.enro.core.activity
import nav.enro.core.navigationHandle
import nav.enro.core.synthetic.SyntheticDestination
import nav.enro.example.core.navigation.LoginErrorKey

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

@NavigationDestination(LoginErrorKey::class)
class LoginErrorDestination : SyntheticDestination<LoginErrorKey> {
    override fun process(
        navigationContext: NavigationContext<out Any>,
        key: LoginErrorKey,
        instruction: NavigationInstruction.Open
    ) {
        val activity = navigationContext.activity
        AlertDialog.Builder(activity)
            .setTitle("Error!")
            .setMessage("Whoops! It looks like '${key.errorUser}' isn't a valid user.\n\nPlease try again.")
            .setNegativeButton("Close") { _, _ -> }
            .show()
    }
}