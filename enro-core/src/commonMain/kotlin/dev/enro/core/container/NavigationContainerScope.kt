package dev.enro.core.container

import dev.enro.animation.NavigationAnimationOverride
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.core.controller.EnroDependencyContainer
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.get
import dev.enro.core.controller.register

internal class NavigationContainerScope(
    owner: NavigationContainer,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
) : EnroDependencyScope {
    private val parentScope = owner.parentContainer()?.dependencyScope ?: owner.context.controller.dependencyScope

    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = parentScope,
        registration = {
            register<NavigationAnimationOverride> {
                val parentOverride = parentScope.get<NavigationAnimationOverride>()
                NavigationAnimationOverrideBuilder()
                    .apply(animations)
                    .build(parentOverride)
            }
        }
    )
}