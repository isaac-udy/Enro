package dev.enro

import kotlinx.parcelize.Parceler
import kotlin.reflect.KClass

public object KClassParceler : Parceler<KClass<*>> {
    override fun create(parcel: android.os.Parcel): KClass<*> {
        return parcel.readSerializable(KClassParceler::class.java.classLoader, Class::class.java)!!.kotlin
    }

    override fun KClass<*>.write(parcel: android.os.Parcel, flags: Int) {
        parcel.writeSerializable(this.java)
    }
}