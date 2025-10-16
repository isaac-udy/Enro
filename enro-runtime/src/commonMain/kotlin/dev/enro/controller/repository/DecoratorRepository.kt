package dev.enro.controller.repository

import dev.enro.NavigationKey
import dev.enro.ui.decorators.NavigationDestinationDecorator

internal class DecoratorRepository {
    private val decoratorBuilders = mutableListOf<() -> NavigationDestinationDecorator<NavigationKey>>()

    fun addDecorator(
        decorator: () -> NavigationDestinationDecorator<NavigationKey>,
    ) {
        decoratorBuilders.add(decorator)
    }

    fun addDecorators(
        decorators: List<() -> NavigationDestinationDecorator<NavigationKey>>
    ) {
        decoratorBuilders.addAll(decorators)
    }

    fun getDecorators() : List<NavigationDestinationDecorator<NavigationKey>> {
        return decoratorBuilders.map { builder -> builder() }
    }
}