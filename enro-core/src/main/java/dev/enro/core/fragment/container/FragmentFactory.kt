package dev.enro.core.fragment.container

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContext
import dev.enro.core.addOpenInstruction

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

        val hostedBinding = parentContext.controller.bindingForKeyType(instruction.navigationKey::class)
            ?: throw EnroException.MissingNavigationBinding(instruction.navigationKey)

        return fragmentManager.fragmentFactory.instantiate(
            hostedBinding.destinationType.java.classLoader!!,
            hostedBinding.destinationType.java.name
        ).apply {
            arguments = Bundle().addOpenInstruction(instruction)
        }
    }
}