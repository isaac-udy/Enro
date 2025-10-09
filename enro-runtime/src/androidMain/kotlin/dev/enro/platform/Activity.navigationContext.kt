package dev.enro.platform

import android.app.Activity
import androidx.activity.ComponentActivity
import dev.enro.context.RootContext
import dev.enro.handle.RootNavigationHandle
import dev.enro.handle.getNavigationHandleHolder

public val Activity.navigationContext: RootContext
    get() {
        if (this !is ComponentActivity) {
            error("Cannot retrieve navigation context from Activity that does not extend ComponentActivity")
        }
        val navigationHandle = getNavigationHandleHolder().navigationHandle
        require(navigationHandle is RootNavigationHandle) {
            "Expected $this to have a RootNavigationHandle, but found $navigationHandle"
        }
        return navigationHandle.context
            ?: error("Navigation context is not available for this activity")
    }