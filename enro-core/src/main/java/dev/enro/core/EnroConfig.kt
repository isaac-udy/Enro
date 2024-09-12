package dev.enro.core

import dev.enro.core.controller.EnroBackConfiguration

public data class EnroConfig(
    val isInTest: Boolean = false,
    internal val isAnimationsDisabled: Boolean = false,
    internal val isStrictMode: Boolean = false,
    internal val backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
    /**
     * This Boolean sets whether or not Composables will attempt to fallback to View based animations (Animation or Animator)
     * when there are no Composable Enter/ExitTransition animations provided. This is disabled by default for tests, based
     * on checking for the presence of the JUnit Test class, because these animations cause issues with ComposeTestRule tests.
     */
    internal val enableViewAnimationsForCompose: Boolean = runCatching { Class.forName("org.junit.Test") }.isFailure,
)
