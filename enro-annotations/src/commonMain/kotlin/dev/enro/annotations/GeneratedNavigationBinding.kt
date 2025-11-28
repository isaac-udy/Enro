package dev.enro.annotations

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class GeneratedNavigationBinding(
    val destination: String,
    val navigationKey: String,
    val bindingType: Int = 0,
) {
    public sealed interface BindingType {
        public class Class : BindingType
        public class Function : BindingType
        public class Property : BindingType

        public companion object {
            public const val CLASS: Int = 0
            public const val FUNCTION: Int = 1
            public const val PROPERTY: Int = 2

            public fun fromInt(value: Int): BindingType {
                return when (value) {
                    CLASS -> Class()
                    FUNCTION -> Function()
                    PROPERTY -> Property()
                    else -> throw IllegalArgumentException("Unknown binding type: $value")
                }
            }
        }
    }
}