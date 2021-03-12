package dev.enro.core.internal

import android.content.res.Resources
import android.util.TypedValue

internal fun Resources.Theme.getAttributeResourceId(attr: Int) = TypedValue().let {
    resolveAttribute(attr, it, true)
    it.resourceId
}