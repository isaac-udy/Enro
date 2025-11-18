package dev.enro.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

class EnroLogger(
    private val messageCollector: MessageCollector
) {

    fun warn(message: String, location: CompilerMessageSourceLocation? = null) {
        messageCollector.report(
            severity = CompilerMessageSeverity.WARNING,
            message = message,
            location = location,
        )
    }

    fun warnStrong(message: String, location: CompilerMessageSourceLocation? = null) {
        messageCollector.report(
            severity = CompilerMessageSeverity.STRONG_WARNING,
            message = message,
            location = location,
        )
    }

    fun error(message: String, location: CompilerMessageSourceLocation? = null) {
        messageCollector.report(
            severity = CompilerMessageSeverity.ERROR,
            message = message,
            location = location,
        )
    }
}

