package dev.enro.animation

public interface NavigationAnimation {
    public data class Defaults<T : NavigationAnimation>(
        // TODO can we remove "none" from defaults somehow?
        public val none: T,
        /**
         * The animation to use when a Push instruction is added to the backstack
         */
        public val push: T,
        /**
         * The animation to use when a Push instruction is removed from the backstack,
         * and the backstack is "returning" to a previous Push instruction
         */
        public val pushReturn: T = push,
        /**
         * The animation to use when a Present instruction is added to the backstack
         */
        public val present: T,
        /**
         * The animation to use when a Present instruction is removed from the backstack
         */
        public val presentReturn: T = present,
    )
}

