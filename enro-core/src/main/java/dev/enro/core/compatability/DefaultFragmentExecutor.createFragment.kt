package dev.enro.core.compatability

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.addOpenInstruction
import dev.enro.core.fragment.DefaultFragmentExecutor

@Deprecated("Please create a fragment and use `fragment.arguments = Bundle().addOpenInstruction(instruction)` yourself")
public fun DefaultFragmentExecutor.createFragment(
    fragmentManager: FragmentManager,
    binding: NavigationBinding<*, *>,
    instruction: AnyOpenInstruction
): Fragment {
    val fragment = fragmentManager.fragmentFactory.instantiate(
        binding.destinationType.java.classLoader!!,
        binding.destinationType.java.name
    )

    fragment.arguments = Bundle()
        .addOpenInstruction(instruction)

    return fragment
}