package dev.enro.extensions

import android.content.res.Resources
import android.util.TypedValue

internal fun Resources.Theme.getAttributeResourceId(attr: Int) = TypedValue().let {
    resolveAttribute(attr, it, true)
    it.resourceId
}

internal fun Resources.Theme.getNestedAttributeResourceId(vararg attrs: Int): Int? {
    val attribute = getAttributeResourceId(attrs.firstOrNull() ?: return null)
    return attrs.drop(1).fold(attribute) { currentAttr, nextAttr ->
        getStyledAttribute(currentAttr, nextAttr) ?: return null
    }
}

private fun Resources.Theme.getStyledAttribute(resId: Int, attr: Int): Int? {
    val attributes = obtainStyledAttributes(resId, intArrayOf(attr))
    val id = attributes.getResourceId(0, -1)
    if(id == -1) return null
    return id
}