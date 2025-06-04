package dev.enro.interceptor


public operator fun NavigationInterceptor.plus(other: NavigationInterceptor): NavigationInterceptor {
    return when {
        this is NoOpNavigationInterceptor -> other
        other is NoOpNavigationInterceptor -> this
        this is AggregateNavigationInterceptor -> this + other
        else -> AggregateNavigationInterceptor(listOf(this, other))
    }
}
