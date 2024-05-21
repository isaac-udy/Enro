package dev.enro.core.result

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationKey

public class NavigationResultScope<Result: Any, Key : NavigationKey.WithResult<Result>> internal constructor(
    public val instruction: AnyOpenInstruction,
    public val key: Key,
)