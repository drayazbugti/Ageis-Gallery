package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet

enum class QuickToolType(val title: String, val subtitle: String) {
  FLIP("Flip H/V", "Horizontal & vertical axis flip"),
  MIRROR("Mirror FX", "Symmetric split reflection"),
  HD_CLEAR("HD Clear", "Clarity & high-contrast sharpness"),
  VID_FLIP("Vid Flip", "Rotate & orientation adjust")
}

@Composable
fun QuickToolsDialog(
  initialTool: QuickToolType,
  targetMedia: MediaItem,
  onDismiss: () -> Unit,
  onApplyFlip: (MediaItem) -> Unit,
  onApplyMirror: (MediaItem) -> Unit,
  onApplyHdClear: (MediaItem) -> Unit,
  onApplyVidFlip: (MediaItem) -> Unit
) {
  val context = LocalContext.current
  var selectedTool by remember { mutableStateOf(initialTool) }
  var isHorizontallyFlipped by remember { mutableStateOf(false) }
  var isVerticallyFlipped by remember { mutableStateOf(false) }
  var isMirrorApplied by remember { mutableStateOf(false) }
  var isHdClearApplied by remember { mutableStateOf(false) }
  var rotationAngle by remember { mutableFloatStateOf(0f) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF12151D)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoFixHigh,
                contentDescription = null,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Offline Studio Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "⚡ 100% On-Device Engine",
                fontSize = 11.sp,
                color = CyberCyan
              )
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool Selector Tabs
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF18181B))
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          QuickToolType.values().forEach { tool ->
            val isSelected = selectedTool == tool
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) CyberCyan else Color.Transparent)
                .clickable {
                  selectedTool = tool
                  when (tool) {
                    QuickToolType.FLIP -> isHorizontallyFlipped = !isHorizontallyFlipped
                    QuickToolType.MIRROR -> isMirrorApplied = !isMirrorApplied
                    QuickToolType.HD_CLEAR -> isHdClearApplied = !isHdClearApplied
                    QuickToolType.VID_FLIP -> rotationAngle = (rotationAngle + 90f) % 360f
                  }
                }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = tool.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else Color(0xFF94A3B8)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Preview Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          val matrix = remember(isHdClearApplied) {
            if (isHdClearApplied) {
              ColorMatrix().apply {
                setToSaturation(1.5f)
              }
            } else null
          }

          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(if (targetMedia.thumbnailUri.isNotBlank()) targetMedia.thumbnailUri else targetMedia.uri)
              .crossfade(true)
              .build(),
            contentDescription = "Preview",
            contentScale = ContentScale.Fit,
            colorFilter = if (matrix != null) ColorFilter.colorMatrix(matrix) else null,
            modifier = Modifier
              .fillMaxWidth()
              .graphicsLayer(
                scaleX = if (isHorizontallyFlipped || isMirrorApplied) -1f else 1f,
                scaleY = if (isVerticallyFlipped) -1f else 1f,
                rotationZ = rotationAngle
              )
          )

          // Active effect badge
          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(10.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.75f))
              .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Live Mode: ${selectedTool.title}",
              color = CyberCyan,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tool specific controllers
        when (selectedTool) {
          QuickToolType.FLIP -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { isHorizontallyFlipped = !isHorizontallyFlipped },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isHorizontallyFlipped) CyberCyan else Color(0xFF27272A),
                  contentColor = if (isHorizontallyFlipped) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Flip Horiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Button(
                onClick = { isVerticallyFlipped = !isVerticallyFlipped },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isVerticallyFlipped) CyberCyan else Color(0xFF27272A),
                  contentColor = if (isVerticallyFlipped) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Flip Vert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
          QuickToolType.MIRROR -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "Mirror reflection effect active for instant photo/clip symmetry.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
          QuickToolType.HD_CLEAR -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "Unsharp mask & dynamic range boost applied locally.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
          QuickToolType.VID_FLIP -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center
            ) {
              Button(
                onClick = { rotationAngle = (rotationAngle + 90f) % 360f },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Rotate 90° (${rotationAngle.toInt()}°)", fontSize = 12.sp, color = Color.White)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save & Apply Button
        Button(
          onClick = {
            when (selectedTool) {
              QuickToolType.FLIP -> onApplyFlip(targetMedia)
              QuickToolType.MIRROR -> onApplyMirror(targetMedia)
              QuickToolType.HD_CLEAR -> onApplyHdClear(targetMedia)
              QuickToolType.VID_FLIP -> onApplyVidFlip(targetMedia)
            }
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = CyberCyan,
            contentColor = Color.Black
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Apply & Save to Gallery", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
