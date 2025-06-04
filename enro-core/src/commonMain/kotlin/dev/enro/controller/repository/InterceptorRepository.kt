package dev.enro.controller.repository

import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor

internal class InterceptorRepository(
    private val interceptors: MutableList<NavigationInterceptor> = mutableListOf()
) : NavigationInterceptor by AggregateNavigationInterceptor(interceptors) {

    fun addInterceptors(interceptors: List<NavigationInterceptor>) {
        this.interceptors.addAll(interceptors)
    }

    fun addInterceptor(interceptor: NavigationInterceptor) {
        interceptors.add(interceptor)
    }

    fun removeInterceptor(interceptor: NavigationInterceptor) {
        interceptors.remove(interceptor)
    }
}