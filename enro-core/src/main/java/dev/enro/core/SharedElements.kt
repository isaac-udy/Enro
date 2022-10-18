package dev.enro.core

import androidx.annotation.IdRes

public data class EnroSharedElement(
    @IdRes val from: Int,
    @IdRes val opens: Int
) {
    public val transitionName: String by lazy {
        "EnroSharedElement_${from}_${opens}"
    }

    public companion object {
        internal const val ENRO_SHARED_ELEMENTS_FROM_KEY =
            "dev.enro.core.EnroSharedElements.ENRO_SHARED_ELEMENTS_FROM_KEY"
        internal const val ENRO_SHARED_ELEMENTS_OPENS_KEY =
            "dev.enro.core.EnroSharedElements.ENRO_SHARED_ELEMENTS_OPENS_KEY"
    }
}

public fun AnyOpenInstruction.hasSharedElements(): Boolean {
    return additionalData.containsKey(EnroSharedElement.ENRO_SHARED_ELEMENTS_FROM_KEY) &&
            additionalData.containsKey(EnroSharedElement.ENRO_SHARED_ELEMENTS_OPENS_KEY)
}

public fun AnyOpenInstruction.setSharedElements(list: List<EnroSharedElement>) {
    additionalData.putIntegerArrayList(
        EnroSharedElement.ENRO_SHARED_ELEMENTS_FROM_KEY, ArrayList(list.map { it.from })
    )
    additionalData.putIntegerArrayList(
        EnroSharedElement.ENRO_SHARED_ELEMENTS_OPENS_KEY, ArrayList(list.map { it.opens })
    )
}

public fun AnyOpenInstruction.getSharedElements(): List<EnroSharedElement> {
    val from = additionalData.getIntegerArrayList(EnroSharedElement.ENRO_SHARED_ELEMENTS_FROM_KEY)
        .orEmpty()
    val opens = additionalData.getIntegerArrayList(EnroSharedElement.ENRO_SHARED_ELEMENTS_OPENS_KEY)
        .orEmpty()
    return from.zip(opens).map { EnroSharedElement(it.first, it.second) }
}