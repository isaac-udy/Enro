package dev.enro.core

import android.os.Parcel
import kotlinx.parcelize.Parceler

public object NavigationDirectionParceler : Parceler<NavigationDirection> {
    override fun NavigationDirection.write(parcel: Parcel, flags: Int) {
        when(this) {
            NavigationDirection.Forward -> parcel.writeString("forward")
            NavigationDirection.Replace -> parcel.writeString("replace")
            NavigationDirection.Present -> parcel.writeString("present")
            NavigationDirection.Push -> parcel.writeString("push")
            NavigationDirection.ReplaceRoot -> parcel.writeString("replaceRoot")
        }
    }

    override fun create(parcel: Parcel): NavigationDirection {
        return when(parcel.readString()) {
            "forward" -> NavigationDirection.Forward
            "replace" -> NavigationDirection.Replace
            "present" -> NavigationDirection.Present
            "push" -> NavigationDirection.Push
            "replaceRoot" -> NavigationDirection.ReplaceRoot
            else -> throw IllegalArgumentException("Unknown NavigationDirection")
        }
    }
}