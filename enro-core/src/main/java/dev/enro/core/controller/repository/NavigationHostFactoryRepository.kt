package dev.enro.core.controller.repository

import android.app.Activity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationHostFactory
import dev.enro.core.NavigationInstruction
import dev.enro.core.hosts.ActivityHost
import dev.enro.core.hosts.DialogFragmentHost
import dev.enro.core.hosts.FragmentHost

internal class NavigationHostFactoryRepository(
    private val navigationBindingRepository: NavigationBindingRepository
) {
    private val producers = mutableMapOf<Class<*>, MutableList<NavigationHostFactory<*>>>()

    init {
        producers[Activity::class.java] = mutableListOf(ActivityHost())
        producers[Fragment::class.java] = mutableListOf(FragmentHost(navigationBindingRepository))
        producers[DialogFragment::class.java] = mutableListOf(DialogFragmentHost(navigationBindingRepository))
    }

    @Suppress("UNCHECKED_CAST")
    fun <HostType: Any> getNavigationHost(
        hostType: Class<HostType>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationHostFactory<HostType>? {
        return producers[hostType].orEmpty().firstOrNull { it.supports(instruction) }
                as? NavigationHostFactory<HostType>
    }
}