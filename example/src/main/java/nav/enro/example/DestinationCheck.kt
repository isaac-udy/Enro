package nav.enro.example

import nav.enro.core.controller.NavigationControllerBuilder
import nav.enro.core.controller.NavigationControllerBuilderAction
import nav.enro.example.feature.DetailActivity
import nav.enro.example.feature.DetailKey


class DetailActivityDestination : NavigationControllerBuilderAction {
    override fun apply(builder: NavigationControllerBuilder) {
        builder.activityNavigator<DetailKey, DetailActivity>()
    }
}