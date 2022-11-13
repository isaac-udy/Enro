package dev.enro.fragment.container

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*

internal object FragmentFactory {
    fun createFragment(
        parentContext: NavigationContext<*>,
        instruction: AnyOpenInstruction
    ): Fragment {
        val fragmentManager = when (parentContext.contextReference) {
            is FragmentActivity -> parentContext.contextReference.supportFragmentManager
            is Fragment -> parentContext.contextReference.childFragmentManager
            else -> throw IllegalStateException()
        }

        val navigationHostFactory = parentContext.controller.dependencyScope.get<NavigationHostFactory>()

        val hostedInstruction = navigationHostFactory.createHostFor<Fragment>(instruction)
        val hostedBinding = parentContext.controller.bindingForKeyType(hostedInstruction.navigationKey::class) as NavigationBinding<*, *>

        return fragmentManager.fragmentFactory.instantiate(
            hostedBinding.destinationType.java.classLoader!!,
            hostedBinding.destinationType.java.name
        ).apply {
            arguments = Bundle().addOpenInstruction(hostedInstruction)
        }
    }
}