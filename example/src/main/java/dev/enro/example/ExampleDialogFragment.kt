package dev.enro.example

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_example_dialog.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*

@Parcelize
class ExampleDialogKey(val number: Int = 1) : NavigationKey

@NavigationDestination(ExampleDialogKey::class)
class ExampleDialogFragment : DialogFragment() {

    private val navigation by navigationHandle<ExampleDialogKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_example_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        exampleDialogNumber.text = navigation.key.number.toString()

        exampleDialogForward.setOnClickListener {
            navigation.forward(ExampleDialogKey(navigation.key.number + 1))
        }

        exampleDialogReplace.setOnClickListener {
            navigation.replace(ResultExampleKey())
        }

        exampleDialogClose.setOnClickListener {
            navigation.close()
        }
    }
}