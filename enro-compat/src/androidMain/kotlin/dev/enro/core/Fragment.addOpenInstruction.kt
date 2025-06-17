package dev.enro.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.enro.platform.putNavigationKeyInstance

public fun Fragment.addOpenInstruction(instruction: AnyOpenInstruction): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putNavigationKeyInstance(instruction)
    }
    return this
}