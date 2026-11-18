package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.delay

private data class DiscoverCategory(
  val label: String,
  val coverUri: String,
  val items: List<MediaItem>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
  viewModel: GalleryViewModel,
  onMediaClick: (MediaItem) -> Unit,
  onOpenVideos: () -> Unit,
  onOpenVault: () -> Unit,
  onOpenTrash: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allMedia by viewModel.allActiveMedia.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedTag by viewModel.selectedTag.collectAsState()
  val filteredMedia by viewModel.filteredMedia.collectAsState()
  val isLightMode by viewModel.isLightMode.collectAsState()

  var isHeaderSearchActive by remember { mutableStateOf(false) }
  var activeDiscoverCategory by remember { mutableStateOf<DiscoverCategory?>(null) }

  // Creation / Smart Features Dialog states
  var isAdvancedEditPickerOpen by remember { mutableStateOf(false) }
  var isCollageBuilderOpen by remember { mutableStateOf(false) }
  var isGifMakerOpen by remember { mutableStateOf(false) }
  var isSlideshowOpen by remember { mutableStateOf(false) }
  var isPhotoMapOpen by remember { mutableStateOf(false) }
  var isSmartCleanupOpen by remember { mutableStateOf(false) }

  BackHandler(
    enabled = isHeaderSearchActive || activeDiscoverCategory != null ||
      isPhotoMapOpen || isSmartCleanupOpen || isSlideshowOpen
  ) {
    when {
      isSlideshowOpen -> isSlideshowOpen = false
      isPhotoMapOpen -> isPhotoMapOpen = false
      isSmartCleanupOpen -> isSmartCleanupOpen = false
      activeDiscoverCategory != null -> activeDiscoverCategory = null
      isHeaderSearchActive -> {
        isHeaderSearchActive = false
        viewModel.setSearchQuery("")
      }
    }
  }

  val bgColor = if (isLightMode) NovaLightBg else NovaDarkBg
  val textColor = if (isLightMode) NovaLightText else NovaDarkText
  val subtextColor = if (isLightMode) NovaLightSubtext else NovaDarkSubtext
  val featureCardBg = if (isLightMode) NovaLightCardBg else NovaDarkTileBg
  val cardBg = if (isLightMode) NovaLightCardBg else NovaDarkCardBg
  val cardBorder = if (isLightMode) NovaLightCardBorder else NovaDarkCardBorder
  val tileBg = if (isLightMode) Color(0xFFE2E8F0) else NovaDarkTileBg
  val accentColor = Color(0xFF0066FF)

  val photoMedia = remember(allMedia) { allMedia.filter { it.type == MediaType.PHOTO } }
  val allTags = remember(allMedia) { allMedia.flatMap { it.tags }.distinct().sorted() }

  // Dynamic Discover Categories (Scenery, Nature, Architecture, Travel, Videos...)
  val discoverCategories = remember(allMedia) {
    val buckets = linkedMapOf<String, MutableList<MediaItem>>()
    allMedia.forEach { item ->
      val key = when {
        item.tags.any { it.contains("Nature", true) || it.contains("Coast", true) || it.contains("Ocean", true) } -> "Nature"
        item.tags.any { it.contains("House", true) || it.contains("Villa", true) || it.contains("Interior", true) } -> "Architecture"
        item.tags.any { it.contains("Beach", true) || it.contains("Travel", true) } -> "Scenery"
        item.type == MediaType.VIDEO -> "Videos"
        else -> item.album.ifBlank { "Gallery" }
      }
      buckets.getOrPut(key) { mutableListOf() }.add(item)
    }
    buckets.mapNotNull { (label, items) ->
      val cover = items.firstOrNull()?.let { it.thumbnailUri.ifBlank { it.uri } }
      if (cover != null) DiscoverCategory(label, cover, items) else null
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
  ) {
    // 1. Sticky Top Header ("Explore" | [PRO] 🔍 ⚙)
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
          placeholder = { Text("Search name, tags, or date...", fontSize = 13.sp, color = subtextColor) },
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
          modifier = Modifier.weight(1f).testTag("explore_search_input")
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
          if (activeDiscoverCategory != null) {
            IconButton(
              onClick = { activeDiscoverCategory = null },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }
          }
          Text(
            text = activeDiscoverCategory?.label ?: "Explore",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.testTag("explore_header_title")
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
            modifier = Modifier.size(48.dp).testTag("explore_theme_toggle_btn")
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
            modifier = Modifier.size(48.dp).testTag("explore_search_btn")
          ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor, modifier = Modifier.size(21.dp))
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(48.dp).testTag("explore_settings_btn")
          ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = textColor, modifier = Modifier.size(21.dp))
          }
        }
      }
    }

    // Search results mode or Discover category drilldown
    if ((isHeaderSearchActive && searchQuery.isNotBlank()) || activeDiscoverCategory != null) {
      val displayItems = activeDiscoverCategory?.items ?: filteredMedia
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(displayItems, key = { it.id }) { item ->
          Box(
            modifier = Modifier
              .aspectRatio(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(tileBg)
              .clickable { onMediaClick(item) }
          ) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(item.thumbnailUri.ifBlank { item.uri })
                .crossfade(true)
                .build(),
              contentDescription = item.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }
        }
      }
    } else {
      // Main Explore Screen Content (Matching Screenshot 2 HTML)
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {
        // 1. Discover photos (Horizontal Cards 130dp x 160dp)
        if (discoverCategories.isNotEmpty()) {
          item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Discover photos",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
              )
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("explore_discover_row")
              ) {
                items(discoverCategories, key = { it.label }) { cat ->
                  Box(
                    modifier = Modifier
                      .width(130.dp)
                      .height(160.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(tileBg)
                      .clickable { activeDiscoverCategory = cat }
                      .testTag("discover_card_${cat.label.lowercase()}")
                  ) {
                    AsyncImage(
                      model = ImageRequest.Builder(context)
                        .data(cat.coverUri)
                        .crossfade(true)
                        .build(),
                      contentDescription = cat.label,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .background(
                          Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 180f
                          )
                        )
                    )
                    Column(
                      modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                    ) {
                      Text(
                        text = cat.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                      Text(
                        text = "${cat.items.size} items",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // 2. Creation 4-Column Grid (Advanced Edit, Collage, GIF Maker, Slideshow)
        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Creation",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = textColor
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              CreationToolCard(
                icon = Icons.Default.Edit,
                label = "Advanced Edit",
                cardBg = featureCardBg,
                cardBorder = cardBorder,
                labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
                iconTint = Color(0xFF38BDF8),
                onClick = { isAdvancedEditPickerOpen = true },
                modifier = Modifier.weight(1f).testTag("creation_advanced_edit")
              )
              CreationToolCard(
                icon = Icons.Default.GridView,
                label = "Collage",
                cardBg = featureCardBg,
                cardBorder = cardBorder,
                labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
                iconTint = Color(0xFFA855F7),
                onClick = { isCollageBuilderOpen = true },
                modifier = Modifier.weight(1f).testTag("creation_collage")
              )
              CreationToolCard(
                icon = Icons.Default.Gif,
                label = "GIF Maker",
                cardBg = featureCardBg,
                cardBorder = cardBorder,
                labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
                iconTint = Color(0xFF10B981),
                onClick = { isGifMakerOpen = true },
                modifier = Modifier.weight(1f).testTag("creation_gif")
              )
              CreationToolCard(
                icon = Icons.Default.Slideshow,
                label = "Slideshow",
                cardBg = featureCardBg,
                cardBorder = cardBorder,
                labelColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCCCCCC),
                iconTint = NovaProGoldStart,
                onClick = { isSlideshowOpen = true },
                modifier = Modifier.weight(1f).testTag("creation_slideshow")
              )
            }
          }
        }

        // 3. Feature Boxes: Photo Map & Smart Cleanup
        item {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExploreFeatureBox(
              title = "Photo Map",
              subtitle = "Browse photos by where they were taken",
              icon = Icons.Default.Map,
              iconTint = Color(0xFF38BDF8),
              cardBg = featureCardBg,
              cardBorder = cardBorder,
              textColor = textColor,
              subtextColor = subtextColor,
              onClick = { isPhotoMapOpen = true },
              modifier = Modifier.testTag("feature_photo_map")
            )
            ExploreFeatureBox(
              title = "Smart Cleanup",
              subtitle = "Find similar photos, screenshots & large videos",
              icon = Icons.Default.CleaningServices,
              iconTint = Color(0xFFF59E0B),
              cardBg = featureCardBg,
              cardBorder = cardBorder,
              textColor = textColor,
              subtextColor = subtextColor,
              onClick = { isSmartCleanupOpen = true },
              modifier = Modifier.testTag("feature_smart_cleanup")
            )
          }
        }

        // 4. Tags & Topics Browser
        if (allTags.isNotEmpty()) {
          item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Tags & Topics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
              )
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                ElevatedFilterChip(
                  selected = selectedTag == null,
                  onClick = { viewModel.setSelectedTag(null) },
                  label = { Text("All", fontSize = 12.sp) },
                  colors = FilterChipDefaults.elevatedFilterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = Color.White,
                    containerColor = featureCardBg,
                    labelColor = textColor
                  )
                )
                allTags.forEach { tag ->
                  val isSelected = selectedTag.equals(tag, ignoreCase = true)
                  ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSelectedTag(if (isSelected) null else tag) },
                    label = { Text(tag, fontSize = 12.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                      selectedContainerColor = accentColor,
                      selectedLabelColor = Color.White,
                      containerColor = featureCardBg,
                      labelColor = textColor
                    )
                  )
                }
              }

              if (selectedTag != null && filteredMedia.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val rows = filteredMedia.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  rows.forEach { rowItems ->
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      rowItems.forEach { item ->
                        Box(
                          modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(tileBg)
                            .clickable { onMediaClick(item) }
                        ) {
                          AsyncImage(
                            model = ImageRequest.Builder(context)
                              .data(item.thumbnailUri.ifBlank { item.uri })
                              .crossfade(true)
                              .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                          )
                        }
                      }
                      repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // --- Dialog 1: Pick Photo for Advanced Edit ---
  if (isAdvancedEditPickerOpen) {
    AlertDialog(
      onDismissRequest = { isAdvancedEditPickerOpen = false },
      containerColor = cardBg,
      title = { Text("Advanced Edit • Choose Photo", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
      text = {
        if (photoMedia.isEmpty()) {
          Text("No photos available to edit.", color = subtextColor)
        } else {
          LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(260.dp)
          ) {
            items(photoMedia, key = { it.id }) { photo ->
              Box(
                modifier = Modifier
                  .aspectRatio(1f)
                  .clip(RoundedCornerShape(6.dp))
                  .background(tileBg)
                  .clickable {
                    isAdvancedEditPickerOpen = false
                    onMediaClick(photo)
                  }
              ) {
                AsyncImage(
                  model = ImageRequest.Builder(context).data(photo.uri).crossfade(true).build(),
                  contentDescription = photo.title,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { isAdvancedEditPickerOpen = false }) {
          Text("Cancel", color = subtextColor)
        }
      }
    )
  }

  // --- Dialog 2: Interactive Collage Studio ---
  if (isCollageBuilderOpen) {
    val selectedForCollage = remember {
      mutableStateListOf<MediaItem>().apply {
        addAll(photoMedia.take(4))
      }
    }
    AlertDialog(
      onDismissRequest = { isCollageBuilderOpen = false },
      containerColor = cardBg,
      title = { Text("Photo Collage Studio", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (selectedForCollage.isEmpty()) {
            Text("Tap photos below to add to collage:", color = subtextColor, fontSize = 12.sp)
          } else {
            // Live Collage Preview Grid
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
                .padding(3.dp)
            ) {
              val preview = selectedForCollage.take(4)
              Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                  preview.take(2).forEach { p ->
                    AsyncImage(
                      model = ImageRequest.Builder(context).data(p.uri).build(),
                      contentDescription = p.title,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(6.dp))
                    )
                  }
                }
                if (preview.size > 2) {
                  Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    preview.drop(2).forEach { p ->
                      AsyncImage(
                        model = ImageRequest.Builder(context).data(p.uri).build(),
                        contentDescription = p.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(6.dp))
                      )
                    }
                  }
                }
              }
            }
          }
          Text("Tap photos to toggle (${selectedForCollage.size}/4):", fontSize = 11.sp, color = subtextColor)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(photoMedia, key = { it.id }) { p ->
              val picked = selectedForCollage.any { it.id == p.id }
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .then(if (picked) Modifier.border(2.dp, accentColor, RoundedCornerShape(8.dp)) else Modifier)
                  .clickable {
                    if (picked) {
                      selectedForCollage.removeAll { it.id == p.id }
                    } else if (selectedForCollage.size < 4) {
                      selectedForCollage.add(p)
                    }
                  }
              ) {
                AsyncImage(
                  model = ImageRequest.Builder(context).data(p.uri).build(),
                  contentDescription = p.title,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
                if (picked) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier
                      .align(Alignment.TopEnd)
                      .padding(3.dp)
                      .size(14.dp)
                      .background(accentColor, CircleShape)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            Toast.makeText(context, "Collage ready (${selectedForCollage.size} photos)", Toast.LENGTH_SHORT).show()
            isCollageBuilderOpen = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
          Text("Done")
        }
      },
      dismissButton = {
        TextButton(onClick = { isCollageBuilderOpen = false }) { Text("Close", color = subtextColor) }
      }
    )
  }

  // --- Dialog 3: Animated GIF Maker Previewer ---
  if (isGifMakerOpen) {
    val frames = remember(photoMedia) { photoMedia.take(6) }
    var frameIndex by remember { mutableIntStateOf(0) }
    var speedMs by remember { mutableIntStateOf(500) }
    LaunchedEffect(frames.size, speedMs) {
      if (frames.isNotEmpty()) {
        while (true) {
          delay(speedMs.toLong())
          frameIndex = (frameIndex + 1) % frames.size
        }
      }
    }
    AlertDialog(
      onDismissRequest = { isGifMakerOpen = false },
      containerColor = cardBg,
      title = { Text("GIF Maker Preview", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          if (frames.isEmpty()) {
            Text("Add photos to create an animated GIF.", color = subtextColor)
          } else {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
            ) {
              AsyncImage(
                model = ImageRequest.Builder(context).data(frames[frameIndex].uri).build(),
                contentDescription = "GIF Frame",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
              Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
              ) {
                Text(
                  text = "GIF • Frame ${frameIndex + 1}/${frames.size}",
                  color = Color.White,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(800 to "Slow", 500 to "Normal", 250 to "Fast").forEach { (ms, label) ->
                Surface(
                  color = if (speedMs == ms) accentColor else featureCardBg,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.clickable { speedMs = ms }
                ) {
                  Text(
                    label,
                    color = if (speedMs == ms) Color.White else subtextColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            Toast.makeText(context, "GIF loop exported at ${1000 / speedMs} fps", Toast.LENGTH_SHORT).show()
            isGifMakerOpen = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
          Text("Export GIF")
        }
      },
      dismissButton = {
        TextButton(onClick = { isGifMakerOpen = false }) { Text("Close", color = subtextColor) }
      }
    )
  }

  // --- Dialog 4: Full-Screen Slideshow ---
  if (isSlideshowOpen) {
    var currentIdx by remember { mutableIntStateOf(0) }
    var isAutoPlaying by remember { mutableStateOf(true) }
    LaunchedEffect(isAutoPlaying, photoMedia.size) {
      if (isAutoPlaying && photoMedia.isNotEmpty()) {
        while (true) {
          delay(2500)
          currentIdx = (currentIdx + 1) % photoMedia.size
        }
      }
    }
    Dialog(
      onDismissRequest = { isSlideshowOpen = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black)
      ) {
        if (photoMedia.isEmpty()) {
          Text("No photos for slideshow", color = Color.White, modifier = Modifier.align(Alignment.Center))
        } else {
          val currentPhoto = photoMedia[currentIdx.coerceIn(0, photoMedia.lastIndex)]
          Crossfade(targetState = currentPhoto, label = "SlideshowCrossfade") { photo ->
            AsyncImage(
              model = ImageRequest.Builder(context).data(photo.uri).build(),
              contentDescription = photo.title,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize()
            )
          }

          Row(
            modifier = Modifier
              .align(Alignment.TopCenter)
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(currentPhoto.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("${currentIdx + 1} / ${photoMedia.size} • ${currentPhoto.location}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            IconButton(onClick = { isSlideshowOpen = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close Slideshow", tint = Color.White)
            }
          }

          Row(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 36.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(Color.Black.copy(alpha = 0.65f))
              .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(onClick = {
              currentIdx = if (currentIdx > 0) currentIdx - 1 else photoMedia.lastIndex
            }) {
              Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
            }
            IconButton(onClick = { isAutoPlaying = !isAutoPlaying }) {
              Icon(
                if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isAutoPlaying) "Pause" else "Play",
                tint = Color.White
              )
            }
            IconButton(onClick = {
              currentIdx = (currentIdx + 1) % photoMedia.size
            }) {
              Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
            }
          }
        }
      }
    }
  }

  // --- Dialog 5: Photo Map (Browse by Location) ---
  if (isPhotoMapOpen) {
    val groupedByPlace = remember(allMedia) {
      allMedia.groupBy { it.location.ifBlank { "Device Storage" } }
    }
    AlertDialog(
      onDismissRequest = { isPhotoMapOpen = false },
      containerColor = cardBg,
      title = { Text("Photo Map • Places", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
      text = {
        LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          groupedByPlace.forEach { (place, items) ->
            item(key = place) {
              Surface(
                color = featureCardBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    isPhotoMapOpen = false
                    items.firstOrNull()?.let(onMediaClick)
                  }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(48.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(tileBg)
                  ) {
                    AsyncImage(
                      model = ImageRequest.Builder(context)
                        .data(items.first().let { it.thumbnailUri.ifBlank { it.uri } })
                        .build(),
                      contentDescription = place,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(place, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    Text("${items.size} media items", fontSize = 11.sp, color = subtextColor)
                  }
                  Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { isPhotoMapOpen = false }) { Text("Close", color = accentColor) }
      }
    )
  }

  // --- Dialog 6: Smart Cleanup Analyzer ---
  if (isSmartCleanupOpen) {
    val largeFiles = remember(allMedia) {
      allMedia.filter { it.sizeBytes >= 15_000_000L }.sortedByDescending { it.sizeBytes }
    }
    AlertDialog(
      onDismissRequest = { isSmartCleanupOpen = false },
      containerColor = cardBg,
      title = { Text("Smart Cleanup", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Large files (15 MB+) ready for review:",
            fontSize = 12.sp,
            color = subtextColor
          )
          if (largeFiles.isEmpty()) {
            Text("Storage looks clean! No oversized files detected.", color = Color(0xFF10B981), fontSize = 13.sp)
          } else {
            LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(largeFiles, key = { it.id }) { file ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(featureCardBg)
                    .padding(10.dp)
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(file.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${file.formattedSize} • ${file.album}", fontSize = 11.sp, color = subtextColor)
                  }
                  IconButton(onClick = {
                    viewModel.moveToTrash(file.id)
                    Toast.makeText(context, "Cleaned ${file.title}", Toast.LENGTH_SHORT).show()
                  }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clean file", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { isSmartCleanupOpen = false }) { Text("Done", color = accentColor) }
      }
    )
  }
}

@Composable
private fun CreationToolCard(
  icon: ImageVector,
  label: String,
  cardBg: Color,
  cardBorder: Color,
  labelColor: Color,
  iconTint: Color,
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
      Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(20.dp))
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
private fun ExploreFeatureBox(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  cardBg: Color,
  cardBorder: Color,
  textColor: Color,
  subtextColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = cardBg,
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, cardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = subtextColor
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
    }
  }
}
