package dev.enro.core

import android.os.Parcel
import kotlinx.parcelize.Parceler
import kotlinx.serialization.json.Json

public object NavigationInstructionOpenParceler : Parceler<NavigationInstruction.Open<*>> {
    override fun NavigationInstruction.Open<*>.write(parcel: Parcel, flags: Int) {
        parcel.writeString(
            Json.encodeToString(this),
        )
    }

    override fun create(parcel: Parcel): NavigationInstruction.Open<*> {
        val data = parcel.readString() ?: error("Failed to read NavigationInstruction.Open from Parcel - readString returned null")
        return Json.decodeFromString(data)
    }
}