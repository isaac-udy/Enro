package dev.enro.core.compose

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

internal val localActivity @Composable get() = LocalContext.current.let {
    var ctx = it
    while (ctx is ContextWrapper) {
        if (ctx is Activity) {
            return@let ctx
        }
        ctx = ctx.baseContext
    }
    throw IllegalStateException("Could not find Activity up from $it")
}