package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FilterFrames
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.viewmodel.GalleryViewModel

enum class EditorTool(val label: String, val icon: ImageVector) {
  CROP("Crop", Icons.Default.Crop),
  ROTATE("Rotate", Icons.AutoMirrored.Filled.RotateRight),
  V_FLIP("V-flip", Icons.Default.SwapVert),
  H_FLIP("H-flip", Icons.Default.SwapHoriz),
  TRANSFORM("Transform", Icons.Default.Transform),
  FRAME("Frame", Icons.Default.FilterFrames),
  POSTER("Poster", Icons.Default.ViewCarousel),
  TEXT("Text", Icons.Default.FormatColorText),
  FIT("Fit", Icons.Default.FitScreen),
  ENHANCE("Enhance", Icons.Default.AutoAwesome)
}

@Composable
fun PhotoEditorScreen(
  item: MediaItem,
  viewModel: GalleryViewModel,
  onDismiss: () -> Unit,
  onSaved: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Editor States
  var activeTool by remember { mutableStateOf<EditorTool?>(null) }
  var rotationDegrees by remember { mutableFloatStateOf(item.rotationDegrees) }
  var isVFlip by remember { mutableStateOf(false) }
  var isHFlip by remember { mutableStateOf(false) }
  var cropAspectRatio by remember { mutableStateOf("Free") }
  var selectedFrame by remember { mutableStateOf("None") }
  var selectedPoster by remember { mutableStateOf("None") }
  var customText by remember { mutableStateOf("") }
  var textColor by remember { mutableStateOf(Color.White) }
  var textSizeSp by remember { mutableFloatStateOf(24f) }
  var fitMode by remember { mutableStateOf("Center Fit") }
  var enhanceFilter by remember { mutableStateOf(item.filterApplied.ifEmpty { "None" }) }
  var scaleFactor by remember { mutableFloatStateOf(1.0f) }

  // Color Filter calculation
  val activeColorFilter = when (enhanceFilter) {
    "Auto-Enhance" -> ColorFilter.colorMatrix(ColorMatrix().apply {
      setToSaturation(1.35f)
    })
    "Vivid Pop" -> ColorFilter.colorMatrix(ColorMatrix().apply {
      setToSaturation(1.7f)
    })
    "B&W Cutout" -> ColorFilter.colorMatrix(ColorMatrix().apply {
      setToSaturation(0f)
    })
    "Warm Glow" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
      1.3f, 0f, 0f, 0f, 0f,
      0f, 1.1f, 0f, 0f, 0f,
      0f, 0f, 0.85f, 0f, 0f,
      0f, 0f, 0f, 1f, 0f
    )))
    "Cool Breeze" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
      0.85f, 0f, 0f, 0f, 0f,
      0f, 0.95f, 0f, 0f, 0f,
      0f, 0f, 1.35f, 0f, 0f,
      0f, 0f, 0f, 1f, 0f
    )))
    "Dramatic" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
      1.2f, 0f, 0f, 0f, -20f,
      0f, 1.2f, 0f, 0f, -20f,
      0f, 0f, 1.2f, 0f, -20f,
      0f, 0f, 0f, 1f, 0f
    )))
    else -> null
  }

  Surface(
    color = Color(0xFF111111),
    modifier = modifier.fillMaxSize()
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // 1. Center Image Canvas with Live Transformations
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(
            top = 70.dp,
            bottom = if (activeTool != null) 220.dp else 120.dp,
            start = when (selectedFrame) {
              "Polaroid", "White Border" -> 20.dp
              "Neon Cyber" -> 16.dp
              else -> 8.dp
            },
            end = when (selectedFrame) {
              "Polaroid", "White Border" -> 20.dp
              "Neon Cyber" -> 16.dp
              else -> 8.dp
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        // Frame Container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(
              when (selectedFrame) {
                "Rounded Card" -> RoundedCornerShape(24.dp)
                "Polaroid" -> RoundedCornerShape(12.dp)
                else -> RoundedCornerShape(0.dp)
              }
            )
            .background(
              when (selectedFrame) {
                "Polaroid", "White Border" -> Color.White
                "Classic Dark" -> Color.Black
                "Gold Vintage" -> Color(0xFFD4AF37)
                "Neon Cyber" -> Color(0xFF0066FF)
                else -> Color.Transparent
              }
            )
            .padding(
              when (selectedFrame) {
                "White Border", "Classic Dark", "Gold Vintage" -> 12.dp
                "Polaroid" -> 16.dp
                "Neon Cyber" -> 6.dp
                else -> 0.dp
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(item.uri)
              .crossfade(true)
              .build(),
            contentDescription = item.title,
            contentScale = when (fitMode) {
              "Fill" -> ContentScale.Crop
              "Square" -> ContentScale.FillBounds
              else -> ContentScale.Fit
            },
            colorFilter = activeColorFilter,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer(
                scaleX = (if (isHFlip) -1f else 1f) * scaleFactor,
                scaleY = (if (isVFlip) -1f else 1f) * scaleFactor,
                rotationZ = rotationDegrees
              )
          )

          // Crop Grid Lines Overlay (Rule of Thirds)
          if (activeTool == EditorTool.CROP) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val lineColor = Color.White.copy(alpha = 0.7f)
              // Horizontal grid lines
              drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 2f)
              drawLine(lineColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeWidth = 2f)
              // Vertical grid lines
              drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 2f)
              drawLine(lineColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeWidth = 2f)
            }
          }

          // Poster Mode Overlays
          if (selectedPoster != "None") {
            when (selectedPoster) {
              "Cinematic" -> {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                  Box(modifier = Modifier.fillMaxWidth().height(36.dp).background(Color.Black))
                  Text(
                    text = "CINEMATIC VIBES",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                  )
                  Box(modifier = Modifier.fillMaxWidth().height(36.dp).background(Color.Black))
                }
              }
              "Magazine" -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                  Text(
                    text = "VOGUE NOVA",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 3.sp,
                    modifier = Modifier.align(Alignment.TopCenter)
                  )
                  Text(
                    text = "ISSUE 2026 • GALLERY EDIT",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.BottomStart)
                  )
                }
              }
            }
          }

          // Custom Text Overlay
          if (customText.isNotBlank()) {
            Box(
              modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = customText,
                color = textColor,
                fontSize = textSizeSp.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      // 2. Top Header (Cancel, Title, Save)
      Surface(
        color = Color(0xFF1E1E1E),
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .statusBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
          }

          Text(
            text = "Photo Editor Suite",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )

          Button(
            onClick = {
              viewModel.saveEditedPhoto(
                filter = enhanceFilter,
                rotation = rotationDegrees,
                brightness = 1.0f,
                contrast = 1.0f,
                newTags = listOf("#Edited")
              )
              Toast.makeText(context, "Photo edits saved successfully!", Toast.LENGTH_SHORT).show()
              onSaved()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save", fontWeight = FontWeight.Bold)
          }
        }
      }

      // 3. Bottom Panels: Sub-Tool Config + Main Toolbar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
      ) {
        // Active Sub-Tool Options Panel
        AnimatedVisibility(
          visible = activeTool != null,
          enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
          exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
          Surface(
            color = Color(0xFF1E1E1E),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              when (activeTool) {
                EditorTool.CROP -> {
                  Text("Crop Aspect Ratio (with grid lines)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    listOf("Free", "1:1", "4:3", "16:9", "9:16").forEach { ratio ->
                      ElevatedFilterChip(
                        selected = cropAspectRatio == ratio,
                        onClick = { cropAspectRatio = ratio },
                        label = { Text(ratio, fontSize = 11.sp) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                          selectedContainerColor = Color(0xFF0066FF),
                          selectedLabelColor = Color.White
                        )
                      )
                    }
                  }
                }
                EditorTool.ROTATE -> {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Rotate Angle: ${rotationDegrees.toInt()}°", color = Color.White, fontSize = 13.sp)
                    Button(
                      onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                      Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("+90°", fontSize = 12.sp)
                    }
                  }
                  Slider(
                    value = rotationDegrees,
                    onValueChange = { rotationDegrees = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF0066FF), activeTrackColor = Color(0xFF0066FF))
                  )
                }
                EditorTool.V_FLIP -> {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Vertical Flip (Upside Down)", color = Color.White, fontSize = 13.sp)
                    Button(
                      onClick = { isVFlip = !isVFlip },
                      colors = ButtonDefaults.buttonColors(containerColor = if (isVFlip) Color(0xFF0066FF) else Color(0xFF333333))
                    ) {
                      Text(if (isVFlip) "Flipped" else "Normal")
                    }
                  }
                }
                EditorTool.H_FLIP -> {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Horizontal Flip (Mirror Image)", color = Color.White, fontSize = 13.sp)
                    Button(
                      onClick = { isHFlip = !isHFlip },
                      colors = ButtonDefaults.buttonColors(containerColor = if (isHFlip) Color(0xFF0066FF) else Color(0xFF333333))
                    ) {
                      Text(if (isHFlip) "Mirrored" else "Normal")
                    }
                  }
                }
                EditorTool.TRANSFORM -> {
                  Text("Zoom & Scale Factor: ${(scaleFactor * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
                  Slider(
                    value = scaleFactor,
                    onValueChange = { scaleFactor = it },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF0066FF), activeTrackColor = Color(0xFF0066FF))
                  )
                }
                EditorTool.FRAME -> {
                  Text("Photo Frame Style", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    listOf("None", "Polaroid", "White Border", "Classic Dark", "Neon Cyber", "Gold Vintage", "Rounded Card").forEach { f ->
                      ElevatedFilterChip(
                        selected = selectedFrame == f,
                        onClick = { selectedFrame = f },
                        label = { Text(f, fontSize = 11.sp) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                          selectedContainerColor = Color(0xFF0066FF),
                          selectedLabelColor = Color.White
                        )
                      )
                    }
                  }
                }
                EditorTool.POSTER -> {
                  Text("Poster Template Overlays", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("None", "Cinematic", "Magazine").forEach { p ->
                      ElevatedFilterChip(
                        selected = selectedPoster == p,
                        onClick = { selectedPoster = p },
                        label = { Text(p, fontSize = 11.sp) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                          selectedContainerColor = Color(0xFF0066FF),
                          selectedLabelColor = Color.White
                        )
                      )
                    }
                  }
                }
                EditorTool.TEXT -> {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                      value = customText,
                      onValueChange = { customText = it },
                      placeholder = { Text("Enter caption text...", color = Color.Gray, fontSize = 12.sp) },
                      singleLine = true,
                      modifier = Modifier.weight(1f),
                      colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf(Color.White, Color(0xFF0066FF), Color(0xFFFFD700), Color(0xFFFF3B30)).forEach { c ->
                      Box(
                        modifier = Modifier
                          .size(28.dp)
                          .clip(CircleShape)
                          .background(c)
                          .clickable { textColor = c }
                          .border(if (textColor == c) 2.dp else 0.dp, Color.White, CircleShape)
                      )
                    }
                  }
                }
                EditorTool.FIT -> {
                  Text("Fit & Layout", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Center Fit", "Fill", "Square").forEach { m ->
                      ElevatedFilterChip(
                        selected = fitMode == m,
                        onClick = { fitMode = m },
                        label = { Text(m, fontSize = 11.sp) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                          selectedContainerColor = Color(0xFF0066FF),
                          selectedLabelColor = Color.White
                        )
                      )
                    }
                  }
                }
                EditorTool.ENHANCE -> {
                  Text("AI Cutout & Auto-Enhance", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    listOf("None", "Auto-Enhance", "Vivid Pop", "Warm Glow", "Cool Breeze", "B&W Cutout", "Dramatic").forEach { eff ->
                      ElevatedFilterChip(
                        selected = enhanceFilter == eff,
                        onClick = { enhanceFilter = eff },
                        label = { Text(eff, fontSize = 11.sp) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                          selectedContainerColor = Color(0xFF0066FF),
                          selectedLabelColor = Color.White
                        )
                      )
                    }
                  }
                }
                null -> {}
              }
            }
          }
        }

        // Main Editing Toolbar
        Surface(
          color = Color(0xFF141414),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            EditorTool.values().forEach { tool ->
              val isSelected = activeTool == tool
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    activeTool = if (activeTool == tool) null else tool
                  }
                  .padding(horizontal = 6.dp, vertical = 4.dp)
                  .testTag("tool_${tool.name.lowercase()}")
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .background(if (isSelected) Color(0xFF0066FF) else Color(0xFF262626), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(tool.icon, contentDescription = tool.label, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = tool.label,
                  color = if (isSelected) Color(0xFF0066FF) else Color(0xFFAAAAAA),
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }
  }
}
