package nav.enro.core

import android.content.res.Resources
import android.util.TypedValue

internal fun Resources.Theme.getAttributeResourceId(attr: Int) = TypedValue().let {
    resolveAttribute(attr, it, true)
    it.resourceId
}