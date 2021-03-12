package dev.enro.example.masterdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationHandle
import dev.enro.example.core.navigation.DetailKey
import dev.enro.example.core.navigation.ListKey
import dev.enro.example.core.navigation.MasterDetailKey
import dev.enro.masterdetail.MasterDetailProperty

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