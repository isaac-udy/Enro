package dev.enro.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json

public fun Fragment.addOpenInstruction(instruction: AnyOpenInstruction): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putString(OPEN_ARG, Json.encodeToString(instruction.internal))
    }
    return this
}