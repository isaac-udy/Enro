@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package dev.enro.core

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.enro.platform.putNavigationKeyInstance
import dev.enro.ui.destinations.putNavigationKeyInstance

public fun Bundle.addOpenInstruction(instruction: AnyOpenInstruction): Bundle {
    return putNavigationKeyInstance(instruction)
}

public fun Intent.addOpenInstruction(instruction: AnyOpenInstruction): Intent {
    return putNavigationKeyInstance(instruction)
}

public fun Fragment.addOpenInstruction(instruction: AnyOpenInstruction): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putNavigationKeyInstance(instruction)
    }
    return this
}