package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.MediaThumbnailCard
import com.example.ui.theme.CyberCyan
import com.example.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchTagScreen(
  viewModel: GalleryViewModel,
  onMediaClick: (MediaItem) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedTag by viewModel.selectedTag.collectAsState()
  val allMedia by viewModel.allActiveMedia.collectAsState()
  val filteredResults by viewModel.filteredMedia.collectAsState()

  // Collect all unique tags across all media
  val allTags = remember(allMedia) {
    allMedia.flatMap { it.tags }.distinct().sorted()
  }

  Scaffold(
    containerColor = Color(0xFFFFFFFF),
    topBar = {
      TopAppBar(
        title = {
          Text("Search & Discovery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF18181B))
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF18181B))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFFFFFFFF)
        )
      )
    },
    modifier = modifier
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
    ) {
      // Search TextField
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Search by file name, tags, or date...", color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0891B2))
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setSearchQuery("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF71717A))
            }
          }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Color(0xFF0891B2),
          unfocusedBorderColor = Color(0xFFE4E4E7),
          focusedContainerColor = Color(0xFFF4F4F5),
          unfocusedContainerColor = Color(0xFFF4F4F5),
          focusedTextColor = Color(0xFF18181B),
          unfocusedTextColor = Color(0xFF18181B)
        ),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_text_field")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Tags Header & Cloud
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0891B2))
        Spacer(modifier = Modifier.size(6.dp))
        Text(
          text = "Tags & Topics",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF18181B)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // "All Tags" chip
        ElevatedFilterChip(
          selected = selectedTag == null,
          onClick = { viewModel.setSelectedTag(null) },
          label = { Text("All", fontSize = 12.sp) },
          colors = FilterChipDefaults.elevatedFilterChipColors(
            selectedContainerColor = Color(0xFF0891B2),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFFF4F4F5),
            labelColor = Color(0xFF18181B)
          )
        )

        allTags.forEach { tag ->
          val isSelected = selectedTag.equals(tag, ignoreCase = true)
          ElevatedFilterChip(
            selected = isSelected,
            onClick = {
              viewModel.setSelectedTag(if (isSelected) null else tag)
            },
            label = { Text(tag, fontSize = 12.sp) },
            colors = FilterChipDefaults.elevatedFilterChipColors(
              selectedContainerColor = Color(0xFF0891B2),
              selectedLabelColor = Color.White,
              containerColor = Color(0xFFF4F4F5),
              labelColor = Color(0xFF18181B)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Results (${filteredResults.size})",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      if (filteredResults.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No matching media found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Try searching for a different keyword or tag",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          contentPadding = PaddingValues(bottom = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(filteredResults, key = { it.id }) { item ->
            MediaThumbnailCard(
              item = item,
              isSelected = false,
              isSelectionMode = false,
              onClick = { onMediaClick(item) },
              onLongClick = {}
            )
          }
        }
      }
    }
  }
}
