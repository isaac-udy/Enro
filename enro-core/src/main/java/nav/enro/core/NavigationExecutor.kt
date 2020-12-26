package nav.enro.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.activity.DefaultActivityExecutor
import nav.enro.core.fragment.DefaultFragmentExecutor
import kotlin.reflect.KClass

// This class is used primarily to simplify the lambda signature of NavigationExecutor.open
class ExecutorArgs<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromContext: NavigationContext<out FromContext>,
    val navigator: Navigator<out KeyType, out OpensContext>,
    val key: KeyType,
    val instruction: NavigationInstruction.Open
)

abstract class NavigationExecutor<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromType: KClass<FromContext>,
    val opensType: KClass<OpensContext>,
    val keyType: KClass<KeyType>
) {
    open fun preOpened(
        context: NavigationContext<out OpensContext>
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

    open fun postClosed(
        context: NavigationContext<out FromContext>
    ) {}
}

class NavigationExecutorBuilder<FromContext: Any, OpensContext: Any, KeyType: NavigationKey> @PublishedApi internal constructor(
    private val fromType: KClass<FromContext>,
    private val opensType: KClass<OpensContext>,
    private val keyType: KClass<KeyType>
) {

    private var preOpenedFunc: (( context: NavigationContext<out OpensContext>) -> Unit)? = null
    private var openedFunc: ((args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>) -> Unit)? = null
    private var postOpenedFunc: ((context: NavigationContext<out OpensContext>) -> Unit)? = null
    private var preClosedFunc: ((context: NavigationContext<out OpensContext>) -> Unit)? = null
    private var closedFunc: ((context: NavigationContext<out OpensContext>) -> Unit)? = null
    private var postClosedFunc: ((context: NavigationContext<out FromContext>) -> Unit)? = null

    @Suppress("UNCHECKED_CAST")
    private val defaultOpens: (args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>) -> Unit by lazy {
        when {
            FragmentActivity::class.java.isAssignableFrom(opensType.java) ->
                DefaultActivityExecutor::open as ((ExecutorArgs<out Any, out OpensContext, out NavigationKey>) -> Unit)

            Fragment::class.java.isAssignableFrom(opensType.java) ->
                DefaultFragmentExecutor::open as ((ExecutorArgs<out Any, out OpensContext, out NavigationKey>) -> Unit)

            else -> throw IllegalArgumentException("No default launch executor found for ${opensType.java}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val defaultCloses: (context: NavigationContext<out OpensContext>) -> Unit by lazy {
        when {
            FragmentActivity::class.java.isAssignableFrom(opensType.java) ->
                DefaultActivityExecutor::close as (NavigationContext<out OpensContext>) -> Unit

            Fragment::class.java.isAssignableFrom(opensType.java) ->
                DefaultFragmentExecutor::close as (NavigationContext<out OpensContext>) -> Unit

            else -> throw IllegalArgumentException("No default close executor found for ${opensType.java}")
        }
    }

    fun preOpened(block: ( context: NavigationContext<out OpensContext>) -> Unit) {
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

    fun postClosed(block: (context: NavigationContext<out FromContext>) -> Unit) {
        if(postClosedFunc != null) throw IllegalStateException("Value is already set!")
        postClosedFunc = block
    }


    internal fun build() = object : NavigationExecutor<FromContext, OpensContext, KeyType>(
        fromType,
        opensType,
        keyType
    ) {
        override fun preOpened(context: NavigationContext<out OpensContext>) {
            preOpenedFunc?.invoke(context)
        }

        override fun open(args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>) {
            (openedFunc ?: defaultOpens).invoke(args)
        }

        override fun postOpened(context: NavigationContext<out OpensContext>) {
            postOpenedFunc?.invoke(context)
        }

        override fun preClosed(context: NavigationContext<out OpensContext>) {
            preClosedFunc?.invoke(context)
        }

        override fun close(context: NavigationContext<out OpensContext>) {
            (closedFunc ?: defaultCloses).invoke(context)
        }

        override fun postClosed(context: NavigationContext<out FromContext>) {
            postClosedFunc?.invoke(context)
        }
    }
}