package dev.enro.core

import android.util.Log
import androidx.fragment.app.Fragment
import dev.enro.core.activity.ActivityNavigator
import dev.enro.core.activity.DefaultActivityExecutor
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.compose.DefaultComposableExecutor
import dev.enro.core.fragment.DefaultFragmentExecutor
import dev.enro.core.fragment.FragmentNavigator
import dev.enro.core.synthetic.DefaultSyntheticExecutor
import dev.enro.core.synthetic.SyntheticNavigator
import kotlin.reflect.KClass

// This class is used primarily to simplify the lambda signature of NavigationExecutor.open
class ExecutorArgs<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromContext: NavigationContext<out FromContext>,
    val navigator: Navigator<out KeyType, out OpensContext>,
    val key: KeyType,
    instruction: AnyOpenInstruction
) {
    val instruction: AnyOpenInstruction = instruction.internal.copy(
        previouslyActiveId = fromContext.containerManager.activeContainer?.id
    )
}

abstract class NavigationExecutor<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromType: KClass<FromContext>,
    val opensType: KClass<OpensContext>,
    val keyType: KClass<KeyType>
) {
    open fun animation(instruction: AnyOpenInstruction): NavigationAnimation {
        return when(instruction.navigationDirection) {
            NavigationDirection.Push -> DefaultAnimations.push
            NavigationDirection.Present -> DefaultAnimations.present
            NavigationDirection.Forward -> DefaultAnimations.forward
            NavigationDirection.Replace -> DefaultAnimations.replace
            NavigationDirection.ReplaceRoot -> DefaultAnimations.replaceRoot
        }
    }

    open fun closeAnimation(context: NavigationContext<out OpensContext>): NavigationAnimation {
        return DefaultAnimations.close
    }

    open fun preOpened(
        context: NavigationContext<out FromContext>
    ) {}

    abstract fun open(
        args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>
    )

    open fun postOpened(
        context: NavigationContext<out OpensContext>
    ) {}

    open fun preClosed(
        context: NavigationContext<out OpensContext>
    ) {}

    abstract fun close(
        context: NavigationContext<out OpensContext>
    )
}

class NavigationExecutorBuilder<FromContext: Any, OpensContext: Any, KeyType: NavigationKey> @PublishedApi internal constructor(
    private val fromType: KClass<FromContext>,
    private val opensType: KClass<OpensContext>,
    private val keyType: KClass<KeyType>
) {

    private var animationFunc: ((instruction: AnyOpenInstruction) -> NavigationAnimation)? = null
    private var closeAnimationFunc: ((context: NavigationContext<out OpensContext>) -> NavigationAnimation)? = null
    private var preOpenedFunc: (( context: NavigationContext<out FromContext>) -> Unit)? = null
    private var openedFunc: ((args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>) -> Unit)? = null
    private var postOpenedFunc: ((context: NavigationContext<out OpensContext>) -> Unit)? = null
    private var preClosedFunc: ((context: NavigationContext<out OpensContext>) -> Unit)? = null
    private var closedFunc: ((context: NavigationContext<out OpensContext>) -> Unit)? = null

    @Suppress("UNCHECKED_CAST")
    fun defaultOpened(args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>) {
        when(args.navigator) {
             is ActivityNavigator ->
                DefaultActivityExecutor::open as ((ExecutorArgs<out Any, out OpensContext, out NavigationKey>) -> Unit)

            is FragmentNavigator ->
                DefaultFragmentExecutor::open as ((ExecutorArgs<out Any, out OpensContext, out NavigationKey>) -> Unit)

            is SyntheticNavigator ->
                DefaultSyntheticExecutor::open as ((ExecutorArgs<out Any, out OpensContext, out NavigationKey>) -> Unit)

            is ComposableNavigator ->
                DefaultComposableExecutor::open as ((ExecutorArgs<out Any, out OpensContext, out NavigationKey>) -> Unit)

            else -> throw IllegalArgumentException("No default launch executor found for ${opensType.java}")
        }.invoke(args)
    }

    @Suppress("UNCHECKED_CAST")
    fun defaultClosed(context: NavigationContext<out OpensContext>) {
        when(context.navigator) {
            is ActivityNavigator ->
                DefaultActivityExecutor::close as (NavigationContext<out OpensContext>) -> Unit

            is FragmentNavigator ->
                DefaultFragmentExecutor::close as (NavigationContext<out OpensContext>) -> Unit

            is ComposableNavigator ->
                DefaultComposableExecutor::close as (NavigationContext<out OpensContext>) -> Unit

            else -> throw IllegalArgumentException("No default close executor found for ${opensType.java}")
        }.invoke(context)
    }

    fun animation(block: (instruction: AnyOpenInstruction) -> NavigationAnimation) {
        if(animationFunc != null) throw IllegalStateException("Value is already set!")
        animationFunc = block
    }

    fun closeAnimation(block: ( context: NavigationContext<out OpensContext>) -> NavigationAnimation) {
        if(closeAnimationFunc != null) throw IllegalStateException("Value is already set!")
        closeAnimationFunc = block
    }

    fun preOpened(block: ( context: NavigationContext<out FromContext>) -> Unit) {
        if(preOpenedFunc != null) throw IllegalStateException("Value is already set!")
        preOpenedFunc = block
    }

    fun opened(block: (args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>) -> Unit) {
        if(openedFunc != null) throw IllegalStateException("Value is already set!")
        openedFunc = block
    }

    fun postOpened(block: (context: NavigationContext<out OpensContext>) -> Unit) {
        if(postOpenedFunc != null) throw IllegalStateException("Value is already set!")
        postOpenedFunc = block
    }

    fun preClosed(block: (context: NavigationContext<out OpensContext>) -> Unit) {
        if(preClosedFunc != null) throw IllegalStateException("Value is already set!")
        preClosedFunc = block
    }

    fun closed(block: (context: NavigationContext<out OpensContext>) -> Unit) {
        if(closedFunc != null) throw IllegalStateException("Value is already set!")
        closedFunc = block
    }

    internal fun build() = object : NavigationExecutor<FromContext, OpensContext, KeyType>(
        fromType,
        opensType,
        keyType
    ) {
        override fun animation(instruction: AnyOpenInstruction): NavigationAnimation {
            return animationFunc?.invoke(instruction) ?: super.animation(instruction)
        }

        override fun closeAnimation(context: NavigationContext<out OpensContext>): NavigationAnimation {
            return closeAnimationFunc?.invoke(context) ?: super.closeAnimation(context)
        }

        override fun preOpened(context: NavigationContext<out FromContext>) {
            preOpenedFunc?.invoke(context)
        }

        override fun open(args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>) {
            openedFunc?.invoke(args) ?: defaultOpened(args)
        }

        override fun postOpened(context: NavigationContext<out OpensContext>) {
            postOpenedFunc?.invoke(context)
        }

        override fun preClosed(context: NavigationContext<out OpensContext>) {
            preClosedFunc?.invoke(context)
        }

        override fun close(context: NavigationContext<out OpensContext>) {
            closedFunc?.invoke(context) ?: defaultClosed(context)
        }
    }
}

fun <From : Any, Opens : Any> createOverride(
    fromClass: KClass<From>,
    opensClass: KClass<Opens>,
    block: NavigationExecutorBuilder<From, Opens, NavigationKey>.() -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    NavigationExecutorBuilder(fromClass, opensClass, NavigationKey::class)
        .apply(block)
        .build()

inline fun <reified From : Any, reified Opens : Any> createOverride(
    noinline block: NavigationExecutorBuilder<From, Opens, NavigationKey>.() -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    createOverride(From::class, Opens::class, block)

inline fun <reified From : Fragment, reified Opens : Fragment>  createSharedElementOverride(
    elements: List<Pair<Int, Int>>
): NavigationExecutor<From, Opens, NavigationKey> {
    return createOverride {
        opened { args ->
            args.instruction.setSharedElements(
                elements.map { EnroSharedElement(it.first, it.second) }
            )
            defaultOpened(args)
        }
    }
}