package nav.enro.core

enum class NavigationDirection {
    FORWARD,
    REPLACE,
    REPLACE_ROOT
}

sealed class NavigationInstruction {
    class Open(
        val navigationDirection: NavigationDirection,
        val navigationKey: NavigationKey,
        val children: List<NavigationKey> = emptyList()
    ) : NavigationInstruction()

    object Close : NavigationInstruction()
}
