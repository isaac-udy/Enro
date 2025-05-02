package dev.enro.animation

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.AttrRes
import dev.enro.extensions.getAttributeResourceId
import dev.enro.extensions.getNestedAttributeResourceId

public class NavigationAnimationForView internal constructor(
    public val enter: Type,
    public val exit: Type,
) : NavigationAnimation {

    public fun enterAsResource(context: Context): Int {
        return when (val enter = enter) {
            is Type.Resource -> when {
                enter.isAttr(context) -> context.theme.obtainStyledAttributes(
                    intArrayOf(enter.id)
                ).getResourceId(0, 0)

                else -> enter.id
            }

            is Type.Theme -> enter.id(context.theme)
        }
    }

    public fun exitAsResource(context: Context): Int {
        return when (val exit = exit) {
            is Type.Resource -> when {
                exit.isAttr(context) -> context.theme.obtainStyledAttributes(
                    intArrayOf(exit.id)
                ).getResourceId(0, 0)

                else -> exit.id
            }

            is Type.Theme -> exit.id(context.theme)
        }
    }

    public constructor(
        @AnimatorRes
        @AnimRes
        @AttrRes
        enter: Int,
        @AnimatorRes
        @AnimRes
        @AttrRes
        exit: Int,
    ) : this(
        enter = Type.Resource(enter),
        exit = Type.Resource(exit),
    )

    public constructor(
        enterFromTheme: (Resources.Theme) -> Int,
        exitFromTheme: (Resources.Theme) -> Int,
    ) : this(
        enter = Type.Theme(enterFromTheme),
        exit = Type.Theme(exitFromTheme),
    )

    public sealed interface Type {
        public data class Resource(
            public val id: Int
        ) : Type {
            public fun isAnim(context: Context): Boolean = runCatching {
                if (id == 0) return@runCatching false
                context.resources.getResourceTypeName(id) == "anim"
            }.getOrDefault(false)

            public fun isAnimator(context: Context): Boolean = runCatching {
                if (id == 0) return@runCatching false
                context.resources.getResourceTypeName(id) == "animator"
            }.getOrDefault(false)

            public fun isAttr(context: Context): Boolean = runCatching {
                if (id == 0) return@runCatching false
                context.resources.getResourceTypeName(id) == "attr"
            }.getOrDefault(false)
        }

        public data class Theme(
            public val id: (Resources.Theme) -> Int,
        ) : Type
    }

    public companion object {
        public val none: NavigationAnimationForView = NavigationAnimationForView(
            enter = Type.Resource(0),
            exit = Type.Resource(0),
        )
    }

    public object Defaults : NavigationAnimation.Defaults<NavigationAnimationForView> {
        public override val none: NavigationAnimationForView = NavigationAnimationForView(
            enter = 0,
            exit = dev.enro.core.R.anim.enro_no_op_exit_animation,
        )

        public val noneReturn: NavigationAnimationForView = NavigationAnimationForView(
            enter = 0,
            exit = 0,
        )

        public override val push: NavigationAnimationForView = NavigationAnimationForView(
            enter = android.R.attr.activityOpenEnterAnimation,
            exit = android.R.attr.activityOpenExitAnimation
        )
        public override val pushReturn: NavigationAnimationForView = NavigationAnimationForView(
            enter = android.R.attr.activityCloseEnterAnimation,
            exit = android.R.attr.activityCloseExitAnimation
        )

        public override val present: NavigationAnimationForView = NavigationAnimationForView(
            enterFromTheme = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                }
            },
            exitFromTheme = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowExitAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenExitAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenExitAnimation)
                }
            }
        )

        public override val presentReturn: NavigationAnimationForView = NavigationAnimationForView(
            enterFromTheme = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                }
            },
            exitFromTheme = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowExitAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenExitAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenExitAnimation)
                }
            },
        )
    }
}

