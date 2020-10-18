package nav.enro.example.masterdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import nav.enro.annotations.NavigationDestination
import nav.enro.core.getNavigationHandle
import nav.enro.core.navigationHandle
import nav.enro.example.core.navigation.DetailKey
import nav.enro.example.core.navigation.ListKey
import nav.enro.example.core.navigation.MasterDetailKey
import nav.enro.masterdetail.MasterDetailProperty

@AndroidEntryPoint
@NavigationDestination(MasterDetailKey::class)
class MasterDetailActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MasterDetailKey>()
    private val masterDetail by MasterDetailProperty(
        lifecycleOwner = this,
        owningType = MasterDetailActivity::class,
        masterContainer = R.id.master,
        masterKey = ListKey::class,
        detailContainer = R.id.detail,
        detailKey = DetailKey::class
    ) {
        ListKey(navigation.key.userId, navigation.key.filter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.master_detail)
    }
}