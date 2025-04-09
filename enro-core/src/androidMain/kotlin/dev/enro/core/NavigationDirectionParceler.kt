package dev.enro.core

import android.os.Parcel
import kotlinx.parcelize.Parceler

public object NavigationDirectionParceler : Parceler<NavigationDirection> {
    override fun NavigationDirection.write(parcel: Parcel, flags: Int) {
        when(this) {
            NavigationDirection.Present -> parcel.writeString("present")
            NavigationDirection.Push -> parcel.writeString("push")
        }
    }

    override fun create(parcel: Parcel): NavigationDirection {
        return when(parcel.readString()) {
            "present" -> NavigationDirection.Present
            "push" -> NavigationDirection.Push
            else -> throw IllegalArgumentException("Unknown NavigationDirection")
        }
    }
}