package dev.enro.core

abstract class EnroException(message: String) : IllegalStateException(message)

class EnroLifecycleException(message: String) : EnroException(message)