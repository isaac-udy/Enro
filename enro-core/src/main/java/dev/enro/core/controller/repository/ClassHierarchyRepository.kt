package dev.enro.core.controller.repository

import android.app.Activity
import androidx.fragment.app.Fragment
import java.util.concurrent.ConcurrentHashMap

internal class ClassHierarchyRepository {
    private val classHierarchy = ConcurrentHashMap<Class<*>, List<Class<*>>>()

    init {
        classHierarchy[Fragment::class.java] = listOf(Fragment::class.java, Any::class.java)
        classHierarchy[Activity::class.java] = listOf(Activity::class.java, Any::class.java)
    }

    private fun getClassHierarchy(cls: Class<*>): List<Class<*>> {
        val existing = classHierarchy[cls]
        if (existing != null) return existing
        val thisHierarchy = listOf(cls)
        val childHierarchy =
            if (cls.superclass != null) getClassHierarchy(cls.superclass) else emptyList()
        val next = thisHierarchy + childHierarchy
        classHierarchy[cls] = next
        return next
    }

    fun getClassHierarchyPairs(
        from: Class<*>,
        to: Class<*>
    ): List<Pair<Class<*>, Class<*>>> {
        val fromClasses = getClassHierarchy(from)
        val toClasses = getClassHierarchy(to)
        return cartesianProduct(fromClasses, toClasses) { f, t ->
            f to t
        }
    }
}

private fun <A, B, C> cartesianProduct(a: List<A>, b: List<B>, block: (A, B) -> C): List<C> =
    cartesianIndexes(a.size, b.size)
        .map {
            block(a[it.first], b[it.second])
        }

private fun cartesianIndexes(a: Int, b: Int): List<Pair<Int, Int>> = List(a * b) {
    val ai = it / b
    val bi = it % b
    ai to bi
}