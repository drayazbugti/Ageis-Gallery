package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaThumbnailCard(
  item: MediaItem,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onShare: ((MediaItem) -> Unit)? = null,
  onRename: ((MediaItem, String) -> Unit)? = null,
  onDelete: ((MediaItem) -> Unit)? = null,
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 16.dp
) {
  val context = LocalContext.current
  var isCardMenuOpen by remember { mutableStateOf(false) }
  var isRenameDialogOpen by remember { mutableStateOf(false) }
  var renameInput by remember { mutableStateOf(item.title) }

  val colorFilter = when (item.filterApplied) {
    "MONO" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    "SEPIA" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
      0.393f, 0.769f, 0.189f, 0f, 0f,
      0.349f, 0.686f, 0.168f, 0f, 0f,
      0.272f, 0.534f, 0.131f, 0f, 0f,
      0f, 0f, 0f, 1f, 0f
    )))
    "VIVID" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.6f) })
    "COOL" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
      0.8f, 0f, 0f, 0f, 0f,
      0.9f, 1f, 0f, 0f, 0f,
      0f, 0f, 1.3f, 0f, 0f,
      0f, 0f, 0f, 1f, 0f
    )))
    else -> null
  }

  Column(
    modifier = modifier
      .height(145.dp) // height: 145px from CSS .media-card
      .clip(RoundedCornerShape(cornerRadius))
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(cornerRadius))
      .testTag("media_card_${item.id}")
      .then(
        if (isSelected) {
          Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(cornerRadius))
        } else {
          Modifier
        }
      )
  ) {
    // Top Media Thumbnail (flex: 1)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .combinedClickable(
          onClick = onClick,
          onLongClick = onLongClick
        )
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

      // Video Badge (bottom-left) ▶ 0:56
      if (item.type == MediaType.VIDEO) {
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "▶ ${if (item.formattedDuration.isNotEmpty()) item.formattedDuration else "0:56"}",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Vaulted Icon Indicator
      if (item.isVaulted) {
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(3.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Vaulted",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(11.dp)
          )
        }
      }

      // Favorite Heart Indicator
      if (item.isFavorite) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(18.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorite",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(11.dp)
          )
        }
      }

      // Selection Checkmark
      if (isSelectionMode) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(20.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f))
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

    // Card Footer (.media-footer: 36px height, background #f8f9fa / #1e1e1e)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(36.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline))
        .padding(horizontal = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // File Name (.media-name: 11px, max-width: 95px, ellipsis)
      Text(
        text = item.title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f)
      )

      // 3-Dots Button on Card Footer (.card-menu-btn)
      Box {
        Text(
          text = "⋮",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier
            .clickable { isCardMenuOpen = !isCardMenuOpen }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("card_menu_btn_${item.id}")
        )

        // Card Popup Menu (Send, Edit/Rename, Delete (Trash))
        DropdownMenu(
          expanded = isCardMenuOpen,
          onDismissRequest = { isCardMenuOpen = false },
          modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        ) {
          // 📤 Send
          DropdownMenuItem(
            text = { Text("📤 Send", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            onClick = {
              isCardMenuOpen = false
              if (onShare != null) {
                onShare(item)
              } else {
                try {
                  val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Sending: ${item.title}")
                    type = if (item.type == MediaType.VIDEO) "video/*" else "image/*"
                  }
                  context.startActivity(Intent.createChooser(sendIntent, "Send ${item.title}"))
                } catch (_: Exception) {}
              }
            }
          )

          // ✏️ Edit/Rename
          DropdownMenuItem(
            text = { Text("✏️ Edit/Rename", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            onClick = {
              isCardMenuOpen = false
              renameInput = item.title
              isRenameDialogOpen = true
            }
          )

          // 🗑️ Delete (Permanently)
          DropdownMenuItem(
            text = { Text("🗑️ Delete (Permanently)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC3545)) },
            onClick = {
              isCardMenuOpen = false
              onDelete?.invoke(item)
            }
          )
        }
      }
    }
  }

  // Rename Dialog
  if (isRenameDialogOpen) {
    AlertDialog(
      onDismissRequest = { isRenameDialogOpen = false },
      containerColor = MaterialTheme.colorScheme.surface,
      title = { Text("Rename File", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Enter new file name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          OutlinedTextField(
            value = renameInput,
            onValueChange = { renameInput = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (renameInput.isNotBlank()) {
              onRename?.invoke(item, renameInput)
              isRenameDialogOpen = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { isRenameDialogOpen = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
