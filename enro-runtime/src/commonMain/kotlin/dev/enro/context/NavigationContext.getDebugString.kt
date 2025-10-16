package dev.enro.context



public fun AnyNavigationContext.getDebugString(): String {
    return buildString {
        appendNode(this@getDebugString, 0)
    }
}

private fun StringBuilder.appendNode(
    context: AnyNavigationContext,
    depth: Int,
) {
    val indent = "    ".repeat(depth)
    val activeIndent = "- - ".repeat(depth)
    when {
        context.isActiveInRoot -> append(activeIndent)
        else -> append(indent)
    }

    if (context !is RootContext) {
        if (context.isActiveInRoot) {
            append("→ ")
        } else {
            append("  ")
        }
    }
    when (context) {
        is ContainerContext -> append("Container(${context.container.key.name})")
        is DestinationContext<*> -> append("Destination(${context.key::class.simpleName})")
        is RootContext -> append("Root")
    }
    appendLine()

    if (context is ContainerContext) {
        val childrenById = context.children.associateBy { it.id }
        context.container.backstack.forEach {
            val context = childrenById[it.id]
            if (context != null) {
                appendNode(context, depth + 1)
            }
            else {
                appendLine("$indent      Destination(${it.key::class.simpleName})")
            }
        }
    }
    else {
        context.children.forEachIndexed { index, child ->
            if (child is AnyNavigationContext) {
                appendNode(child, depth + 1)
            }
        }
    }
}
