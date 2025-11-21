package dev.enro.annotations

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class GeneratedNavigationBinding(
    val destination: String,
    val navigationKey: String,
    val bindingType: Int = 0,
) {
    public object BindingType {
        public const val CLASS: Int = 0
        public const val FUNCTION: Int = 1
        public const val PROPERTY: Int = 2
    }
}