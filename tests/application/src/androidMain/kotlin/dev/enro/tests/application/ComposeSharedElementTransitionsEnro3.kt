package dev.enro.tests.application

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro3.NavigationKey
import dev.enro3.open
import dev.enro3.ui.navigationDestination
import dev.enro3.ui.navigationHandle
import kotlinx.serialization.Serializable

data class SharedElementIcon(
    val title: String,
    val icon: ImageVector,
    val id: Int,
)

val sharedElementIcons = listOf(
    SharedElementIcon("Profile", Icons.Default.Person, 1),
    SharedElementIcon("Home", Icons.Default.Home, 2),
    SharedElementIcon("Favorites", Icons.Default.Favorite, 3),
    SharedElementIcon("Settings", Icons.Default.Settings, 4),
    SharedElementIcon("Mail", Icons.Default.Email, 5),
    SharedElementIcon("Star", Icons.Default.Star, 6)
)

object ComposeSharedElementTransitions {
    @Serializable
    class List : NavigationKey

    @Serializable
    data class Detail(val iconId: Int) : NavigationKey
}

@OptIn(ExperimentalSharedTransitionApi::class)
val composeSharedElementTransitionsListScreen = navigationDestination<ComposeSharedElementTransitions.List> {
    val navigation = navigationHandle<ComposeSharedElementTransitions.List>()

    TitledColumn(title = "Shared Element Icons") {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sharedElementIcons) { icon ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigation.open(ComposeSharedElementTransitions.Detail(icon.id))
                        },
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            imageVector = icon.icon,
                            contentDescription = icon.title,
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = "icon_${icon.id}"),
                                    animatedVisibilityScope = this@navigationDestination,
                                )
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )


                        Text(
                            text = icon.title,
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "title_${icon.id}"),
                                    animatedVisibilityScope = this@navigationDestination,
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
val composeSharedElementTransitionsDetailScreen = navigationDestination<ComposeSharedElementTransitions.Detail> {
    val navigation = navigationHandle<ComposeSharedElementTransitions.Detail>()
    val selectedIconId = navigation.key.iconId
    val selectedIcon = sharedElementIcons.find { it.id == selectedIconId }
        ?: sharedElementIcons.first()

    TitledColumn(title = "Icon Details") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedIcon.title,
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            )

            Image(
                imageVector = selectedIcon.icon,
                contentDescription = selectedIcon.title,
                modifier = Modifier
                    .sharedElement(
                        rememberSharedContentState(key = "icon_${selectedIcon.id}"),
                        animatedVisibilityScope = this@navigationDestination,
                    )
                    .size(200.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "This is a detailed view of the ${selectedIcon.title} icon.",
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
    }
}