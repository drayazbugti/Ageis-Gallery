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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.CyberCyan

data class FrameStyleOption(
  val name: String,
  val brush: Brush,
  val textColor: Color = Color.White
)

@Composable
fun ExportStudioDialog(
  mediaItem: MediaItem,
  onDismiss: () -> Unit,
  onExport: (frameStyle: String, aspectRatio: String, watermark: Boolean) -> Unit
) {
  val context = LocalContext.current

  val frameOptions = listOf(
    FrameStyleOption("Cyber Neon", Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF8B5CF6)))),
    FrameStyleOption("Sunset Mirage", Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFF97316)))),
    FrameStyleOption("Emerald Aura", Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF00E5FF)))),
    FrameStyleOption("Obsidian Pro", Brush.horizontalGradient(listOf(Color(0xFF27272A), Color(0xFF3F3F46)))),
    FrameStyleOption("Polaroid Studio", Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F5F9))), Color.Black)
  )

  var selectedFrame by remember { mutableStateOf(frameOptions.first()) }
  var selectedRatio by remember { mutableStateOf("1:1") }
  var isWatermarkEnabled by remember { mutableStateOf(true) }

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
        // Studio Header
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
                .background(
                  Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF22D3EE)))
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Colorful Designer Export Studio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Vibrant frames & stylish export gradients",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Framed Preview Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(selectedFrame.brush)
            .padding(8.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(184.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(if (mediaItem.thumbnailUri.isNotBlank()) mediaItem.thumbnailUri else mediaItem.uri)
                .crossfade(true)
                .build(),
              contentDescription = "Framed Preview",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth()
            )

            // Optional Watermark Stamp
            if (isWatermarkEnabled) {
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(8.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.Black.copy(alpha = 0.75f))
                  .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "⚡ AEGIS PRO • 4K HD",
                  color = CyberCyan,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Frame Selector Label
        Text(
          text = "Select Vibrant Frame",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          frameOptions.forEach { frame ->
            val isSelected = selectedFrame == frame
            Box(
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(frame.brush)
                .clickable { selectedFrame = frame }
                .then(
                  if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = frame.textColor,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Watermark Switch
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF18181B))
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Include Offline 4K Stamp",
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
          )
          Switch(
            checked = isWatermarkEnabled,
            onCheckedChange = { isWatermarkEnabled = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.Black,
              checkedTrackColor = CyberCyan,
              uncheckedTrackColor = Color(0xFF27272A)
            )
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Button(
          onClick = {
            onExport(selectedFrame.name, selectedRatio, isWatermarkEnabled)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
          ),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF22D3EE))),
              RoundedCornerShape(14.dp)
            )
        ) {
          Text(
            text = "Export High-Res to Gallery",
            color = Color.Black,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 12.dp)
          )
        }
      }
    }
  }
}
