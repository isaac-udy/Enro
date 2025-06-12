package dev.enro.core.container

import dev.enro.NavigationContainerFilter
import dev.enro.NavigationContainerFilterBuilder
import dev.enro.doNotAccept

public typealias NavigationInstructionFilter = NavigationContainerFilter
public typealias NavigationInstructionFilterBuilder = NavigationContainerFilterBuilder

public fun acceptAll(): NavigationContainerFilter =
    dev.enro.acceptAll()

public fun acceptNone(): NavigationContainerFilter =
    dev.enro.acceptNone()


public fun accept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationContainerFilter =
    dev.enro.accept(block)

public fun doNotAccept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationContainerFilter =
    doNotAccept(block)