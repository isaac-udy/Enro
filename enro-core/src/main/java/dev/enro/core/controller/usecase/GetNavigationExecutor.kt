package dev.enro.core.controller.usecase

import dev.enro.core.*
import dev.enro.core.container.DefaultContainerExecutor
import dev.enro.core.controller.repository.ClassHierarchyRepository
import dev.enro.core.controller.repository.ExecutorRepository

internal class GetNavigationExecutor(
    private val executorRepository: ExecutorRepository,
    private val classHierarchyRepository: ClassHierarchyRepository,
) {
    operator fun invoke(types: Pair<Class<out Any>, Class<out Any>>): NavigationExecutor<Any, Any, NavigationKey> {
        return classHierarchyRepository.getClassHierarchyPairs(types.first, types.second)
            .asSequence()
            .mapNotNull {
                executorRepository.getExecutor(it.first to it.second)
            }
            .firstOrNull() ?: DefaultContainerExecutor
    }
}

internal fun GetNavigationExecutor.forOpening(instruction: AnyOpenInstruction) =
    invoke(instruction.internal.openedByType to instruction.internal.openingType)

internal fun GetNavigationExecutor.forClosing(navigationContext: NavigationContext<*>) =
    invoke(navigationContext.getNavigationHandle().instruction.internal.openedByType to navigationContext.contextReference::class.java)