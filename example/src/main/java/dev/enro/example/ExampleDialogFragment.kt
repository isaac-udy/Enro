package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.example.databinding.FragmentExampleDialogBinding
import kotlinx.parcelize.Parcelize

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
        FragmentExampleDialogBinding.bind(view).apply {
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
}