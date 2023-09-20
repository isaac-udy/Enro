package dev.enro.test

import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainerContext
import org.junit.Assert

fun NavigationContainerContext.assertBackstackEquals(
    backstack: NavigationBackstack
) {
    Assert.assertEquals(backstack, this.backstack)
}
