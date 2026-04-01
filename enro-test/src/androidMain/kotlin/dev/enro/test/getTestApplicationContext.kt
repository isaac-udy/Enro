package dev.enro.test

import androidx.test.platform.app.InstrumentationRegistry

internal actual fun getTestApplicationContext(): Any? {
    return runCatching {
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    }.getOrNull()
}
