package dev.enro.example.login

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.activity
import dev.enro.core.navigationHandle
import dev.enro.core.synthetic.SyntheticDestination
import dev.enro.example.core.navigation.LoginErrorKey

@SuppressLint("MissingNavigationDestinationAnnotation")
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
class LoginErrorDestination : SyntheticDestination<LoginErrorKey>() {
    override fun process() {
        val activity = navigationContext.activity
        AlertDialog.Builder(activity)
            .setTitle("Error!")
            .setMessage("Whoops! It looks like '${key.errorUser}' isn't a valid user.\n\nPlease try again.")
            .setNegativeButton("Close") { _, _ -> }
            .show()
    }
}
