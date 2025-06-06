package dev.enro.controller.repository

import dev.enro.NavigationContext
import dev.enro.NavigationOperation
import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor

internal class InterceptorRepository(
    private val interceptors: MutableList<NavigationInterceptor> = mutableListOf()
) : NavigationInterceptor {

    fun addInterceptors(interceptors: List<NavigationInterceptor>) {
        this.interceptors.addAll(interceptors)
    }

    fun addInterceptor(interceptor: NavigationInterceptor) {
        interceptors.add(interceptor)
    }

    fun removeInterceptor(interceptor: NavigationInterceptor) {
        interceptors.remove(interceptor)
    }

    override fun intercept(
        context: NavigationContext,
        operation: NavigationOperation,
    ): NavigationOperation? {
        return AggregateNavigationInterceptor(interceptors)
            .intercept(
                context = context,
                operation = operation,
            )
    }
}