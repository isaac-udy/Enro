package dev.enro

public class NavigationTransition(
    public val from: NavigationBackstack,
    public val to: NavigationBackstack,
) {
    public val closed: List<NavigationKey.Instance<out NavigationKey>> by lazy {
        from - to
    }
}