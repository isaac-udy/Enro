package dev.enro

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.ui.NavigationDestination
import dev.enro.viewmodel.getNavigationHandle
import kotlin.jvm.JvmName

// TODO generically type NavigationContext to allow for enforcing parent/child types?
public sealed class NavigationContext() :
    LifecycleOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    public abstract val id: String
    public abstract val parentContext: NavigationContext?

    private val mutableChildren = mutableListOf<NavigationContext>()
    public val children: List<NavigationContext> get() = mutableChildren.toList()

    internal fun registerChild(context: NavigationContext) {
        mutableChildren.add(context)
    }

    internal fun unregisterChild(context: NavigationContext) {
        mutableChildren.remove(context)
    }

    public class Destination<out T : NavigationKey> internal constructor(
        lifecycleOwner: LifecycleOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,
        public override val parentContext: Container,
        public val destination: NavigationDestination<T>,
    ) : NavigationContext(),
        LifecycleOwner by lifecycleOwner,
        ViewModelStoreOwner by viewModelStoreOwner,
        HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory {

        public override val id: String get() = destination.instance.id

        public val parentContainer: NavigationContainer get() = parentContext.container
        public val instance: NavigationKey.Instance<T> get() = destination.instance
        public val key: T get() = destination.instance.key

    }

    public class Container internal constructor(
        public override val parentContext: NavigationContext,
        public val container: NavigationContainer,
    ) : NavigationContext(),
        LifecycleOwner by parentContext,
        ViewModelStoreOwner by parentContext,
        HasDefaultViewModelProviderFactory by parentContext {

        public override val id: String get() = container.key.name

    }

    public class Root internal constructor(
        lifecycleOwner: LifecycleOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,
    ) : NavigationContext(),
        LifecycleOwner by lifecycleOwner,
        ViewModelStoreOwner by viewModelStoreOwner,
        HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory {

        override val id: String = "Root"
        override val parentContext: NavigationContext? = null
    }
}

public fun NavigationContext.root(): NavigationContext.Root {
    return when (this) {
        is NavigationContext.Root -> this
        is NavigationContext.Container -> parentContext.root()
        is NavigationContext.Destination<*> -> parentContext.root()
    }
}

@PublishedApi
internal fun NavigationContext.findChildContext(
    predicate: (NavigationContext) -> Boolean,
) : NavigationContext? {
    // Check if current context matches
    if (predicate(this)) return this
    // Recursively search through children
    for (child in children) {
        val found = child.findChildContext(predicate)
        if (found != null) return found
    }
    return null
}

@PublishedApi
@JvmName("findTypedChildContext")
internal inline fun <reified T: NavigationContext> NavigationContext.findChildContext(
    crossinline predicate: (T) -> Boolean = { true },
) : T? {
    val result = findChildContext {
        it is T && predicate(it)
    }
    return result as T?
}

public fun <T: NavigationKey> NavigationContext.findChildDestinationContext(
    predicate: (NavigationContext.Destination<T>) -> Boolean = { true },
) : NavigationContext.Destination<T>? {
   return findChildContext(predicate)
}

public inline fun NavigationContext.findChildContainerContext(
    crossinline predicate: (NavigationContext.Container) -> Boolean,
) : NavigationContext.Container? {
    return findChildContext(predicate)
}

public fun NavigationContext.getDebugString(): String {
    return buildString {
        appendNode(this@getDebugString, 0)
    }
}

public inline fun <reified T : NavigationKey> NavigationContext.Destination<T>.getNavigationHandle(): NavigationHandle<T> {
    return (this as ViewModelStoreOwner).getNavigationHandle()
}

private fun StringBuilder.appendNode(context: NavigationContext, depth: Int) {
    val indent = "    ".repeat(depth)
    append(indent)
    when (context) {
        is NavigationContext.Root -> append("Root")
        is NavigationContext.Container -> append("Container(${context.container.key.name})")
        is NavigationContext.Destination<*> -> append("Destination(${context.key::class.simpleName})")
    }
    appendLine()

    context.children.forEachIndexed { index, child ->
        appendNode(child, depth + 1)
    }
}
