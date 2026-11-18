package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkCardBg
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaDarkSubtext
import com.example.ui.theme.NovaDarkText
import com.example.ui.theme.NovaDarkTileBg
import com.example.ui.theme.NovaLightBg
import com.example.ui.theme.NovaLightCardBg
import com.example.ui.theme.NovaLightCardBorder
import com.example.ui.theme.NovaLightSubtext
import com.example.ui.theme.NovaLightText
import com.example.ui.theme.NovaProGoldEnd
import com.example.ui.theme.NovaProGoldStart
import com.example.viewmodel.GalleryViewModel
import com.example.viewmodel.SortOrder

data class CustomAlbumItem(
  val name: String,
  val countText: String,
  val itemCount: Int,
  val coverUrl: String? = null
)

@Composable
fun AlbumsScreen(
  viewModel: GalleryViewModel,
  onMediaClick: (MediaItem) -> Unit = { viewModel.setViewingItem(it) },
  onOpenVault: () -> Unit = {},
  onOpenVideos: () -> Unit = {},
  onOpenTrash: () -> Unit = {},
  onOpenSearch: () -> Unit = {},
  onOpenSettings: () -> Unit = {},
  onAlbumClick: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allMedia by viewModel.allActiveMedia.collectAsState()
  val isLightMode by viewModel.isLightMode.collectAsState()
  val sortOrder by viewModel.sortOrder.collectAsState()
  val sortAscending by viewModel.sortAscending.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val filteredMedia by viewModel.filteredMedia.collectAsState()

  var openedAlbumName by remember { mutableStateOf<String?>(null) }
  var isHeaderSearchActive by remember { mutableStateOf(false) }
  var isCreateAlbumDialogOpen by remember { mutableStateOf(false) }
  var newAlbumName by remember { mutableStateOf("") }
  var newAlbumType by remember { mutableStateOf("photo") }

  val albumPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty() && newAlbumName.isNotBlank()) {
      val targetAlbum = newAlbumName.trim()
      uris.forEachIndexed { idx, uri ->
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val isVideo = mimeType.startsWith("video/")
        viewModel.importMedia(
          uri = uri.toString(),
          isVideo = isVideo,
          title = "Album_${targetAlbum}_$idx",
          album = targetAlbum
        )
      }
      Toast.makeText(context, "Added ${uris.size} item(s) to $targetAlbum", Toast.LENGTH_SHORT).show()
    }
  }

  BackHandler(enabled = openedAlbumName != null || isHeaderSearchActive) {
    when {
      isHeaderSearchActive -> {
        isHeaderSearchActive = false
        viewModel.setSearchQuery("")
      }
      openedAlbumName != null -> openedAlbumName = null
    }
  }

  val bgColor = if (isLightMode) NovaLightBg else NovaDarkBg
  val textColor = if (isLightMode) NovaLightText else NovaDarkText
  val subtextColor = if (isLightMode) NovaLightSubtext else NovaDarkSubtext
  val shortcutBg = if (isLightMode) NovaLightCardBg else NovaDarkTileBg
  val cardBg = if (isLightMode) NovaLightCardBg else NovaDarkCardBg
  val cardBorder = if (isLightMode) NovaLightCardBorder else NovaDarkCardBorder
  val tileBg = if (isLightMode) Color(0xFFE2E8F0) else NovaDarkTileBg
  val accentColor = Color(0xFF0066FF)

  // "My albums" 3-column list: Recent, Camera, Download(s), plus all dynamic/user folders
  val albumsList = remember(allMedia, sortOrder, sortAscending) {
    val recentItem = CustomAlbumItem(
      name = "Recent",
      countText = "${allMedia.size} items",
      itemCount = allMedia.size,
      coverUrl = allMedia.firstOrNull()?.let { it.thumbnailUri.ifBlank { it.uri } }
    )

    val dynamicAlbums = allMedia.groupBy { it.album }
      .filter { (name, _) -> name.isNotBlank() && !name.equals("SafeFolder", ignoreCase = true) }
      .map { (name, items) ->
        val cover = items.firstOrNull()?.let { it.thumbnailUri.ifBlank { it.uri } }
        CustomAlbumItem(
          name = name,
          countText = "${items.size} items",
          itemCount = items.size,
          coverUrl = cover
        )
      }

    val combined = listOf(recentItem) + dynamicAlbums.filterNot { it.name.equals("Recent", ignoreCase = true) }
    when (sortOrder) {
      SortOrder.NAME -> if (sortAscending) combined.sortedBy { it.name.lowercase() } else combined.sortedByDescending { it.name.lowercase() }
      SortOrder.SIZE -> if (sortAscending) combined.sortedBy { it.itemCount } else combined.sortedByDescending { it.itemCount }
      SortOrder.DATE_ADDED -> combined
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
  ) {
    // 1. Sticky Top Header ("Albums" | [PRO] 🔍 ⚙)
    if (isHeaderSearchActive) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        IconButton(
          onClick = {
            isHeaderSearchActive = false
            viewModel.setSearchQuery("")
          }
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Search", tint = textColor)
        }
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Search name, tag, or date...", fontSize = 13.sp, color = subtextColor) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", tint = subtextColor, modifier = Modifier.size(18.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = cardBorder,
            focusedContainerColor = cardBg,
            unfocusedContainerColor = cardBg,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
          ),
          modifier = Modifier.weight(1f).testTag("albums_search_input")
        )
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (openedAlbumName != null) {
            IconButton(
              onClick = { openedAlbumName = null },
              modifier = Modifier.size(36.dp).testTag("albums_back_btn")
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Albums", tint = textColor)
            }
          }
          Text(
            text = openedAlbumName ?: "Albums",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("albums_header_title")
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Brush.linearGradient(listOf(NovaProGoldStart, NovaProGoldEnd)))
              .clickable {
                viewModel.performStorageScan()
                Toast.makeText(context, "Storage scanned!", Toast.LENGTH_SHORT).show()
              }
              .padding(horizontal = 7.dp, vertical = 3.dp)
          ) {
            Text("PRO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
          }

          IconButton(
            onClick = { viewModel.toggleTheme() },
            modifier = Modifier.size(48.dp).testTag("albums_theme_toggle_btn")
          ) {
            Icon(
              imageVector = if (isLightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
              contentDescription = if (isLightMode) "Switch to Dark Mode" else "Switch to Light Mode",
              tint = textColor,
              modifier = Modifier.size(21.dp)
            )
          }

          IconButton(
            onClick = { isHeaderSearchActive = true },
            modifier = Modifier.size(48.dp).testTag("albums_search_btn")
          ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor, modifier = Modifier.size(21.dp))
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(48.dp).testTag("albums_settings_btn")
          ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = textColor, modifier = Modifier.size(21.dp))
          }
        }
      }
    }

    if (isHeaderSearchActive && searchQuery.isNotBlank()) {
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredMedia, key = { it.id }) { item ->
          AlbumMediaTile(item = item, tileBg = tileBg, onClick = { onMediaClick(item) })
        }
      }
    } else if (openedAlbumName == null) {
      // Main Albums View (Screenshot 1 HTML)
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
          .fillMaxSize()
          .testTag("albumGrid")
      ) {
        // 4-Column Quick Shortcuts: Videos, Private, Favourites, Trash
        item(span = { GridItemSpan(3) }) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            AlbumQuickShortcutCard(
              icon = Icons.Default.Videocam,
              label = "Videos",
              tint = Color(0xFF38BDF8),
              cardBg = shortcutBg,
              cardBorder = cardBorder,
              labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
              onClick = onOpenVideos,
              modifier = Modifier.weight(1f).testTag("shortcut_videos")
            )
            AlbumQuickShortcutCard(
              icon = Icons.Default.Lock,
              label = "Private",
              tint = NovaProGoldStart,
              cardBg = shortcutBg,
              cardBorder = cardBorder,
              labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
              onClick = onOpenVault,
              modifier = Modifier.weight(1f).testTag("shortcut_private")
            )
            AlbumQuickShortcutCard(
              icon = Icons.Default.Favorite,
              label = "Favourites",
              tint = Color(0xFFFF3B30),
              cardBg = shortcutBg,
              cardBorder = cardBorder,
              labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
              onClick = { openedAlbumName = "Favourites" },
              modifier = Modifier.weight(1f).testTag("shortcut_favourites")
            )
            AlbumQuickShortcutCard(
              icon = Icons.Default.Delete,
              label = "Trash",
              tint = Color(0xFFF97316),
              cardBg = shortcutBg,
              cardBorder = cardBorder,
              labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
              onClick = onOpenTrash,
              modifier = Modifier.weight(1f).testTag("shortcut_trash")
            )
          }
        }

        // Section Heading "My albums" + Add/Scan/Sort row
        item(span = { GridItemSpan(3) }) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "My albums",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
              )
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  color = shortcutBg,
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, cardBorder),
                  modifier = Modifier
                    .clickable {
                      viewModel.performStorageScan()
                      Toast.makeText(context, "Storage scanned!", Toast.LENGTH_SHORT).show()
                    }
                    .testTag("albums_scan_btn")
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = "Scan", tint = subtextColor, modifier = Modifier.size(13.dp))
                    Text("Scan", fontSize = 11.sp, color = subtextColor)
                  }
                }
                Surface(
                  color = shortcutBg,
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, cardBorder),
                  modifier = Modifier
                    .clickable {
                      newAlbumName = ""
                      isCreateAlbumDialogOpen = true
                    }
                    .testTag("albums_add_btn")
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = "New Album", tint = accentColor, modifier = Modifier.size(14.dp))
                    Text("New", fontSize = 11.sp, color = textColor)
                  }
                }
              }
            }

            // Sort pills
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              SortOrder.values().forEach { order ->
                val active = sortOrder == order
                Surface(
                  color = if (active) accentColor.copy(alpha = 0.22f) else shortcutBg,
                  shape = RoundedCornerShape(14.dp),
                  border = BorderStroke(1.dp, if (active) accentColor else cardBorder),
                  modifier = Modifier.clickable { viewModel.setSortOrder(order) }
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Text(order.label, fontSize = 11.sp, color = if (active) textColor else subtextColor)
                    if (active) {
                      Icon(
                        imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(11.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // 3-Column Albums Grid (.albums-grid)
        items(albumsList, key = { it.name }) { album ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                openedAlbumName = album.name
                onAlbumClick(album.name)
              }
              .testTag("album_card_${album.name}")
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(tileBg),
              contentAlignment = Alignment.Center
            ) {
              if (album.coverUrl != null) {
                AsyncImage(
                  model = ImageRequest.Builder(context)
                    .data(album.coverUrl)
                    .crossfade(true)
                    .build(),
                  contentDescription = album.name,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Folder,
                  contentDescription = album.name,
                  tint = subtextColor,
                  modifier = Modifier.size(32.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = album.name,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = textColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = album.countText,
              fontSize = 11.sp,
              color = subtextColor,
              maxLines = 1
            )
          }
        }
      }
    } else {
      // Folder Inner Grid View (3-Column)
      val folderItems = remember(openedAlbumName, allMedia, sortOrder, sortAscending) {
        val raw = when (openedAlbumName) {
          "Recent" -> allMedia
          "Favourites", "Favorite" -> allMedia.filter { it.isFavorite }
          else -> allMedia.filter { it.album.equals(openedAlbumName, ignoreCase = true) }
        }
        viewModel.sortMediaList(raw, sortOrder, sortAscending)
      }

      if (folderItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = subtextColor, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No media in $openedAlbumName", fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { openedAlbumName = null },
              colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
              Text("Back to Albums")
            }
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.fillMaxSize().testTag("album_inner_grid")
        ) {
          items(folderItems, key = { it.id }) { item ->
            AlbumMediaTile(item = item, tileBg = tileBg, onClick = { onMediaClick(item) })
          }
        }
      }
    }
  }

  // Create Album Dialog
  if (isCreateAlbumDialogOpen) {
    AlertDialog(
      onDismissRequest = { isCreateAlbumDialogOpen = false },
      containerColor = cardBg,
      title = { Text("Create Album", color = textColor, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Enter a name for your new collection:", fontSize = 13.sp, color = subtextColor)
          OutlinedTextField(
            value = newAlbumName,
            onValueChange = { newAlbumName = it },
            placeholder = { Text("e.g., Travel / Studio") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
              color = if (newAlbumType == "photo") accentColor else bgColor,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.clickable { newAlbumType = "photo" }
            ) {
              Text(
                "Photos",
                color = if (newAlbumType == "photo") Color.White else textColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
            Surface(
              color = if (newAlbumType == "video") accentColor else bgColor,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.clickable { newAlbumType = "video" }
            ) {
              Text(
                "Videos",
                color = if (newAlbumType == "video") Color.White else textColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newAlbumName.isNotBlank()) {
              isCreateAlbumDialogOpen = false
              val mediaFilter = if (newAlbumType == "video") {
                ActivityResultContracts.PickVisualMedia.VideoOnly
              } else {
                ActivityResultContracts.PickVisualMedia.ImageOnly
              }
              albumPickerLauncher.launch(PickVisualMediaRequest(mediaFilter))
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
          Text("Pick Media & Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { isCreateAlbumDialogOpen = false }) {
          Text("Cancel", color = subtextColor)
        }
      }
    )
  }
}

@Composable
private fun AlbumQuickShortcutCard(
  icon: ImageVector,
  label: String,
  tint: Color,
  cardBg: Color,
  cardBorder: Color,
  labelColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = cardBg,
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, cardBorder),
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 14.dp)
    ) {
      Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        color = labelColor,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun AlbumMediaTile(
  item: MediaItem,
  tileBg: Color,
  onClick: () -> Unit
) {
  val context = LocalContext.current
  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(6.dp))
      .background(tileBg)
      .clickable(onClick = onClick)
  ) {
    AsyncImage(
      model = ImageRequest.Builder(context)
        .data(if (item.thumbnailUri.isNotBlank()) item.thumbnailUri else item.uri)
        .crossfade(true)
        .build(),
      contentDescription = item.title,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    if (item.type == MediaType.VIDEO) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(4.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color.Black.copy(alpha = 0.65f))
          .padding(horizontal = 4.dp, vertical = 2.dp)
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(item.formattedDuration.ifEmpty { "0:30" }, color = Color.White, fontSize = 9.sp)
      }
    }
  }
}
