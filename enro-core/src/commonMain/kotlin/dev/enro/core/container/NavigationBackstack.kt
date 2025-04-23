package dev.enro.core.container

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.uuid.Uuid

@Serializable(with = NavigationBackstackSerializer::class)
public class NavigationBackstack(private val backstack: List<AnyOpenInstruction>) : List<AnyOpenInstruction> by backstack {
    public val active: AnyOpenInstruction? get() = lastOrNull()

    public val activePushed: AnyOpenInstruction? get() = lastOrNull { it.navigationDirection == NavigationDirection.Push }

    public val activePresented: AnyOpenInstruction? get() = takeWhile { it.navigationDirection != NavigationDirection.Push }
        .lastOrNull { it.navigationDirection == NavigationDirection.Push }

    internal val identity: Int = Uuid.random().hashCode()
//            return enroIdentityHashCode(backstack)
//        }
}

public object NavigationBackstackSerializer : KSerializer<NavigationBackstack> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NavigationBackstack") {
        element("active", String.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): NavigationBackstack {
        decoder.decodeStructure(descriptor) {
            val active = decodeStringElement(descriptor, 0)
            if (active.isNotEmpty()) {
                throw IllegalStateException("NavigationBackstack cannot be deserialized with an active instruction")
            }
        }
        return emptyBackstack()
    }

    override fun serialize(encoder: Encoder, value: NavigationBackstack) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.active?.toString().orEmpty())
        }
    }
}

public fun emptyBackstack() : NavigationBackstack = NavigationBackstack(emptyList())
public fun backstackOf(vararg instructions: AnyOpenInstruction) : NavigationBackstack = NavigationBackstack(instructions.toList())
public fun backstackOfNotNull(vararg instructions: AnyOpenInstruction?) : NavigationBackstack = NavigationBackstack(instructions.filterNotNull())

public fun List<AnyOpenInstruction>.toBackstack() : NavigationBackstack {
    if (this is NavigationBackstack) return this
    return NavigationBackstack(this)
}

internal fun merge(
    oldBackstack: List<AnyOpenInstruction>,
    newBackstack: List<AnyOpenInstruction>,
): List<AnyOpenInstruction> {
    val results = mutableMapOf<Int, MutableList<AnyOpenInstruction>>()
    val indexes = mutableMapOf<AnyOpenInstruction, Int>()
    newBackstack.forEachIndexed { index, it ->
        results[index] = mutableListOf(it)
        indexes[it] = index
    }
    results[-1] = mutableListOf()

    var oldIndex = -1
    oldBackstack.forEach { oldItem ->
        oldIndex = maxOf(indexes[oldItem] ?: -1, oldIndex)
        results[oldIndex].let {
            if(it == null) return@let
            if(it.firstOrNull() == oldItem) return@let
            it.add(oldItem)
        }
    }

    return results.entries
        .sortedBy { it.key }
        .flatMap { it.value }
}
