package dev.enro.extensions

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat

internal inline fun <reified T : Parcelable?> Bundle.getParcelableListCompat(key: String): List<T>? =
    BundleCompat.getParcelableArrayList(this, key, T::class.java)