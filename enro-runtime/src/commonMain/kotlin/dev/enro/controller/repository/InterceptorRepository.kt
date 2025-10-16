package dev.enro.controller.repository

import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor

internal class InterceptorRepository(
    private val interceptors: MutableList<NavigationInterceptor> = mutableListOf()
) {
    var aggregateInterceptor = AggregateNavigationInterceptor(interceptors)

    fun addInterceptors(interceptors: List<NavigationInterceptor>) {
        this.interceptors.addAll(interceptors)
        aggregateInterceptor = AggregateNavigationInterceptor(this.interceptors)
    }

    fun addInterceptor(interceptor: NavigationInterceptor) {
        interceptors.add(interceptor)
        aggregateInterceptor = AggregateNavigationInterceptor(this.interceptors)
    }

    fun removeInterceptor(interceptor: NavigationInterceptor) {
        interceptors.remove(interceptor)
        aggregateInterceptor = AggregateNavigationInterceptor(this.interceptors)
    }
}