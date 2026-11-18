package com.example.ui.screens

import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ota.OtaSyncStatus
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class TimelineRowEntry(
  val sectionTitle: String?,
  val items: List<MediaItem>,
  val isFirstRowInSection: Boolean,
  val isLastRowInSection: Boolean
)

@Composable
fun GalleryHomeScreen(
  viewModel: GalleryViewModel,
  onMediaClick: (MediaItem) -> Unit,
  onOpenVault: () -> Unit,
  onOpenSearch: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val filteredMedia by viewModel.filteredMedia.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val sortOrder by viewModel.sortOrder.collectAsState()
  val sortAscending by viewModel.sortAscending.collectAsState()
  val selectedAlbum by viewModel.selectedAlbum.collectAsState()
  val selectedIds by viewModel.selectedMediaIds.collectAsState()
  val isSelectionMode by viewModel.isSelectionMode.collectAsState()
  val isLightMode by viewModel.isLightMode.collectAsState()
  val isStorageScanned by viewModel.isStorageScanned.collectAsState()
  val isScanningNow by viewModel.isScanningNow.collectAsState()
  val otaState by viewModel.otaState.collectAsState()

  var isHeaderSearchActive by remember { mutableStateOf(false) }
  var isProSheetOpen by remember { mutableStateOf(false) }
  var isOtaBannerDismissed by remember { mutableStateOf(false) }

  // Zero-permission Photo Picker to import real photos/videos
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      uris.forEachIndexed { idx, uri ->
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val isVideo = mimeType.startsWith("video/")
        val fileName = "Media_${System.currentTimeMillis() % 10000}_$idx"
        viewModel.importMedia(uri.toString(), isVideo = isVideo, title = fileName, album = selectedAlbum ?: "Camera")
      }
      Toast.makeText(context, "${uris.size} item(s) added to Ayaz Gallery", Toast.LENGTH_SHORT).show()
    }
  }

  BackHandler(enabled = isHeaderSearchActive || isSelectionMode || selectedAlbum != null) {
    when {
      isSelectionMode -> viewModel.clearSelection()
      isHeaderSearchActive -> {
        isHeaderSearchActive = false
        viewModel.setSearchQuery("")
      }
      selectedAlbum != null -> viewModel.setSelectedAlbum(null)
    }
  }

  // Color system matching HTML (#0b0f15 dark / #f5f6fa light)
  val bgColor = if (isLightMode) NovaLightBg else NovaDarkBg
  val textColor = if (isLightMode) NovaLightText else NovaDarkText
  val subtextColor = if (isLightMode) NovaLightSubtext else NovaDarkSubtext
  val cardBg = if (isLightMode) NovaLightCardBg else NovaDarkCardBg
  val cardBorder = if (isLightMode) NovaLightCardBorder else NovaDarkCardBorder
  val tileBg = if (isLightMode) Color(0xFFD1FAE5) else NovaDarkTileBg
  val accentColor = remember(otaState.accentColorHex) {
    runCatching {
      Color(android.graphics.Color.parseColor(otaState.accentColorHex))
    }.getOrDefault(Color(0xFF10B981))
  }

  // Group timeline media into date sections ("Today", "Yesterday", "Nov 10") or sort group
  val timelineRows = remember(filteredMedia, sortOrder, sortAscending) {
    if (filteredMedia.isEmpty()) {
      emptyList()
    } else if (sortOrder == SortOrder.DATE_ADDED) {
      val grouped = linkedMapOf<String, MutableList<MediaItem>>()
      filteredMedia.forEach { item ->
        val label = formatSectionDateLabel(item.dateAdded)
        grouped.getOrPut(label) { mutableListOf() }.add(item)
      }
      buildList {
        grouped.forEach { (sectionLabel, itemsInGroup) ->
          val chunks = itemsInGroup.chunked(3)
          chunks.forEachIndexed { idx, rowItems ->
            add(
              TimelineRowEntry(
                sectionTitle = if (idx == 0) sectionLabel else null,
                items = rowItems,
                isFirstRowInSection = idx == 0,
                isLastRowInSection = idx == chunks.lastIndex
              )
            )
          }
        }
      }
    } else {
      val headerTitle = when (sortOrder) {
        SortOrder.NAME -> if (sortAscending) "Sorted by Name (A–Z)" else "Sorted by Name (Z–A)"
        SortOrder.SIZE -> if (sortAscending) "Sorted by Size (Smallest)" else "Sorted by Size (Largest)"
        SortOrder.DATE_ADDED -> "Recent"
      }
      val chunks = filteredMedia.chunked(3)
      chunks.mapIndexed { idx, rowItems ->
        TimelineRowEntry(
          sectionTitle = if (idx == 0) headerTitle else null,
          items = rowItems,
          isFirstRowInSection = idx == 0,
          isLastRowInSection = idx == chunks.lastIndex
        )
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
  ) {
    // 1. Sticky Top Header (.header: Photos | [PRO] 🔍 ⚙)
    if (isSelectionMode) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(cardBg)
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { viewModel.clearSelection() }) {
            Icon(Icons.Default.Close, contentDescription = "Cancel Selection", tint = textColor)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${selectedIds.size} Selected",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { viewModel.selectAll() }) {
            Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = textColor)
          }
          IconButton(onClick = {
            viewModel.moveSelectedToVault()
            Toast.makeText(context, "Moved to Safe Vault", Toast.LENGTH_SHORT).show()
          }) {
            Icon(Icons.Default.Lock, contentDescription = "Move to Vault", tint = NovaProGoldStart)
          }
          IconButton(onClick = {
            viewModel.deletePermanently(selectedIds.toList())
            viewModel.clearSelection()
            Toast.makeText(context, "Deleted permanently", Toast.LENGTH_SHORT).show()
          }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
          }
        }
      }
    } else if (isHeaderSearchActive) {
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
          },
          modifier = Modifier.testTag("header_search_close_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Close Search",
            tint = textColor
          )
        }

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = {
            Text(
              text = "Search file name, tag, or date...",
              fontSize = 13.sp,
              color = subtextColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = subtextColor, modifier = Modifier.size(18.dp))
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
          modifier = Modifier
            .weight(1f)
            .testTag("header_search_input")
        )

        IconButton(
          onClick = onOpenSearch,
          modifier = Modifier.testTag("header_tag_explorer_btn")
        ) {
          Icon(Icons.Default.Tag, contentDescription = "Tag Explorer", tint = accentColor)
        }
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left: Back arrow if inside album, otherwise crisp "Photos" title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (selectedAlbum != null) {
            IconButton(
              onClick = { viewModel.setSelectedAlbum(null) },
              modifier = Modifier.size(36.dp).testTag("header_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "All Photos",
                tint = textColor
              )
            }
          }
          Column {
            Text(
              text = selectedAlbum ?: otaState.appTitle,
              fontSize = 23.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.6.sp,
              color = if (selectedAlbum != null) textColor else accentColor,
              modifier = Modifier.testTag("header_gallery_title")
            )
            Text(
              text = otaState.splashSubtitle,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              style = TextStyle(
                brush = Brush.linearGradient(
                  colors = listOf(Color(0xFF34D399), Color(0xFF22D3EE))
                )
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            // Passive Automatic Background Sync Status + Live Media Count Badge
            val (otaBadgeColor, otaBadgeText) = when (otaState.status) {
              OtaSyncStatus.CHECKING -> Color(0xFF34D399) to "Auto-Checking..."
              OtaSyncStatus.SYNCING -> Color(0xFF22D3EE) to "Syncing v${otaState.version}..."
              OtaSyncStatus.UPDATED -> accentColor to "v${otaState.version} (Live)"
              OtaSyncStatus.OFFLINE -> Color(0xFFF59E0B) to "v${otaState.version} (Cached)"
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(top = 4.dp)
            ) {
              Surface(
                color = otaBadgeColor.copy(alpha = 0.16f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, otaBadgeColor.copy(alpha = 0.35f)),
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .testTag("ota_status_pill")
              ) {
                Text(
                  text = otaBadgeText,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = otaBadgeColor,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
              Text(
                text = if (isScanningNow) "Scanning..." else "${filteredMedia.size} items loaded",
                fontSize = 11.sp,
                color = subtextColor,
                modifier = Modifier.testTag("media_count_label")
              )
            }
          }
        }

        // Right: PRO badge + Theme Toggle + Search + Settings
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(NovaProGoldStart, NovaProGoldEnd)
                )
              )
              .clickable { isProSheetOpen = true }
              .padding(horizontal = 7.dp, vertical = 3.dp)
              .testTag("header_pro_badge")
          ) {
            Text(
              text = "PRO",
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.Black,
              letterSpacing = 0.5.sp
            )
          }

          IconButton(
            onClick = { viewModel.toggleTheme() },
            modifier = Modifier.size(48.dp).testTag("header_theme_toggle_btn")
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
            modifier = Modifier.size(48.dp).testTag("header_search_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search Photos",
              tint = textColor,
              modifier = Modifier.size(21.dp)
            )
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(48.dp).testTag("header_settings_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = textColor,
              modifier = Modifier.size(21.dp)
            )
          }
        }
      }
    }

    // 2. Slim Sort & Quick Tools Row (Date Added, Name, Size + Add Photo shortcut)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .testTag("gallery_sort_bar"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SortOrder.values().forEach { order ->
        val isSelected = sortOrder == order
        Surface(
          color = if (isSelected) accentColor.copy(alpha = 0.22f) else cardBg,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, if (isSelected) accentColor else cardBorder),
          modifier = Modifier
            .clickable { viewModel.setSortOrder(order) }
            .testTag("sort_chip_${order.name.lowercase()}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = order.label,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
              color = if (isSelected) Color.White.let { if (isLightMode) accentColor else it } else subtextColor
            )
            if (isSelected) {
              Icon(
                imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = if (sortAscending) "Ascending" else "Descending",
                tint = if (isLightMode) accentColor else Color.White,
                modifier = Modifier.size(12.dp)
              )
            }
          }
        }
      }

      // Quick Add Media chip (Photo Picker)
      Surface(
        color = cardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
          .clickable {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
          }
          .testTag("quick_import_chip")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = "Import Media",
            tint = subtextColor,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = "Import",
            fontSize = 11.sp,
            color = subtextColor
          )
        }
      }
    }

    // 2b. Dynamic OTA Feature Banner (#ota-dynamic-container)
    val hasOtaBanner = !isOtaBannerDismissed && (
      !otaState.announcementTitle.isNullOrBlank() ||
      !otaState.announcementBody.isNullOrBlank() ||
      otaState.featureBadges.isNotEmpty()
    )
    if (hasOtaBanner && otaState.status == OtaSyncStatus.UPDATED) {
      val parsedOtaAccent = remember(otaState.accentColorHex) {
        runCatching {
          otaState.accentColorHex?.let { Color(android.graphics.Color.parseColor(it)) }
        }.getOrNull() ?: accentColor
      }
      Surface(
        color = cardBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, parsedOtaAccent.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("ota_dynamic_container")
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = otaState.announcementTitle ?: "OTA Update v${otaState.version}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = parsedOtaAccent
            )
            IconButton(
              onClick = { isOtaBannerDismissed = true },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss OTA Banner",
                tint = subtextColor,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          if (!otaState.announcementBody.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = otaState.announcementBody!!,
              fontSize = 11.sp,
              color = subtextColor,
              lineHeight = 16.sp
            )
          }
          if (otaState.featureBadges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              otaState.featureBadges.forEach { badge ->
                Surface(
                  color = parsedOtaAccent.copy(alpha = 0.14f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = parsedOtaAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Date-Grouped 3-Column Photo & Media Grid or Scanning State
    if (isScanningNow) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = accentColor, modifier = Modifier.size(38.dp))
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Scanning photos and videos...",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = subtextColor,
            modifier = Modifier.testTag("scanning_progress_text")
          )
        }
      }
    } else if (!isStorageScanned) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          color = if (isLightMode) Color(0xFFECFDF5) else Color(0x2610B981),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("inline_permission_banner")
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(18.dp)
          ) {
            Text(
              text = "Storage permission required to scan device photos & videos automatically.",
              fontSize = 14.sp,
              color = textColor,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = { viewModel.performStorageScan() },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
              ),
              modifier = Modifier.testTag("grant_permission_rescan_btn")
            ) {
              Text(
                text = "Grant Permission & Scan",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    } else if (timelineRows.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = subtextColor.copy(alpha = 0.5f),
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = if (searchQuery.isNotBlank()) "No matching media found" else "No photos or videos found.",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = subtextColor,
            modifier = Modifier.testTag("empty_media_text")
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = {
              if (searchQuery.isNotBlank()) {
                viewModel.setSearchQuery("")
              } else {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
              }
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier.testTag("empty_import_media_btn")
          ) {
            Text(if (searchQuery.isNotBlank()) "Clear Search" else "Import Photos / Videos")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("photos_timeline_list"),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        itemsIndexed(
          items = timelineRows,
          key = { index, row -> "${row.sectionTitle ?: ""}_${row.items.firstOrNull()?.id ?: index}" }
        ) { _, row ->
          if (row.sectionTitle != null) {
            Text(
              text = row.sectionTitle,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = subtextColor,
              modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 4.dp)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            row.items.forEach { mediaItem ->
              TimelineSquareTile(
                item = mediaItem,
                tileBg = tileBg,
                isLightMode = isLightMode,
                isSelected = selectedIds.contains(mediaItem.id),
                isSelectionMode = isSelectionMode,
                accentColor = accentColor,
                onClick = {
                  if (isSelectionMode) {
                    viewModel.toggleSelection(mediaItem.id)
                  } else {
                    onMediaClick(mediaItem)
                  }
                },
                onLongClick = { viewModel.toggleSelection(mediaItem.id) },
                onQuickDelete = {
                  viewModel.moveToTrash(mediaItem.id)
                  Toast.makeText(context, "Moved to Trash", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
              )
            }
            repeat(3 - row.items.size) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }

  // PRO Quick Tools Dialog
  if (isProSheetOpen) {
    AlertDialog(
      onDismissRequest = { isProSheetOpen = false },
      containerColor = cardBg,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Brush.linearGradient(listOf(NovaProGoldStart, NovaProGoldEnd)))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text("PRO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
          }
          Text("${otaState.appTitle} Pro Tools", fontWeight = FontWeight.Bold, color = textColor, fontSize = 17.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          ProToolActionRow(
            icon = Icons.Default.DocumentScanner,
            title = "Scan Device Media",
            subtitle = "Refresh timeline & persistent cache",
            textColor = textColor,
            subtextColor = subtextColor,
            onClick = {
              isProSheetOpen = false
              viewModel.performStorageScan()
              Toast.makeText(context, "Media library synced", Toast.LENGTH_SHORT).show()
            }
          )
          ProToolActionRow(
            icon = Icons.Default.Lock,
            title = "Open Safe Vault",
            subtitle = "PIN & biometric private storage",
            textColor = textColor,
            subtextColor = subtextColor,
            onClick = {
              isProSheetOpen = false
              onOpenVault()
            }
          )
          ProToolActionRow(
            icon = Icons.Default.AddPhotoAlternate,
            title = "Import Photos / Videos",
            subtitle = "Select items using system Photo Picker",
            textColor = textColor,
            subtextColor = subtextColor,
            onClick = {
              isProSheetOpen = false
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
              )
            }
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { isProSheetOpen = false }) {
          Text("Close", color = NovaProGoldStart)
        }
      }
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineSquareTile(
  item: MediaItem,
  tileBg: Color,
  isLightMode: Boolean,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  accentColor: Color,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onQuickDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressScale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1f,
    label = "TileScale"
  )
  val tileShape = RoundedCornerShape(16.dp)
  val borderColor = if (isSelected) Color(0xFF34D399) else accentColor
  val borderWidth = if (isSelected) 2.5.dp else 1.5.dp

  val colorFilter = when (item.filterApplied) {
    "MONO" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    "SEPIA" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
      0.393f, 0.769f, 0.189f, 0f, 0f,
      0.349f, 0.686f, 0.168f, 0f, 0f,
      0.272f, 0.534f, 0.131f, 0f, 0f,
      0f, 0f, 0f, 1f, 0f
    )))
    "VIVID" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.6f) })
    else -> null
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .graphicsLayer {
        scaleX = pressScale
        scaleY = pressScale
      }
      .shadow(
        elevation = 6.dp,
        shape = tileShape,
        spotColor = accentColor.copy(alpha = 0.28f),
        ambientColor = accentColor.copy(alpha = 0.18f)
      )
      .clip(tileShape)
      .background(
        if (isLightMode) {
          Brush.linearGradient(listOf(tileBg, Color.White))
        } else {
          Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        }
      )
      .border(borderWidth, borderColor, tileShape)
      .combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
        onLongClick = onLongClick
      )
      .testTag("timeline_tile_${item.id}")
  ) {
    AsyncImage(
      model = ImageRequest.Builder(context)
        .data(if (item.thumbnailUri.isNotBlank()) item.thumbnailUri else item.uri)
        .crossfade(true)
        .build(),
      contentDescription = item.title,
      contentScale = ContentScale.Crop,
      colorFilter = colorFilter,
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer(
          scaleX = if (item.filterApplied == "FLIP_H" || item.filterApplied == "MIRROR") -1f else 1f,
          scaleY = if (item.filterApplied == "FLIP_V") -1f else 1f,
          rotationZ = item.rotationDegrees
        )
    )

    // Minimal Video duration indicator bottom-left
    if (item.type == MediaType.VIDEO) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(5.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color.Black.copy(alpha = 0.65f))
          .padding(horizontal = 5.dp, vertical = 2.dp)
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
          text = item.formattedDuration.ifEmpty { "0:30" },
          color = Color.White,
          fontSize = 9.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    if (!isSelectionMode) {
      // Quick Trash button (.delete-btn) at TopEnd with 48dp accessible touch target
      Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(48.dp)
          .clickable(onClick = onQuickDelete)
          .padding(5.dp)
          .testTag("tile_delete_btn_${item.id}")
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Move ${item.title} to Trash",
            tint = Color.White,
            modifier = Modifier.size(13.dp)
          )
        }
      }
    }

    if (item.isFavorite) {
      Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Favorite",
        tint = Color(0xFFFF3B30),
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(5.dp)
          .size(13.dp)
      )
    }

    if (isSelectionMode) {
      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(5.dp)
          .size(20.dp)
          .clip(CircleShape)
          .background(if (isSelected) accentColor else Color.Black.copy(alpha = 0.55f))
          .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Selected",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun ProToolActionRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  textColor: Color,
  subtextColor: Color,
  onClick: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(NovaProGoldStart.copy(alpha = 0.16f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = NovaProGoldStart, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
      Text(subtitle, fontSize = 11.sp, color = subtextColor)
    }
  }
}

private fun formatSectionDateLabel(timestampMs: Long): String {
  if (DateUtils.isToday(timestampMs)) return "Today"
  if (DateUtils.isToday(timestampMs + DateUtils.DAY_IN_MILLIS)) return "Yesterday"

  val calNow = Calendar.getInstance()
  val calItem = Calendar.getInstance().apply { timeInMillis = timestampMs }
  return if (calNow.get(Calendar.YEAR) == calItem.get(Calendar.YEAR)) {
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestampMs))
  } else {
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestampMs))
  }
}
