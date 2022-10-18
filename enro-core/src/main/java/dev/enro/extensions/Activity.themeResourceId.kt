package dev.enro.extensions

import android.app.Activity

internal val Activity.themeResourceId: Int
    get() = packageManager.getActivityInfo(componentName, 0).themeResource