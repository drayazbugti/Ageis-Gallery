package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.NovaLightBg
import com.example.ui.theme.NovaLightCardBg
import com.example.ui.theme.NovaLightCardBorder
import com.example.ui.theme.NovaLightSubtext
import com.example.ui.theme.NovaLightText
import com.example.viewmodel.GalleryViewModel
import com.example.viewmodel.SortOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosGalleryScreen(
  viewModel: GalleryViewModel,
  onVideoClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allMedia by viewModel.allActiveMedia.collectAsState()
  val isLightMode by viewModel.isLightMode.collectAsState()
  val sortOrder by viewModel.sortOrder.collectAsState()
  val sortAscending by viewModel.sortAscending.collectAsState()

  var isSearchActive by remember { mutableStateOf(false) }
  var videoQuery by remember { mutableStateOf("") }

  val videoList = remember(allMedia, sortOrder, sortAscending, videoQuery) {
    val baseVideos = allMedia.filter { it.type == MediaType.VIDEO }
    val q = videoQuery.trim()
    val filtered = if (q.isBlank()) {
      baseVideos
    } else {
      val df1 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
      val df2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      baseVideos.filter { item ->
        val dateStr = "${df1.format(Date(item.dateAdded))} ${df2.format(Date(item.dateAdded))}"
        item.title.contains(q, ignoreCase = true) ||
          item.tags.any { it.contains(q, ignoreCase = true) || it.removePrefix("#").contains(q.removePrefix("#"), ignoreCase = true) } ||
          dateStr.contains(q, ignoreCase = true)
      }
    }
    viewModel.sortMediaList(filtered, sortOrder, sortAscending)
  }

  val bgColor = if (isLightMode) NovaLightBg else NovaDarkBg
  val textColor = if (isLightMode) NovaLightText else NovaDarkText
  val subtextColor = if (isLightMode) NovaLightSubtext else NovaDarkSubtext
  val cardBg = if (isLightMode) NovaLightCardBg else NovaDarkCardBg
  val cardBorder = if (isLightMode) NovaLightCardBorder else NovaDarkCardBorder
  val accentColor = Color(0xFF0066FF)

  Scaffold(
    containerColor = bgColor,
    topBar = {
      Surface(
        color = bgColor,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
        ) {
          if (isSearchActive) {
            OutlinedTextField(
              value = videoQuery,
              onValueChange = { videoQuery = it },
              placeholder = { Text("Search videos by name, tag, or date...", fontSize = 13.sp, color = subtextColor) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
              trailingIcon = {
                IconButton(onClick = {
                  videoQuery = ""
                  isSearchActive = false
                }) {
                  Icon(Icons.Default.Close, contentDescription = "Close Search", tint = subtextColor)
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
              modifier = Modifier.fillMaxWidth().testTag("videos_search_input")
            )
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Videos",
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  color = textColor
                )
                Text(
                  text = "${videoList.size} Video clips in library",
                  fontSize = 12.sp,
                  color = subtextColor
                )
              }
              IconButton(
                onClick = { isSearchActive = true },
                modifier = Modifier.testTag("videos_search_btn")
              ) {
                Icon(Icons.Default.Search, contentDescription = "Search Videos", tint = textColor)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Sort Bar: Date Added, Name, Size
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .testTag("videos_sort_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = subtextColor, modifier = Modifier.size(16.dp))
            Text("Sort:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = subtextColor)
            SortOrder.values().forEach { order ->
              val isSelected = sortOrder == order
              ElevatedFilterChip(
                selected = isSelected,
                onClick = { viewModel.setSortOrder(order) },
                label = {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = order.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    if (isSelected) {
                      Icon(
                        imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = if (sortAscending) "Ascending" else "Descending",
                        modifier = Modifier.size(12.dp)
                      )
                    }
                  }
                },
                colors = FilterChipDefaults.elevatedFilterChipColors(
                  selectedContainerColor = accentColor,
                  selectedLabelColor = Color.White,
                  containerColor = cardBg,
                  labelColor = textColor
                ),
                modifier = Modifier.height(30.dp)
              )
            }
          }
        }
      }
    },
    modifier = modifier
  ) { paddingValues ->
    if (videoList.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .background(accentColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp))
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text("No videos found", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
          Text("Record or import videos into Nova Gallery", color = subtextColor, fontSize = 12.sp)
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .testTag("videos_grid")
      ) {
        items(videoList, key = { it.id }) { item ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(cardBg)
              .clickable { onVideoClick(item) }
              .testTag("video_card_${item.id}")
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
            ) {
              AsyncImage(
                model = ImageRequest.Builder(context)
                  .data(item.uri)
                  .crossfade(true)
                  .build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              // Play icon in center
              Box(
                modifier = Modifier
                  .align(Alignment.Center)
                  .size(40.dp)
                  .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
              }

              // Duration Badge
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(8.dp)
                  .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = item.formattedDuration.ifEmpty { "00:00" },
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = item.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${item.resolution} • ${item.formattedSize}",
                fontSize = 11.sp,
                color = subtextColor,
                maxLines = 1
              )
            }
          }
        }
      }
    }
  }
}
