package nav.enro.example.feature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationKey
import nav.enro.core.addOpenInstruction
import nav.enro.core.context.fragment
import nav.enro.core.context.parentActivity
import nav.enro.core.controller.createNavigationComponent
import nav.enro.core.forward
import nav.enro.core.navigationHandle
import nav.enro.example.R

@Parcelize
class MasterDetailKey(
    val userId: String,
    val filter: ListFilterType
) : NavigationKey

class MasterDetailActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MasterDetailKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.master_detail)
        if(savedInstanceState == null) {
            navigation.forward(ListKey(navigation.key.userId, navigation.key.filter))
        }
    }

}

val masterDetailComponent = createNavigationComponent {
    activityNavigator<MasterDetailKey, MasterDetailActivity>()

    activityToFragmentOverride<MasterDetailActivity, ListFragment>(
        launch = {
            val fragment =  ListFragment().addOpenInstruction(it.instruction)
            it.fromContext.childFragmentManager.beginTransaction()
                .replace(R.id.master, fragment)
                .setPrimaryNavigationFragment(fragment)
                .commitNow()
        },
        close = {
            it.parentActivity.finish()
        }
    )

    activityToFragmentOverride<MasterDetailActivity, DetailFragment>(
        launch = {
            val fragment =  DetailFragment().addOpenInstruction(it.instruction)
            it.fromContext.childFragmentManager.beginTransaction()
                .replace(R.id.detail, fragment)
                .setPrimaryNavigationFragment(fragment)
                .commitNow()
        },
        close = { context ->
            context.fragment.parentFragmentManager.beginTransaction()
                .remove(context.fragment)
                .setPrimaryNavigationFragment(context.parentActivity.supportFragmentManager.findFragmentById(R.id.master))
                .commitNow()
        }
    )
}