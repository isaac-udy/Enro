package dev.enro.core.controller.usecase

import dev.enro.core.NavigationExecutor
import dev.enro.core.NavigationKey
import dev.enro.core.controller.repository.ClassHierarchyRepository
import dev.enro.core.controller.repository.ExecutorRepository
import dev.enro.core.usecase.GetNavigationExecutor

internal class GetNavigationExecutorImpl(
    private val executorRepository: ExecutorRepository,
    private val classHierarchyRepository: ClassHierarchyRepository,
) : GetNavigationExecutor {
    override operator fun invoke(types: Pair<Class<out Any>, Class<out Any>>): NavigationExecutor<Any, Any, NavigationKey> {
        return classHierarchyRepository.getClassHierarchyPairs(types.first, types.second)
            .asSequence()
            .mapNotNull {
                executorRepository.getExecutor(it.first to it.second)
            }
            .first()
    }
}