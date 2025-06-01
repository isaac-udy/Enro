package dev.enro3.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class WrappedPrimitive

@Serializable
@SerialName("WrappedBoolean")
internal class WrappedBoolean(val value: Boolean) : WrappedPrimitive()

@Serializable
@SerialName("WrappedDouble")
internal class WrappedDouble(val value: Double) : WrappedPrimitive()

@Serializable
@SerialName("WrappedFloat")
internal class WrappedFloat(val value: Float) : WrappedPrimitive()

@Serializable
@SerialName("WrappedInt")
internal class WrappedInt(val value: Int) : WrappedPrimitive()

@Serializable
@SerialName("WrappedLong")
internal class WrappedLong(val value: Long) : WrappedPrimitive()

@Serializable
@SerialName("WrappedShort")
internal class WrappedShort(val value: Short) : WrappedPrimitive()

@Serializable
@SerialName("WrappedString")
internal class WrappedString(val value: String) : WrappedPrimitive()

@Serializable
@SerialName("WrappedByte")
internal class WrappedByte(val value: Byte) : WrappedPrimitive()

@Serializable
@SerialName("WrappedChar")
internal class WrappedChar(val value: Char) : WrappedPrimitive()

@Serializable
@SerialName("WrappedNull")
internal data object WrappedNull : WrappedPrimitive()
