package dev.enrolegacy.extensions

import androidx.savedstate.SavedState


internal actual fun Any.isSaveable(): Boolean {
    return AcceptableClasses.any { it.isInstance(this) }
}

/**
 * Copied from androidx.compose.ui.platform.DisposableSaveableStateRegistry
 *
 * Contains Classes which can be stored inside [Bundle].
 *
 * Some of the classes are not added separately because:
 *
 * This classes implement Serializable:
 * - Arrays (DoubleArray, BooleanArray, IntArray, LongArray, ByteArray, FloatArray, ShortArray,
 * CharArray, Array<Parcelable, Array<String>)
 * - ArrayList
 * - Primitives (Boolean, Int, Long, Double, Float, Byte, Short, Char) will be boxed when casted
 * to Any, and all the boxed classes implements Serializable.
 * This class implements Parcelable:
 * - Bundle
 *
 * Note: it is simplified copy of the array from SavedStateHandle (lifecycle-viewmodel-savedstate).
 */
private val AcceptableClasses = arrayOf(
    String::class,
    Number::class,
    SavedState::class,
)