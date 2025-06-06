package dev.enro.platform

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import dev.enro.context.RootContext

public val Activity.navigationContext: RootContext
    get() {
        if (this !is ComponentActivity) {
            error("Cannot retrieve navigation context from Activity that does not extend ComponentActivity")
        }
        return viewModels<ActivityContextHolder>().value.rootContext
            ?: error("Navigation context is not available for this activity")
    }