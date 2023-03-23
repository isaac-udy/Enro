package dev.enro.core

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.enro.core.container.originalNavigationDirection
import dev.enro.extensions.ResourceAnimatedVisibility
import dev.enro.extensions.getAttributeResourceId
import dev.enro.extensions.getNestedAttributeResourceId

@Deprecated("Please use NavigationAnimation")
public typealias AnimationPair = NavigationAnimation

public sealed interface NavigationAnimation {
    public sealed interface ForView : NavigationAnimation
    public sealed interface Enter : NavigationAnimation
    public sealed interface Exit : NavigationAnimation

    public data class Resource(
        public val id: Int
    ) : ForView, Enter, Exit {
        public fun isAnim(context: Context): Boolean = runCatching {
            if (id == 0) return@runCatching false
            context.resources.getResourceTypeName(id) == "anim"
        }.getOrDefault(false)

        public fun isAnimator(context: Context): Boolean = runCatching {
            if (id == 0) return@runCatching false
            context.resources.getResourceTypeName(id) == "animator"
        }.getOrDefault(false)
    }

    public data class Attr(
        public val attr: Int,
    ) : ForView, Enter, Exit

    public data class Theme(
        public val id: (Resources.Theme) -> Int,
    ) : ForView, Enter, Exit

    public sealed class Composable : NavigationAnimation, Enter, Exit {
        internal abstract val forView: ForView

        @androidx.compose.runtime.Composable
        public abstract fun Animate(
            visible: Transition<Boolean>,
            content: @androidx.compose.runtime.Composable () -> Unit
        )

        public companion object {
            public operator fun invoke(
                enter: EnterTransition,
                exit: ExitTransition,
                forView: ForView = DefaultAnimations.ForView.noneEnter,
            ): Composable = EnterExit(enter, exit, forView)

            public operator fun invoke(
                enter: EnterTransition,
                forView: ForView = DefaultAnimations.ForView.noneEnter,
            ): Enter = EnterExit(enter, ExitTransition.None, forView)

            public operator fun invoke(
                exit: ExitTransition,
                forView: ForView = DefaultAnimations.ForView.noneEnter,
            ): Enter = EnterExit(EnterTransition.None, exit, forView)

            public operator fun invoke(
                forView: ForView,
            ): Composable = FromView(forView)
        }

        @Immutable
        internal data class EnterExit(
            val enter: EnterTransition = EnterTransition.None,
            val exit: ExitTransition = ExitTransition.None,
            override val forView: ForView = DefaultAnimations.ForView.noneEnter,
        ) : Composable(), Enter, Exit {
            @OptIn(ExperimentalAnimationApi::class)
            @androidx.compose.runtime.Composable
            override fun Animate(visible: Transition<Boolean>, content: @androidx.compose.runtime.Composable () -> Unit) {
                visible.AnimatedVisibility(
                    visible = { it },
                    enter = enter,
                    exit = exit,
                    modifier = Modifier.fillMaxSize()
                ) {
                    content()
                }
            }
        }

        @Immutable
        internal data class FromView(
            override val forView: ForView
        ) : Composable(), Enter, Exit {
            @androidx.compose.runtime.Composable
            override fun Animate(visible: Transition<Boolean>, content: @androidx.compose.runtime.Composable () -> Unit) {
                val context = LocalContext.current
                val resourceAnimation = remember(forView) { forView.asResource(context.theme) }
                visible.ResourceAnimatedVisibility(
                    visible = { it },
                    enter = resourceAnimation.id,
                    exit = resourceAnimation.id,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    content()
                }
            }
        }

        @Immutable
        internal object NoAnimation : Composable(), Enter, Exit {
            override val forView: ForView = Resource(0)

            @androidx.compose.runtime.Composable
            override fun Animate(visible: Transition<Boolean>, content: @androidx.compose.runtime.Composable () -> Unit) {
                if (!visible.currentState && !visible.targetState) return
                content()
            }
        }
    }

    public fun asResource(theme: Resources.Theme): Resource = when (this) {
        is Resource -> this
        is Attr -> Resource(
            theme.getAttributeResourceId(attr),
        )
        is Theme -> Resource(
            id(theme),
        )
        is Composable -> forView.asResource(theme)
    }

    public fun asComposable(): Composable {
        return when (this) {
            is ForView -> Composable(forView = this)
            is Composable -> this
        }
    }
}

public data class NavigationAnimationTransition(
    public val entering: NavigationAnimation,
    public val exiting: NavigationAnimation,
)

public object DefaultAnimations {
    public val none: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.noneEnter,
        exiting = ForView.noneExit,
    )

    public val noOp: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = NavigationAnimation.Composable(
            forView = NavigationAnimation.Resource(0),
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        ),
        exiting = NavigationAnimation.Composable(
            forView = NavigationAnimation.Resource(0),
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        )
    )

    public fun opening(exiting: AnyOpenInstruction?, entering: AnyOpenInstruction): NavigationAnimationTransition {
        if (entering.originalNavigationDirection() == NavigationDirection.ReplaceRoot) {
            return NavigationAnimationTransition(
                entering = ForView.replaceRootEnter,
                exiting = ForView.replaceRootExit
            )
        }

        val enteringAnimation = when (entering.originalNavigationDirection()) {
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushEnter
            else -> ForView.presentEnter
        }

        val exitingAnimation = when (exiting?.navigationDirection) {
            null -> ForView.noneExit
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushExit
            else -> ForView.presentExit
        }

        return NavigationAnimationTransition(
            entering = enteringAnimation,
            exiting = exitingAnimation
        )
    }

    public fun closing(exiting: AnyOpenInstruction, entering: AnyOpenInstruction?): NavigationAnimationTransition {
        val enteringAnimation = when (entering?.navigationDirection) {
            null -> ForView.noneCloseExit
            NavigationDirection.ReplaceRoot -> when (exiting.originalNavigationDirection()) {
                NavigationDirection.Present -> ForView.presentCloseEnter
                else -> ForView.pushCloseEnter
            }
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushCloseEnter
            else -> ForView.presentCloseEnter
        }

        val exitingAnimation = when (exiting.navigationDirection) {
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushCloseExit
            else -> ForView.presentCloseExit
        }

        return NavigationAnimationTransition(
            entering = enteringAnimation,
            exiting = exitingAnimation
        )
    }

    public object ForView {
        public val pushEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.activityOpenEnterAnimation,
        )

        public val pushExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.activityOpenExitAnimation
        )

        public val pushCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.activityCloseEnterAnimation,
        )

        public val pushCloseExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.activityCloseExitAnimation
        )

        public val presentEnter: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                }
            }
        )

        public val presentExit: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
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

        public val presentCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                }
            }
        )

        public val presentCloseExit: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
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

        public val replaceRootEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.taskOpenEnterAnimation,
        )

        public val replaceRootExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.taskOpenExitAnimation
        )

        public val noneEnter: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )

        public val noneExit: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = R.anim.enro_no_op_exit_animation
        )

        public val noneCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )

        public val noneCloseExit: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )
    }
}

internal data class OpeningTransition(
    val priority: Int,
    val transition: (exiting: AnyOpenInstruction?, entering: AnyOpenInstruction) -> NavigationAnimationTransition?
)

internal data class ClosingTransition(
    val priority: Int,
    val transition: (exiting: AnyOpenInstruction, entering: AnyOpenInstruction?) -> NavigationAnimationTransition?
)

internal data class NavigationAnimationOverride(
    val parent: NavigationAnimationOverride?,
    val opening: List<OpeningTransition>,
    val closing: List<ClosingTransition>,
)

public class NavigationAnimationOverrideBuilder {
    private val opening = mutableListOf<OpeningTransition>()
    private val closing = mutableListOf<ClosingTransition>()

    public fun addOpeningTransition(priority: Int, transition: (exiting: AnyOpenInstruction?, entering: AnyOpenInstruction) -> NavigationAnimationTransition?) {
        opening.add(OpeningTransition(priority, transition))
    }

    public fun addClosingTransition(priority: Int, transition: (exiting: AnyOpenInstruction, entering: AnyOpenInstruction?) -> NavigationAnimationTransition?) {
        closing.add(ClosingTransition(priority, transition))
    }

    public fun direction(
        direction: NavigationDirection,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }
    }

    public fun direction(
        direction: NavigationDirection,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter,
        returnExiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        addClosingTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction == null) return@addClosingTransition null
            if (enteringInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    public inline fun <reified Key : NavigationKey> transitionTo(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(PARTIAL_KEY_PRIORITY) { _, enteringInstruction ->
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Key) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }
    }

    public inline fun <reified Key : NavigationKey> transitionTo(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter,
        returnExiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(PARTIAL_KEY_PRIORITY) { _, enteringInstruction ->
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Key) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        addClosingTransition(PARTIAL_KEY_PRIORITY) { exitingInstruction, _ ->
            if (direction != null && exitingInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            if (exitingInstruction.navigationKey !is Key) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> transitionBetween(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (exitingInstruction == null) return@addOpeningTransition null
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (exitingInstruction.navigationKey !is Exit) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Enter) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }
    }

    public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> transitionBetween(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter,
        returnExiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (exitingInstruction == null) return@addOpeningTransition null
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (exitingInstruction.navigationKey !is Exit) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Enter) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        addClosingTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (enteringInstruction == null) return@addClosingTransition null
            if (direction != null && exitingInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            if (exitingInstruction.navigationKey !is Enter) return@addClosingTransition null
            if (enteringInstruction.navigationKey !is Exit) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    internal fun build(parent: NavigationAnimationOverride?): NavigationAnimationOverride {
        return NavigationAnimationOverride(
            parent = parent,
            opening = opening,
            closing = closing
        )
    }

    public companion object {
        @PublishedApi
        internal const val DEFAULT_PRIORITY: Int = 0
        @PublishedApi
        internal const val DIRECTION_PRIORITY: Int = 10
        @PublishedApi
        internal const val PARTIAL_KEY_PRIORITY: Int = 20
        @PublishedApi
        internal const val EXACT_KEY_PRIORITY: Int = 30
    }
}
