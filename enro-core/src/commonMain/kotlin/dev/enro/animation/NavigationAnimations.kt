package dev.enro.animation

public interface NavigationAnimation {
    public interface Defaults<T: NavigationAnimation> {
        public val none: T
        public val push: T
        public val pushReturn: T
        public val present: T
        public val presentReturn: T
    }
}

