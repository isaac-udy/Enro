package dev.enro.tests.application.serialization

import android.os.Parcelable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

object Serialization {
    @Parcelize
    data class ParcelableNavigationKey(
        val name: String,
        val parcelableData: ParcelableData,
        val generalData: ParcelableAndSerializableData,
    ) : Parcelable, NavigationKey.SupportsPush

    @Serializable
    data class SerializableNavigationKey(
        val name: String,
        val serializableData: SerializableData,
        val generalData: ParcelableAndSerializableData,
    ) : NavigationKey.SupportsPush
}

@Parcelize
data class ParcelableData(
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val float: Float,
    val double: Double,
    val long: Long,
) : Parcelable

@Serializable
data class SerializableData(
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val float: Float,
    val double: Double,
    val long: Long,
) : NavigationKey.SupportsPush

@Parcelize
@Serializable
data class ParcelableAndSerializableData(
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val float: Float,
    val double: Double,
    val long: Long,
) : Parcelable, NavigationKey.SupportsPush

@NavigationDestination(Serialization.ParcelableNavigationKey::class)
@Composable
fun ParcelableNavigationKeyScreen() {
    // This is a placeholder function to ensure that the ParcelableNavigationKey class is
    // correctly picked up and added as a destination in the generated code.
    Text("Parcelable Navigation Key Screen")
}

@NavigationDestination(Serialization.SerializableNavigationKey::class)
@Composable
fun SerializableNavigationKeyScreen() {
    // This is a placeholder function to ensure that the SerializableNavigationKey class is
    // correctly picked up and added as a destination in the generated code.
    Text("Serializable Navigation Key Screen")
}