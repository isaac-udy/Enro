package dev.enro.tests.application.compose

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.push
import dev.enro.destination.compose.EnroSharedElements
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.serialization.Serializable

data class SharedElementIcon(
    val title: String,
    val icon: ImageVector,
    val id: Int
)

val sharedElementIcons = listOf(
    SharedElementIcon("Profile", Icons.Default.Person, 1),
    SharedElementIcon("Home", Icons.Default.Home, 2),
    SharedElementIcon("Favorites", Icons.Default.Favorite, 3),
    SharedElementIcon("Settings", Icons.Default.Settings, 4),
    SharedElementIcon("Mail", Icons.Default.Email, 5),
    SharedElementIcon("Star", Icons.Default.Star, 6)
)

@Serializable
object ComposeSharedElementTransitions : NavigationKey.SupportsPush {
    @Serializable
    class List : NavigationKey.SupportsPush
    
    @Serializable
    data class Detail(val iconId: Int) : NavigationKey.SupportsPush
}

@NavigationDestination(ComposeSharedElementTransitions::class)
@Composable
fun ComposableSharedElementTransitionsRootScreen() {
    val container = rememberNavigationContainer(
        root = ComposeSharedElementTransitions.List(),
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    Box(Modifier.fillMaxSize()) {
        container.Render()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@NavigationDestination(ComposeSharedElementTransitions.List::class)
@Composable
fun ComposeSharedElementTransitionsListScreen() {
    val navigation = navigationHandle()
    
    TitledColumn(title = "Shared Element Icons") {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sharedElementIcons) { icon ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigation.push(ComposeSharedElementTransitions.Detail(icon.id))
                        },
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EnroSharedElements {
                            Image(
                                imageVector = icon.icon,
                                contentDescription = icon.title,
                                modifier = Modifier
                                    .sharedElement(
                                        rememberSharedContentState(key = "icon_${icon.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
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
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@NavigationDestination(ComposeSharedElementTransitions.Detail::class)
@Composable
fun ComposeSharedElementTransitionsDetailScreen() {
    val navigation = navigationHandle<ComposeSharedElementTransitions.Detail>()
    val selectedIconId = navigation.key.iconId
    val selectedIcon = sharedElementIcons.find { it.id == selectedIconId } 
        ?: sharedElementIcons.first()
    
    TitledColumn(title = "Icon Details") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnroSharedElements {
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
                            animatedVisibilityScope = animatedVisibilityScope,
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
}