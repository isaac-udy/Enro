package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.getNavigationHandle
import dev.enro.core.push
import dev.enro.example.databinding.FragmentHomeBinding
import kotlinx.parcelize.Parcelize


@Parcelize
object Home : NavigationKey.SupportsPush

@NavigationDestination(Home::class)
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentHomeBinding.bind(view).apply {
            launchExample.setOnClickListener {
                getNavigationHandle().push(
                    SimpleExampleFragmentKey(
                        name = "Start",
                        launchedFrom = "Home",
                        backstack = listOf("Home")
                    )
                )
            }
        }
    }
}