package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.CyberCyan
import com.example.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
  viewModel: GalleryViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val trashList by viewModel.trashMedia.collectAsState()
  var showEmptyConfirmDialog by remember { mutableStateOf(false) }
  var selectedItemForAction by remember { mutableStateOf<MediaItem?>(null) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Text("Recycle Bin (30 Days)", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (trashList.isNotEmpty()) {
            IconButton(
              onClick = { viewModel.restoreAllTrash() },
              modifier = Modifier.testTag("restore_all_trash_button")
            ) {
              Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore All", tint = CyberCyan)
            }
            IconButton(
              onClick = { showEmptyConfirmDialog = true },
              modifier = Modifier.testTag("empty_trash_button")
            ) {
              Icon(Icons.Default.DeleteForever, contentDescription = "Empty Trash", tint = MaterialTheme.colorScheme.error)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    modifier = modifier
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 30-Day Info Banner
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Items here will be permanently deleted after 30 days. You can restore them anytime.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (trashList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Recycle Bin is Empty",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Deleted photos and videos will appear here for 30 days before permanent removal.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          contentPadding = PaddingValues(12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(trashList, key = { it.id }) { item ->
            TrashThumbnailCard(
              item = item,
              onClick = { selectedItemForAction = item }
            )
          }
        }
      }
    }

    // Empty Trash Confirmation Dialog
    if (showEmptyConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showEmptyConfirmDialog = false },
        title = { Text("Empty Recycle Bin?") },
        text = { Text("This will permanently delete all ${trashList.size} items immediately. This action cannot be undone.") },
        confirmButton = {
          Button(
            onClick = {
              viewModel.emptyTrash()
              showEmptyConfirmDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
          ) {
            Text("Delete All Forever")
          }
        },
        dismissButton = {
          TextButton(onClick = { showEmptyConfirmDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Single item restore/delete dialog
    selectedItemForAction?.let { item ->
      AlertDialog(
        onDismissRequest = { selectedItemForAction = null },
        title = { Text(item.title) },
        text = {
          Text("Days remaining: ${item.remainingTrashDays} days\nWhat would you like to do?")
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.restoreFromTrash(item.id)
              selectedItemForAction = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
          ) {
            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Restore")
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = {
              viewModel.deletePermanently(listOf(item.id))
              selectedItemForAction = null
            }
          ) {
            Text("Delete Forever", color = MaterialTheme.colorScheme.error)
          }
        }
      )
    }
  }
}

@Composable
fun TrashThumbnailCard(
  item: MediaItem,
  onClick: () -> Unit
) {
  val context = LocalContext.current

  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
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

    // Remaining Days Badge
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(Color.Black.copy(alpha = 0.7f))
        .padding(vertical = 3.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "${item.remainingTrashDays}d left",
        color = if (item.remainingTrashDays < 5) Color(0xFFFF5252) else Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
