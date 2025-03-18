package dev.enro.extensions

import android.os.Bundle
import androidx.core.os.BundleCompat

internal inline fun <reified T> Bundle.getParcelableListCompat(key: String): List<T>? =
    BundleCompat.getParcelableArrayList(this, key, T::class.java)