package dev.enro3

public data class NavigationTransition(
    public val currentBackstack: NavigationBackstack,
    public val targetBackstack: NavigationBackstack,
) {
    public val closed: List<NavigationKey.Instance<out NavigationKey>> by lazy {
        currentBackstack - targetBackstack
    }

    public val opened: List<NavigationKey.Instance<out NavigationKey>> by lazy {
        targetBackstack - currentBackstack
    }

    public val retained: Set<NavigationKey.Instance<out NavigationKey>> by lazy {
        currentBackstack intersect targetBackstack
    }
}