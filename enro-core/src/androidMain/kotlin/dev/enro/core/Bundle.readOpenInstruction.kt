package dev.enro.core

import android.os.Bundle
import kotlinx.serialization.json.Json

public fun Bundle.readOpenInstruction(): AnyOpenInstruction? {
    val jsonString = getString(OPEN_ARG) ?: return null
    return Json.decodeFromString<AnyOpenInstruction>(jsonString)
}