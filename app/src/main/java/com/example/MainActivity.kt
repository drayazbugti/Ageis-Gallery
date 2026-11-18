package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MediaType
import com.example.service.VideoPlaybackManager
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.GalleryHomeScreen
import com.example.ui.screens.MediaViewerScreen
import com.example.ui.screens.SearchTagScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.VideosGalleryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaDarkGlassBg
import com.example.ui.theme.NovaDarkSubtext
import com.example.ui.theme.NovaLightBg
import com.example.ui.theme.NovaLightGlassBg
import com.example.ui.theme.NovaLightSubtext
import com.example.ui.theme.NovaLightText
import com.example.viewmodel.GalleryViewModel

enum class NavDestination(
  val label: String,
  val testTag: String
) {
  PHOTOS("Photos", "nav_photos"),
  ALBUMS("Albums", "nav_albums"),
  EXPLORE("Explore", "nav_explore"),
  VIDEOS("Videos", "nav_videos"),
  SETTINGS("Settings", "nav_settings"),
  SEARCH("Search", "nav_search"),
  VAULT("Vault", "nav_vault"),
  TRASH("Trash", "nav_trash")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    VideoPlaybackManager.initialize(this)
    setContent {
      val viewModel: GalleryViewModel = viewModel()
      val isLightMode by viewModel.isLightMode.collectAsState()
      val otaState by viewModel.otaState.collectAsState()
      MyApplicationTheme(
        darkTheme = !isLightMode,
        remoteAccentColorHex = otaState.accentColorHex
      ) {
        AegisGalleryApp(viewModel = viewModel)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    VideoPlaybackManager.onAppForegrounded(this)
  }

  override fun onStop() {
    super.onStop()
    VideoPlaybackManager.onAppBackgrounded(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    if (!VideoPlaybackManager.isBackgroundPlaybackEnabled.value) {
      VideoPlaybackManager.pause()
      VideoPlaybackManager.stopBackgroundService()
    }
  }
}

enum class AppStage {
  SPLASH,
  MAIN
}

@Composable
fun AegisGalleryApp(viewModel: GalleryViewModel) {
  var appStage by remember { mutableStateOf(AppStage.SPLASH) }
  val lifecycleOwner = LocalLifecycleOwner.current
  val otaState by viewModel.otaState.collectAsState()
  val isLightMode by viewModel.isLightMode.collectAsState()

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        viewModel.refreshRemoteConfigInBackground(force = true)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  when (appStage) {
    AppStage.SPLASH -> {
      SplashScreen(
        otaState = otaState,
        isLightMode = isLightMode,
        onTimeout = { appStage = AppStage.MAIN }
      )
    }
    AppStage.MAIN -> {
      NovaGalleryMainContent(viewModel = viewModel)
    }
  }
}

@Composable
fun NovaGalleryMainContent(viewModel: GalleryViewModel) {
  var currentDestination by remember { mutableStateOf(NavDestination.PHOTOS) }
  var lastMainTab by remember { mutableStateOf(NavDestination.PHOTOS) }
  val viewingItem by viewModel.viewingItem.collectAsState()
  val isLightMode by viewModel.isLightMode.collectAsState()
  val isStorageScanned by viewModel.isStorageScanned.collectAsState()

  val isTopLevelTab = currentDestination == NavDestination.PHOTOS ||
    currentDestination == NavDestination.ALBUMS ||
    currentDestination == NavDestination.EXPLORE

  BackHandler(enabled = viewingItem != null || currentDestination != NavDestination.PHOTOS) {
    if (viewingItem != null) {
      viewModel.setViewingItem(null)
    } else if (!isTopLevelTab) {
      currentDestination = lastMainTab
    } else if (currentDestination != NavDestination.PHOTOS) {
      currentDestination = NavDestination.PHOTOS
      lastMainTab = NavDestination.PHOTOS
      viewModel.setSelectedAlbum(null)
    }
  }

  if (viewingItem != null) {
    val item = viewingItem!!
    if (item.type == MediaType.VIDEO) {
      VideoPlayerScreen(
        item = item,
        viewModel = viewModel,
        onBack = { viewModel.setViewingItem(null) },
        onOpenVault = {
          viewModel.setViewingItem(null)
          currentDestination = NavDestination.VAULT
        }
      )
    } else {
      MediaViewerScreen(
        item = item,
        viewModel = viewModel,
        onBack = { viewModel.setViewingItem(null) },
        onOpenVault = {
          viewModel.setViewingItem(null)
          currentDestination = NavDestination.VAULT
        }
      )
    }
  } else {
    val bgColor = if (isLightMode) NovaLightBg else NovaDarkBg
    val pillBg = if (isLightMode) NovaLightGlassBg else NovaDarkGlassBg
    val pillBorder = if (isLightMode) Color(0x1F000000) else NovaDarkCardBorder
    val activeTextColor = if (isLightMode) NovaLightText else Color.White
    val inactiveTextColor = if (isLightMode) NovaLightSubtext else NovaDarkSubtext

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(bgColor)
    ) {
      AnimatedContent(
        targetState = currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition",
        modifier = Modifier.fillMaxSize()
      ) { dest ->
        when (dest) {
          NavDestination.PHOTOS -> GalleryHomeScreen(
            viewModel = viewModel,
            onMediaClick = { item -> viewModel.setViewingItem(item) },
            onOpenVault = { currentDestination = NavDestination.VAULT },
            onOpenSearch = { currentDestination = NavDestination.SEARCH },
            onOpenSettings = { currentDestination = NavDestination.SETTINGS }
          )
          NavDestination.ALBUMS -> AlbumsScreen(
            viewModel = viewModel,
            onMediaClick = { item -> viewModel.setViewingItem(item) },
            onOpenVault = { currentDestination = NavDestination.VAULT },
            onOpenVideos = { currentDestination = NavDestination.VIDEOS },
            onOpenTrash = { currentDestination = NavDestination.TRASH },
            onOpenSearch = { currentDestination = NavDestination.SEARCH },
            onOpenSettings = { currentDestination = NavDestination.SETTINGS }
          )
          NavDestination.EXPLORE -> ExploreScreen(
            viewModel = viewModel,
            onMediaClick = { item -> viewModel.setViewingItem(item) },
            onOpenVideos = { currentDestination = NavDestination.VIDEOS },
            onOpenVault = { currentDestination = NavDestination.VAULT },
            onOpenTrash = { currentDestination = NavDestination.TRASH },
            onOpenSettings = { currentDestination = NavDestination.SETTINGS }
          )
          NavDestination.VIDEOS -> VideosGalleryScreen(
            viewModel = viewModel,
            onVideoClick = { item -> viewModel.setViewingItem(item) }
          )
          NavDestination.SETTINGS -> SettingsScreen(
            viewModel = viewModel,
            onOpenVault = { currentDestination = NavDestination.VAULT }
          )
          NavDestination.SEARCH -> SearchTagScreen(
            viewModel = viewModel,
            onMediaClick = { item -> viewModel.setViewingItem(item) },
            onBack = { currentDestination = lastMainTab }
          )
          NavDestination.VAULT -> VaultScreen(
            viewModel = viewModel,
            onMediaClick = { item -> viewModel.setViewingItem(item) },
            onBack = { currentDestination = lastMainTab }
          )
          NavDestination.TRASH -> TrashScreen(
            viewModel = viewModel,
            onBack = { currentDestination = lastMainTab }
          )
        }
      }

      // Floating Translucent Pill Bottom Navigation Bar (.bottom-nav: Photos, Albums, Explore)
      if (isTopLevelTab) {
        Surface(
          color = pillBg,
          shape = RoundedCornerShape(30.dp),
          border = BorderStroke(1.dp, pillBorder),
          shadowElevation = 12.dp,
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 14.dp)
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .testTag("bottom_nav_bar")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf(
              NavDestination.PHOTOS,
              NavDestination.ALBUMS,
              NavDestination.EXPLORE
            ).forEach { tab ->
              FloatingPillNavItem(
                label = tab.label,
                isSelected = currentDestination == tab,
                activeColor = activeTextColor,
                inactiveColor = inactiveTextColor,
                testTag = tab.testTag,
                onClick = {
                  currentDestination = tab
                  lastMainTab = tab
                  if (tab == NavDestination.PHOTOS) {
                    viewModel.setSelectedAlbum(null)
                  }
                }
              )
            }
          }
        }
      }

      // Rimzim Permission & Auto-Scan Overlay (#permissionOverlay)
      AnimatedVisibility(
        visible = !isStorageScanned,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = true, onClick = {}) // Modal barrier
            .padding(20.dp)
            .testTag("permission_overlay"),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            color = Color(0xFF1A2230),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
              .fillMaxWidth()
              .widthIn(max = 320.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp)
            ) {
              Text(
                text = "Ayaz Gallery",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Grant storage access to automatically scan and display your device photos and videos.",
                fontSize = 13.sp,
                color = Color(0xFF94D3BE),
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(20.dp))
              Button(
                onClick = { viewModel.performStorageScan() },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF10B981),
                  contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                modifier = Modifier
                  .heightIn(min = 48.dp)
                  .testTag("permission_allow_button")
              ) {
                Text(
                  text = "Allow & Scan",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FloatingPillNavItem(
  label: String,
  isSelected: Boolean,
  activeColor: Color,
  inactiveColor: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .heightIn(min = 48.dp)
      .clip(RoundedCornerShape(24.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true),
        onClick = onClick
      )
      .padding(horizontal = 18.dp, vertical = 10.dp)
      .testTag(testTag)
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) activeColor else inactiveColor
    )
  }
}
