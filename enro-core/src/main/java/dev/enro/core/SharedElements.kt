package dev.enro.core

import android.os.Parcelable
import android.transition.AutoTransition
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

data class EnroSharedElement(
    @IdRes val from: Int,
    @IdRes val opens: Int
)  {
    val transitionName by lazy {
        "EnroSharedElement_${from}_${opens}"
    }

    companion object {
        internal const val ENRO_SHARED_ELEMENTS_FROM_KEY = "dev.enro.core.EnroSharedElements.ENRO_SHARED_ELEMENTS_FROM_KEY"
        internal const val ENRO_SHARED_ELEMENTS_OPENS_KEY = "dev.enro.core.EnroSharedElements.ENRO_SHARED_ELEMENTS_OPENS_KEY"
    }
}

fun NavigationInstruction.Open.hasSharedElements(): Boolean {
    return additionalData.containsKey(EnroSharedElement.ENRO_SHARED_ELEMENTS_FROM_KEY) &&
            additionalData.containsKey(EnroSharedElement.ENRO_SHARED_ELEMENTS_OPENS_KEY)
}

fun NavigationInstruction.Open.setSharedElements(list: List<EnroSharedElement>) {
    additionalData.putIntegerArrayList(
        EnroSharedElement.ENRO_SHARED_ELEMENTS_FROM_KEY, ArrayList(list.map { it.from })
    )
    additionalData.putIntegerArrayList(
        EnroSharedElement.ENRO_SHARED_ELEMENTS_OPENS_KEY, ArrayList(list.map { it.opens })
    )
}

fun NavigationInstruction.Open.getSharedElements(): List<EnroSharedElement> {
    val from = additionalData.getIntegerArrayList(EnroSharedElement.ENRO_SHARED_ELEMENTS_FROM_KEY).orEmpty()
    val opens = additionalData.getIntegerArrayList(EnroSharedElement.ENRO_SHARED_ELEMENTS_OPENS_KEY).orEmpty()
    return from.zip(opens).map { EnroSharedElement(it.first, it.second) }
}