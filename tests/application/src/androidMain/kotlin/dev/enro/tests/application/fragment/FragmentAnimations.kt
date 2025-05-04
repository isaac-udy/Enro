package dev.enro.tests.application.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import dev.enro.animation.NavigationAnimation
import dev.enro.animation.NavigationAnimationForView
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationHandle
import dev.enro.core.present
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.tests.application.R
import kotlinx.serialization.Serializable

@Serializable
object FragmentAnimations : NavigationKey.SupportsPush {
    @Serializable
    class PushedFragment : NavigationKey.SupportsPush

    @Serializable
    class HostedPushedCompose : NavigationKey.SupportsPush

    @Serializable
    class PresentedFragment : NavigationKey.SupportsPresent

    @Serializable
    class DialogFragment : NavigationKey.SupportsPresent
}

@NavigationDestination(FragmentAnimations::class)
class FragmentAnimationsActivity : AppCompatActivity() {

    private val navigation by navigationHandle<FragmentAnimations>()

    private val container by navigationContainer(
        containerId = R.id.fragment_container,
        emptyBehavior = EmptyBehavior.AllowEmpty,
        animations = {
            defaults(FragmentAnimationsDefaults)
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_animations)
        findViewById<Button>(R.id.fragment_animations_menu)
            .setOnClickListener { view ->
                val popupMenu = PopupMenu(this, view)
                popupMenu.menu.add("Push Fragment")
                popupMenu.menu.add("Push Composable")
                popupMenu.menu.add("Present Fragment")
                popupMenu.menu.add("Present Dialog Fragment")
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Push Fragment" -> navigation.push(FragmentAnimations.PushedFragment())
                        "Push Composable" -> navigation.push(FragmentAnimations.HostedPushedCompose())
                        "Present Fragment" -> navigation.present(FragmentAnimations.PresentedFragment())
                        "Present Dialog Fragment" -> navigation.present(FragmentAnimations.DialogFragment())
                    }
                    true
                }
                popupMenu.show()
            }
    }
}

@NavigationDestination(FragmentAnimations.PresentedFragment::class)
class FragmentAnimationsPresentedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fragment_animations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(0xFFCCFFCC.toInt())
        view.findViewById<TextView>(R.id.fragment_title)
            .setText("Presented Fragment")
    }
}

@NavigationDestination(FragmentAnimations.PushedFragment::class)
class FragmentAnimationsPushedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fragment_animations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(0xFFCCCCFF.toInt())
        view.findViewById<TextView>(R.id.fragment_title)
            .setText("Pushed Fragment")
    }
}

@NavigationDestination(FragmentAnimations.HostedPushedCompose::class)
@Composable
fun FragmentAnimationsHostedPushedCompose() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFCCCC))
    ) {
        Text(
            text = "Pushed Composable",
            style = MaterialTheme.typography.h6
        )
    }
}

@NavigationDestination(FragmentAnimations.DialogFragment::class)
class FragmentAnimationsDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Dialog Fragment")
            .setMessage("This is a dialog fragment.")
            .setPositiveButton("Close") { _, _ ->
                getNavigationHandle().requestClose()
            }
            .create()
    }
}

object FragmentAnimationsDefaults : NavigationAnimation.Defaults<NavigationAnimationForView> {
    override val none: NavigationAnimationForView = NavigationAnimationForView.Defaults.none
    override val push: NavigationAnimationForView = NavigationAnimationForView(
        enter = R.animator.fragment_animations_push_enter,
        exit = R.animator.fragment_animations_push_exit,
    )
    override val pushReturn: NavigationAnimationForView = NavigationAnimationForView(
        enter = R.animator.fragment_animations_push_return_enter,
        exit = R.animator.fragment_animations_push_return_exit,
    )
    override val present: NavigationAnimationForView = NavigationAnimationForView(
        enter = R.animator.fragment_animations_present_enter,
        exit = R.animator.fragment_animations_present_exit,
    )
}
