// This class exists in an incorrect directory to isolate deprecated compatibility functionality
@file:Suppress("PackageDirectoryMismatch")
package dev.enro.core.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.addOpenInstruction

public object DefaultFragmentExecutor {
    @Deprecated("Please create a fragment and use `fragment.arguments = Bundle().addOpenInstruction(instruction)` yourself")
    public fun createFragment(
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
}