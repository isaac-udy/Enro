package dev.enro.core

import dev.enro.core.controller.EnroBackConfiguration

public data class EnroConfig(
    internal val isInTest: Boolean = false,
    internal val isAnimationsDisabled: Boolean = false,
    internal val isStrictMode: Boolean = false,
    /**
     * In versions of Enro before 2.8.0, NavigationContainers would always accept destinations that were presented, and
     * would only enforce their navigation instruction filter for pushed instructions. This is no longer the default
     * behavior, but can be re-enabled by setting this Boolean to true.
     */
    @Deprecated("This behavior is no longer recommended, and will be removed in a future version of Enro. Please update your NavigationContainers to use a NavigationInstructionFilter that explicitly declares all instructions that are valid for the container.")
    internal val useLegacyContainerPresentBehavior: Boolean = false,
    internal val backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
    /**
     * This Boolean sets whether or not Composables will attempt to fallback to View based animations (Animation or Animator)
     * when there are no Composable Enter/ExitTransition animations provided. This is disabled by default for tests, based
     * on checking for the presence of the JUnit Test class, because these animations cause issues with ComposeTestRule tests.
     */
    internal val enableViewAnimationsForCompose: Boolean = !isInTest,
)
