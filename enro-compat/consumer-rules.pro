-dontwarn dagger.hilt.**

-keep class kotlin.LazyKt

-keep class * extends dev.enro.core.NavigationKey

#noinspection ShrinkerUnresolvedReference
-keep @dev.enro.annotations.GeneratedNavigationBinding public class **
-keep @dev.enro.annotations.GeneratedNavigationModule public class **
-keep @dev.enro.annotations.GeneratedNavigationComponent public class **