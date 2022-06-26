package dev.enro.core.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.*
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.compose.ComposeFragmentHostKey
import dev.enro.core.compose.HiltComposeFragmentHostKey

internal object FragmentFactory {

    private val generatedComponentManagerHolderClass = kotlin.runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()

    fun createFragment(
        parentContext: NavigationContext<*>,
        navigator: Navigator<*, *>,
        instruction: AnyOpenInstruction
    ): Fragment {
        val fragmentManager = when(parentContext.contextReference) {
            is FragmentActivity -> parentContext.contextReference.supportFragmentManager
            is Fragment -> parentContext.contextReference.childFragmentManager
            else -> throw IllegalStateException()
        }
        when (navigator) {
            is FragmentNavigator<*, *> -> {
                val fragment = fragmentManager.fragmentFactory.instantiate(
                    navigator.contextType.java.classLoader!!,
                    navigator.contextType.java.name
                )

                fragment.arguments = Bundle()
                    .addOpenInstruction(instruction)

                return fragment
            }
            is ComposableNavigator<*, *> -> {
                val isHiltContext = if(generatedComponentManagerHolderClass != null) {
                    parentContext.contextReference is GeneratedComponentManagerHolder
                } else false

                val wrappedKey = when {
                    isHiltContext -> HiltComposeFragmentHostKey(instruction, isRoot = false)
                    else -> ComposeFragmentHostKey(instruction, isRoot = false)
                }

                return createFragment(
                    parentContext = parentContext,
                    navigator = parentContext.controller.navigatorForKeyType(wrappedKey::class) as Navigator<*, *>,
                    instruction = NavigationInstruction.Open.OpenInternal(
                        instructionId = instruction.instructionId,
                        navigationDirection = instruction.navigationDirection,
                        navigationKey = wrappedKey
                    )
                )
            }
            else -> throw IllegalStateException()
        }
    }
}