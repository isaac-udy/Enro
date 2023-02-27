package dev.enro.extensions

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

internal inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}