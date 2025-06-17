package dev.enro.core.controller

import dev.enro.EnroController

@Deprecated(
    message = "NavigationController has been renamed to EnroController. Please use EnroController instead.",
    replaceWith = ReplaceWith("EnroController", "dev.enro.EnroController"),
    level = DeprecationLevel.WARNING
)
public typealias NavigationController = EnroController