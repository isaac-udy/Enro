package dev.enro.platform

import android.app.Application
import dev.enro.EnroController

internal val EnroController.application: Application get() {
    val instance = EnroController.instance
    val reference = platformReference
    require(this == instance) {
        "The EnroController $this is not the same as the currently installed EnroController $instance"
    }
    require(reference is Application) {
        "The EnroController $this is not installed on an Application"
    }
    return reference
}