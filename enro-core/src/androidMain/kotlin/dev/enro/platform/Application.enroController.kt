package dev.enro.platform

import android.app.Application
import dev.enro.EnroController

public val Application.enroController: EnroController get() {
    val instance = EnroController.instance
    val reference = instance?.platformReference
    require(this == reference) {
        "The currently installed EnroController $instance is not installed with an Application reference. The current reference is $reference."
    }
    return instance
}