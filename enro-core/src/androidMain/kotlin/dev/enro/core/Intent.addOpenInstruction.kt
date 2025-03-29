package dev.enro.core

import android.content.Intent
import kotlinx.serialization.json.Json

public fun Intent.addOpenInstruction(instruction: AnyOpenInstruction): Intent {
    putExtra(OPEN_ARG, Json.encodeToString(instruction.internal))
    return this
}