package dev.enro.core

import android.os.Bundle
import kotlinx.serialization.json.Json

public fun Bundle.addOpenInstruction(instruction: AnyOpenInstruction): Bundle {
    putString(OPEN_ARG, Json.encodeToString(instruction))
    return this
}