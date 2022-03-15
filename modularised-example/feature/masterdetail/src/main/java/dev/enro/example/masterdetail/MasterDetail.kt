package dev.enro.example.masterdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.EmptyBehavior
import dev.enro.core.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.example.core.navigation.DetailKey
import dev.enro.example.core.navigation.ListKey
import dev.enro.example.core.navigation.MasterDetailKey

@AndroidEntryPoint
@NavigationDestination(MasterDetailKey::class)
class MasterDetailActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MasterDetailKey>()

    private val masterContainer by navigationContainer(
        containerId = R.id.master,
        emptyBehavior = EmptyBehavior.CloseParent,
        root = { ListKey(navigation.key.userId, navigation.key.filter) },
        accept = { it is ListKey }
    )

    private val detailsContainer by navigationContainer(
        containerId = R.id.detail,
        emptyBehavior = EmptyBehavior.AllowEmpty,
        accept = { it is DetailKey }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.master_detail)
    }
}