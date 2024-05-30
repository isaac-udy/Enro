package dev.enro.core.result.flows

@PublishedApi
internal fun List<Any?>.hashForDependsOn(): Long = fold(0L) { result, it ->
    val hash = if (it is List<*>) it.hashForDependsOn() else it.hashCode().toLong()
    return@fold 31L * result + hash
}