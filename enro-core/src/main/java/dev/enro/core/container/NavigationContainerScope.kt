package dev.enro.core.container

import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.core.controller.EnroDependencyContainer
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.get
import dev.enro.core.controller.register
import dev.enro.core.controller.usecase.GetNavigationAnimations
import dev.enro.core.parentContainer

internal class NavigationContainerScope(
    owner: NavigationContainer,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
) : EnroDependencyScope {
    private val parentScope = owner.parentContainer()?.dependencyScope ?: owner.context.controller.dependencyScope

    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = parentScope,
        registration = {
            register {
                val parentOverride = parentScope.get<GetNavigationAnimations>().navigationAnimationOverride
                GetNavigationAnimations(
                    controller = owner.context.controller,
                    navigationAnimationOverride = NavigationAnimationOverrideBuilder()
                        .apply(animations)
                        .build(parentOverride)
                )
            }
        }
    )
}