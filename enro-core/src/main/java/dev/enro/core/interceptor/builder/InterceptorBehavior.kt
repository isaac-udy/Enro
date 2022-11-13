package dev.enro.core.interceptor.builder

import dev.enro.core.NavigationInstruction

public sealed interface InterceptorBehavior {
    public sealed interface ForOpen : InterceptorBehavior
    public sealed interface ForClose : InterceptorBehavior
    public sealed interface ForResult : InterceptorBehavior

    public class Cancel internal constructor() :
        ForOpen,
        ForClose,
        ForResult {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    public class Continue :
        ForOpen,
        ForClose,
        ForResult {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    public class ReplaceWith internal constructor(public val instruction: NavigationInstruction.Open<*>) :
        ForOpen,
        ForClose,
        ForResult {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ReplaceWith

            if (instruction != other.instruction) return false

            return true
        }

        override fun hashCode(): Int {
            return instruction.hashCode()
        }
    }

    public class DeliverResultAndCancel internal constructor() :
        ForResult {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}