package dev.enro.extensions

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal val localActivity @Composable get() = LocalContext.current.let {
    remember(it) {
        var ctx = it
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                break
            }
            ctx = ctx.baseContext
        }

        ctx as? Activity
            ?: throw IllegalStateException("Could not find Activity up from $it")
    }
}