package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_home.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.forward
import nav.enro.core.getNavigationHandle


@Parcelize
class Home : NavigationKey

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
        launchExample.setOnClickListener {
            getNavigationHandle()
                .forward(SimpleExampleKey("Start", "Home", listOf("Home")))
        }
    }
}