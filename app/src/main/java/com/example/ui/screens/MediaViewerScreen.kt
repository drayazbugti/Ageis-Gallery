package com.example.ui.screens

import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HdrStrong
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.CyberCyan
import com.example.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class QuickEditorTool(val label: String, val icon: ImageVector) {
  CROP("Crop", Icons.Default.Crop),
  MOSAIC("Mosaic", Icons.Default.GridOn),
  DOODLE("Doodle", Icons.Default.Draw),
  ADJUST("Adjust", Icons.Default.Tune),
  BLUR("Blur", Icons.Default.BlurOn),
  HDR("HDR", Icons.Default.HdrStrong),
  EFFECT("Effect", Icons.Default.AutoFixHigh)
}

private data class DoodleStroke(
  val points: List<Offset>,
  val color: Color
)

@Composable
fun MediaViewerScreen(
  item: MediaItem,
  viewModel: GalleryViewModel,
  onBack: () -> Unit,
  onOpenVault: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isControlsVisible by remember { mutableStateOf(true) }
  var isInfoDialogOpen by remember { mutableStateOf(false) }
  var isMoveDialogOpen by remember { mutableStateOf(false) }
  var isRenameDialogOpen by remember { mutableStateOf(false) }
  var isMoreMenuOpen by remember { mutableStateOf(false) }
  var isFullEditorOpen by remember { mutableStateOf(false) }
  var renameInput by remember(item) { mutableStateOf(item.title) }

  // Floating Quick Editor Panel States (.editor-panel)
  var isEditorPanelOpen by remember { mutableStateOf(false) }
  var activeQuickTool by remember { mutableStateOf<QuickEditorTool?>(null) }
  var quickFilter by remember(item.id) { mutableStateOf(item.filterApplied.ifEmpty { "NORMAL" }) }
  var quickBrightness by remember(item.id) { mutableFloatStateOf(0f) }
  var quickContrast by remember(item.id) { mutableFloatStateOf(1f) }
  var quickBlurDp by remember(item.id) { mutableFloatStateOf(0f) }
  var isHdrEnabled by remember(item.id) { mutableStateOf(false) }
  var isMosaicEnabled by remember(item.id) { mutableStateOf(false) }
  var cropRatio by remember(item.id) { mutableStateOf("Free") }

  // Doodle drawing states
  val doodleStrokes = remember(item.id) { mutableStateListOf<DoodleStroke>() }
  var currentStrokePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
  var doodleColor by remember { mutableStateOf(Color(0xFF00FF96)) }

  // Zoom and Pan states
  var scale by remember(item.id) { mutableFloatStateOf(1f) }
  var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
  val animatedScale by animateFloatAsState(targetValue = scale, label = "zoomScale")

  // Drag Gesture States for Swipe-Down Dismiss & Edge Navigation
  var dismissOffsetY by remember { mutableFloatStateOf(0f) }
  val animatedOffsetY by animateFloatAsState(targetValue = dismissOffsetY, label = "dismissOffset")
  val bgAlpha = ((1f - (dismissOffsetY / 450f)).coerceIn(0.2f, 1f))
  var gestureFeedback by remember { mutableStateOf<String?>(null) }

  val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
    val nextScale = (scale * zoomChange).coerceIn(1f, 4f)
    scale = nextScale
    if (nextScale > 1.01f) {
      offset += offsetChange
    } else {
      offset = Offset.Zero
    }
  }

  // Reset transient viewer states on item change
  LaunchedEffect(item.id) {
    scale = 1f
    offset = Offset.Zero
    dismissOffsetY = 0f
    isEditorPanelOpen = false
    activeQuickTool = null
  }

  LaunchedEffect(gestureFeedback) {
    if (gestureFeedback != null) {
      delay(900)
      gestureFeedback = null
    }
  }

  BackHandler {
    when {
      isFullEditorOpen -> isFullEditorOpen = false
      isEditorPanelOpen -> {
        isEditorPanelOpen = false
        activeQuickTool = null
      }
      else -> onBack()
    }
  }

  // Compose dynamic ColorMatrix combining Filter + HDR + Brightness/Contrast
  val activeColorFilter = remember(quickFilter, quickBrightness, quickContrast, isHdrEnabled) {
    val c = if (isHdrEnabled) quickContrast * 1.25f else quickContrast
    val b = (if (isHdrEnabled) quickBrightness + 10f else quickBrightness)
    val t = ((1f - c) / 2f) * 255f + b

    val matrix = ColorMatrix(
      floatArrayOf(
        c, 0f, 0f, 0f, t,
        0f, c, 0f, 0f, t,
        0f, 0f, c, 0f, t,
        0f, 0f, 0f, 1f, 0f
      )
    )

    val filterMatrix = when (quickFilter) {
      "MONO", "B&W Cutout" -> ColorMatrix().apply { setToSaturation(0f) }
      "SEPIA" -> ColorMatrix(
        floatArrayOf(
          0.393f, 0.769f, 0.189f, 0f, 0f,
          0.349f, 0.686f, 0.168f, 0f, 0f,
          0.272f, 0.534f, 0.131f, 0f, 0f,
          0f, 0f, 0f, 1f, 0f
        )
      )
      "VIVID", "Vivid Pop", "Auto-Enhance" -> ColorMatrix().apply { setToSaturation(1.65f) }
      "COOL", "Cool Breeze" -> ColorMatrix(
        floatArrayOf(
          0.85f, 0f, 0f, 0f, 0f,
          0f, 0.95f, 0f, 0f, 0f,
          0f, 0f, 1.35f, 0f, 0f,
          0f, 0f, 0f, 1f, 0f
        )
      )
      "WARM", "Warm Glow" -> ColorMatrix(
        floatArrayOf(
          1.3f, 0f, 0f, 0f, 0f,
          0f, 1.1f, 0f, 0f, 0f,
          0f, 0f, 0.85f, 0f, 0f,
          0f, 0f, 0f, 1f, 0f
        )
      )
      else -> if (isHdrEnabled) ColorMatrix().apply { setToSaturation(1.35f) } else null
    }

    if (filterMatrix != null) {
      matrix.timesAssign(filterMatrix)
      ColorFilter.colorMatrix(matrix)
    } else if (c != 1f || b != 0f) {
      ColorFilter.colorMatrix(matrix)
    } else {
      null
    }
  }

  val dateFormatted = remember(item.dateAdded) {
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.dateAdded))
  }
  val neonEmerald = Color(0xFF00FF96)

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF111111).copy(alpha = bgAlpha))
  ) {
    val screenWidth = constraints.maxWidth.toFloat()

    // 1. Photo Canvas + Interactive Gestures / Doodle Overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(
          if (activeQuickTool == QuickEditorTool.DOODLE && isEditorPanelOpen) {
            Modifier.pointerInput(doodleColor) {
              detectDragGestures(
                onDragStart = { start ->
                  currentStrokePoints = listOf(start)
                },
                onDrag = { change, _ ->
                  change.consume()
                  currentStrokePoints = currentStrokePoints + change.position
                },
                onDragEnd = {
                  if (currentStrokePoints.isNotEmpty()) {
                    doodleStrokes.add(DoodleStroke(currentStrokePoints, doodleColor))
                    currentStrokePoints = emptyList()
                  }
                },
                onDragCancel = {
                  currentStrokePoints = emptyList()
                }
              )
            }
          } else {
            Modifier
              .pointerInput(Unit) {
                detectTapGestures(
                  onDoubleTap = {
                    if (scale > 1.05f) {
                      scale = 1f
                      offset = Offset.Zero
                      gestureFeedback = "Zoom Reset"
                    } else {
                      scale = 2.5f
                      gestureFeedback = "Zoomed In (2.5x)"
                    }
                  },
                  onTap = {
                    if (!isFullEditorOpen) {
                      isControlsVisible = !isControlsVisible
                    }
                  }
                )
              }
              .pointerInput(scale) {
                var touchStartX = 0f
                var totalDragX = 0f
                var totalDragY = 0f
                var isSwipingDismiss = false
                var isSwipingHorizontal = false

                detectDragGestures(
                  onDragStart = { startOffset ->
                    touchStartX = startOffset.x
                    totalDragX = 0f
                    totalDragY = 0f
                    isSwipingDismiss = false
                    isSwipingHorizontal = false
                  },
                  onDrag = { change, dragAmount ->
                    if (scale > 1.05f) {
                      // Drag/Pan around zoomed photo smoothly
                      change.consume()
                      val maxPanX = (size.width * (scale - 1f)) / 2f
                      val maxPanY = (size.height * (scale - 1f)) / 2f
                      offset = Offset(
                        x = (offset.x + dragAmount.x).coerceIn(-maxPanX, maxPanX),
                        y = (offset.y + dragAmount.y).coerceIn(-maxPanY, maxPanY)
                      )
                    } else {
                      totalDragX += dragAmount.x
                      totalDragY += dragAmount.y

                      if (!isSwipingDismiss && !isSwipingHorizontal) {
                        if (kotlin.math.abs(totalDragY) > kotlin.math.abs(totalDragX) && totalDragY > 18f) {
                          isSwipingDismiss = true
                          gestureFeedback = "Closing Photo..."
                        } else if (kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY) && kotlin.math.abs(totalDragX) > 18f) {
                          if (touchStartX < screenWidth * 0.32f || touchStartX > screenWidth * 0.68f) {
                            isSwipingHorizontal = true
                          }
                        }
                      }

                      if (isSwipingDismiss) {
                        change.consume()
                        dismissOffsetY = (dismissOffsetY + dragAmount.y).coerceAtLeast(0f)
                      } else if (isSwipingHorizontal) {
                        change.consume()
                      }
                    }
                  },
                  onDragEnd = {
                    if (isSwipingDismiss) {
                      if (dismissOffsetY > 120f) {
                        onBack()
                      } else {
                        dismissOffsetY = 0f
                        gestureFeedback = null
                      }
                    } else if (isSwipingHorizontal) {
                      if (touchStartX < screenWidth * 0.32f && totalDragX > 45f) {
                        viewModel.navigateToPreviousMedia()
                      } else if (touchStartX > screenWidth * 0.68f && totalDragX < -45f) {
                        viewModel.navigateToNextMedia()
                      }
                    }
                    isSwipingDismiss = false
                    isSwipingHorizontal = false
                  },
                  onDragCancel = {
                    dismissOffsetY = 0f
                    gestureFeedback = null
                    isSwipingDismiss = false
                    isSwipingHorizontal = false
                  }
                )
              }
              .transformable(state = transformState)
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
        contentScale = ContentScale.Fit,
        colorFilter = activeColorFilter,
        modifier = Modifier
          .fillMaxSize()
          .then(if (quickBlurDp > 0f) Modifier.blur(quickBlurDp.dp) else Modifier)
          .graphicsLayer(
            scaleX = animatedScale,
            scaleY = animatedScale,
            translationX = offset.x,
            translationY = offset.y + animatedOffsetY,
            rotationZ = item.rotationDegrees
          )
      )

      // Mosaic grid simulation overlay when Mosaic tool is enabled
      if (isMosaicEnabled) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val cellSize = 48f
          val cols = (size.width / cellSize).toInt()
          val rows = (size.height / cellSize).toInt()
          for (r in 0..rows) {
            for (c in 0..cols) {
              if ((r + c) % 2 == 0) {
                drawRect(
                  color = Color.White.copy(alpha = 0.08f),
                  topLeft = Offset(c * cellSize, r * cellSize),
                  size = Size(cellSize, cellSize)
                )
              }
            }
          }
        }
      }

      // Doodle strokes canvas
      if (doodleStrokes.isNotEmpty() || currentStrokePoints.isNotEmpty()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          doodleStrokes.forEach { stroke ->
            if (stroke.points.size > 1) {
              val path = Path().apply {
                moveTo(stroke.points.first().x, stroke.points.first().y)
                stroke.points.drop(1).forEach { lineTo(it.x, it.y) }
              }
              drawPath(
                path = path,
                color = stroke.color,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
              )
            }
          }
          if (currentStrokePoints.size > 1) {
            val path = Path().apply {
              moveTo(currentStrokePoints.first().x, currentStrokePoints.first().y)
              currentStrokePoints.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
              path = path,
              color = doodleColor,
              style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
          }
        }
      }

      // Crop grid overlay when Crop tool is selected
      if (isEditorPanelOpen && activeQuickTool == QuickEditorTool.CROP) {
        Canvas(modifier = Modifier.fillMaxSize().padding(32.dp)) {
          val w = size.width
          val h = size.height
          val gridColor = Color.White.copy(alpha = 0.75f)
          drawRect(color = neonEmerald, style = Stroke(width = 3f))
          drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.5f)
          drawLine(gridColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeWidth = 1.5f)
          drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.5f)
          drawLine(gridColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeWidth = 1.5f)
        }
      }
    }

    // Gesture Popup Pill (.gesture-popup)
    if (gestureFeedback != null) {
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .clip(RoundedCornerShape(6.dp))
          .background(Color.Black.copy(alpha = 0.7f))
          .padding(horizontal = 20.dp, vertical = 10.dp)
      ) {
        Text(text = gestureFeedback!!, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
      }
    }

    // 2. Top Header (.top-bar): Back arrow, Date & Time / Photo Viewer, Favorite & Info
    AnimatedVisibility(
      visible = isControlsVisible && !isFullEditorOpen,
      enter = slideInVertically(),
      exit = slideOutVertically(),
      modifier = Modifier.align(Alignment.TopCenter)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
            )
          )
          .statusBarsPadding()
          .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("viewer_back_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp)
        ) {
          Text(
            text = dateFormatted,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${item.album} • ${item.resolution}",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { viewModel.toggleFavorite(item) },
            modifier = Modifier.testTag("viewer_favorite_btn")
          ) {
            Icon(
              imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Favorite",
              tint = if (item.isFavorite) Color(0xFFFF3B30) else Color.White
            )
          }

          IconButton(
            onClick = { isInfoDialogOpen = true },
            modifier = Modifier.testTag("viewer_info_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Details",
              tint = Color.White
            )
          }
        }
      }
    }

    // 3. Bottom Action Bar (.bottom-actions): Hide, Cut, Delete, Share, More
    AnimatedVisibility(
      visible = isControlsVisible && !isEditorPanelOpen && !isFullEditorOpen,
      enter = slideInVertically(initialOffsetY = { it }),
      exit = slideOutVertically(targetOffsetY = { it }),
      modifier = Modifier.align(Alignment.BottomCenter)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF0F0F0F).copy(alpha = 0.92f))
          .navigationBarsPadding()
          .padding(horizontal = 10.dp, vertical = 12.dp)
          .testTag("viewer_bottom_action_bar"),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 1. Hide (Move to Safe Vault)
        ViewerActionButton(
          icon = Icons.Default.Lock,
          label = "Hide",
          onClick = {
            viewModel.moveToVault(item.id)
            Toast.makeText(context, "Moved to Safe Vault", Toast.LENGTH_SHORT).show()
            onOpenVault()
          }
        )

        // 2. Cut (Opens Quick Editor Panel)
        ViewerActionButton(
          icon = Icons.Default.ContentCut,
          label = "Cut",
          tint = neonEmerald,
          onClick = {
            isEditorPanelOpen = true
            activeQuickTool = QuickEditorTool.CROP
          }
        )

        // 3. Delete
        ViewerActionButton(
          icon = Icons.Default.Delete,
          label = "Delete",
          tint = Color(0xFFFF453A),
          onClick = {
            viewModel.deleteMediaPermanently(item)
            Toast.makeText(context, "Deleted permanently", Toast.LENGTH_SHORT).show()
            onBack()
          }
        )

        // 4. Share
        ViewerActionButton(
          icon = Icons.Default.Share,
          label = "Share",
          onClick = {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
              type = "image/*"
              putExtra(Intent.EXTRA_TEXT, "Shared from Nova Gallery: ${item.title}\n${item.uri}")
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Photo"))
          }
        )

        // 5. More (Popup Options Menu)
        Box {
          ViewerActionButton(
            icon = Icons.Default.MoreVert,
            label = "More",
            onClick = { isMoreMenuOpen = !isMoreMenuOpen }
          )

          DropdownMenu(
            expanded = isMoreMenuOpen,
            onDismissRequest = { isMoreMenuOpen = false },
            modifier = Modifier.background(Color(0xFF1F1F28))
          ) {
            DropdownMenuItem(
              text = { Text("Quick Editor Tools", color = neonEmerald) },
              leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = neonEmerald) },
              onClick = {
                isMoreMenuOpen = false
                isEditorPanelOpen = true
              }
            )

            DropdownMenuItem(
              text = { Text("Full Photo Editor Suite", color = Color.White) },
              leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = CyberCyan) },
              onClick = {
                isMoreMenuOpen = false
                isFullEditorOpen = true
              }
            )

            DropdownMenuItem(
              text = { Text("Move to Album", color = Color.White) },
              leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = Color.White) },
              onClick = {
                isMoreMenuOpen = false
                isMoveDialogOpen = true
              }
            )

            DropdownMenuItem(
              text = { Text("Add / Manage Tags", color = Color.White) },
              leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = neonEmerald) },
              onClick = {
                isMoreMenuOpen = false
                isInfoDialogOpen = true
              }
            )

            DropdownMenuItem(
              text = { Text("Details & EXIF", color = Color.White) },
              leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.White) },
              onClick = {
                isMoreMenuOpen = false
                isInfoDialogOpen = true
              }
            )

            DropdownMenuItem(
              text = { Text("Set as Wallpaper", color = Color.White) },
              leadingIcon = { Icon(Icons.Default.Photo, contentDescription = null, tint = Color.White) },
              onClick = {
                isMoreMenuOpen = false
                try {
                  WallpaperManager.getInstance(context)
                  val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    setDataAndType(android.net.Uri.parse(item.uri), "image/*")
                    putExtra("mimeType", "image/*")
                  }
                  context.startActivity(Intent.createChooser(intent, "Set as Wallpaper"))
                } catch (_: Exception) {
                  Toast.makeText(context, "Wallpaper picker launched", Toast.LENGTH_SHORT).show()
                }
              }
            )

            DropdownMenuItem(
              text = { Text("Rename", color = Color.White) },
              leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
              onClick = {
                isMoreMenuOpen = false
                renameInput = item.title
                isRenameDialogOpen = true
              }
            )

            DropdownMenuItem(
              text = { Text("Copy File Path", color = Color.White) },
              leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White) },
              onClick = {
                isMoreMenuOpen = false
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Media Path", item.uri))
                Toast.makeText(context, "Path copied to clipboard", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      }
    }

    // 4. Floating Editor Tools Panel (.editor-panel): Crop, Mosaic, Doodle, Adjust, Blur, HDR, Effect
    AnimatedVisibility(
      visible = isEditorPanelOpen && !isFullEditorOpen,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 16.dp, start = 14.dp, end = 14.dp)
    ) {
      Surface(
        color = Color(0xFF191923).copy(alpha = 0.95f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, neonEmerald.copy(alpha = 0.45f)),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 440.dp)
          .testTag("photo_editor_floating_panel")
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Panel Header: Active tool title + Full Suite / Save / Close
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = activeQuickTool?.label ?: "Photo Editor",
              color = neonEmerald,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )

            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(
                onClick = {
                  isEditorPanelOpen = false
                  isFullEditorOpen = true
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text("Full Suite", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }

              Button(
                onClick = {
                  viewModel.saveEditedPhoto(
                    filter = quickFilter,
                    rotation = item.rotationDegrees,
                    brightness = quickBrightness,
                    contrast = quickContrast,
                    newTags = listOf("#Edited")
                  )
                  isEditorPanelOpen = false
                  activeQuickTool = null
                  Toast.makeText(context, "Edits saved!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = neonEmerald, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              IconButton(
                onClick = {
                  isEditorPanelOpen = false
                  activeQuickTool = null
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Close Editor Panel", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
              }
            }
          }

          // Active Quick Sub-Tool Controls
          when (activeQuickTool) {
            QuickEditorTool.CROP -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf("Free", "1:1", "4:3", "16:9").forEach { ratio ->
                  ElevatedFilterChip(
                    selected = cropRatio == ratio,
                    onClick = { cropRatio = ratio },
                    label = { Text(ratio, fontSize = 11.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                      selectedContainerColor = neonEmerald,
                      selectedLabelColor = Color.Black
                    )
                  )
                }
              }
            }
            QuickEditorTool.MOSAIC -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Mosaic Overlay Grid", color = Color.White, fontSize = 12.sp)
                Button(
                  onClick = { isMosaicEnabled = !isMosaicEnabled },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMosaicEnabled) neonEmerald else Color.White.copy(alpha = 0.15f),
                    contentColor = if (isMosaicEnabled) Color.Black else Color.White
                  ),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier.height(30.dp)
                ) {
                  Text(if (isMosaicEnabled) "Enabled" else "Off", fontSize = 11.sp)
                }
              }
            }
            QuickEditorTool.DOODLE -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  listOf(neonEmerald, Color.White, Color(0xFFFF3B30), Color(0xFF0066FF), Color(0xFFFFD60A)).forEach { col ->
                    Box(
                      modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(col)
                        .border(if (doodleColor == col) 2.dp else 0.dp, Color.White, CircleShape)
                        .clickable { doodleColor = col }
                    )
                  }
                }
                TextButton(
                  onClick = { doodleStrokes.clear() },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text("Clear", color = Color(0xFFFF453A), fontSize = 11.sp)
                }
              }
            }
            QuickEditorTool.ADJUST -> {
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("Brightness", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Slider(
                  value = quickBrightness,
                  onValueChange = { quickBrightness = it },
                  valueRange = -60f..60f,
                  colors = SliderDefaults.colors(thumbColor = neonEmerald, activeTrackColor = neonEmerald),
                  modifier = Modifier.height(28.dp)
                )
                Text("Contrast", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Slider(
                  value = quickContrast,
                  onValueChange = { quickContrast = it },
                  valueRange = 0.5f..1.8f,
                  colors = SliderDefaults.colors(thumbColor = neonEmerald, activeTrackColor = neonEmerald),
                  modifier = Modifier.height(28.dp)
                )
              }
            }
            QuickEditorTool.BLUR -> {
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("Gaussian Blur: ${quickBlurDp.toInt()}dp", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Slider(
                  value = quickBlurDp,
                  onValueChange = { quickBlurDp = it },
                  valueRange = 0f..20f,
                  colors = SliderDefaults.colors(thumbColor = neonEmerald, activeTrackColor = neonEmerald),
                  modifier = Modifier.height(28.dp)
                )
              }
            }
            QuickEditorTool.HDR -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Dynamic HDR Boost", color = Color.White, fontSize = 12.sp)
                Button(
                  onClick = { isHdrEnabled = !isHdrEnabled },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHdrEnabled) neonEmerald else Color.White.copy(alpha = 0.15f),
                    contentColor = if (isHdrEnabled) Color.Black else Color.White
                  ),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier.height(30.dp)
                ) {
                  Text(if (isHdrEnabled) "HDR ON" else "HDR OFF", fontSize = 11.sp)
                }
              }
            }
            QuickEditorTool.EFFECT -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf("NORMAL", "VIVID", "WARM", "COOL", "MONO", "SEPIA").forEach { eff ->
                  ElevatedFilterChip(
                    selected = quickFilter == eff,
                    onClick = { quickFilter = eff },
                    label = { Text(eff, fontSize = 10.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                      selectedContainerColor = neonEmerald,
                      selectedLabelColor = Color.Black
                    )
                  )
                }
              }
            }
            null -> {}
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Horizontal Tool Buttons Row: Crop, Mosaic, Doodle, Adjust, Blur, HDR, Effect
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            QuickEditorTool.values().forEach { tool ->
              val isSelected = activeQuickTool == tool
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .widthIn(min = 48.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    activeQuickTool = if (activeQuickTool == tool) null else tool
                  }
                  .padding(vertical = 4.dp)
                  .testTag("quick_tool_${tool.name.lowercase()}")
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(
                      if (isSelected) neonEmerald.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                      RoundedCornerShape(8.dp)
                    )
                    .border(
                      width = if (isSelected) 1.dp else 0.dp,
                      color = if (isSelected) neonEmerald else Color.Transparent,
                      shape = RoundedCornerShape(8.dp)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = if (isSelected) neonEmerald else Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = tool.label,
                  color = if (isSelected) neonEmerald else Color.White,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }
      }
    }

    // 5. Full Photo Editor Suite Overlay
    if (isFullEditorOpen) {
      PhotoEditorScreen(
        item = item,
        viewModel = viewModel,
        onDismiss = { isFullEditorOpen = false },
        onSaved = { isFullEditorOpen = false }
      )
    }

    // 6. Move Media Dialog
    if (isMoveDialogOpen) {
      val albums = listOf("Camera", "Downloads", "Animals", "Nature", "Screenshots", "WhatsApp")
      AlertDialog(
        onDismissRequest = { isMoveDialogOpen = false },
        title = { Text("Move Media", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text("Select destination folder or secure vault:", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  viewModel.moveToVault(item.id)
                  isMoveDialogOpen = false
                  Toast.makeText(context, "Moved to Safe Vault", Toast.LENGTH_SHORT).show()
                  onOpenVault()
                }
                .padding(vertical = 10.dp, horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text("Safe Vault (Protected)", fontWeight = FontWeight.SemiBold, color = CyberCyan)
            }

            albums.forEach { targetAlbum ->
              if (!targetAlbum.equals(item.album, ignoreCase = true)) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      viewModel.moveMediaToAlbum(item, targetAlbum)
                      isMoveDialogOpen = false
                      Toast.makeText(context, "Moved to $targetAlbum", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(targetAlbum, fontWeight = FontWeight.Medium)
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { isMoveDialogOpen = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // 7. Rename Dialog
    if (isRenameDialogOpen) {
      AlertDialog(
        onDismissRequest = { isRenameDialogOpen = false },
        title = { Text("Rename Photo", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text("Enter new name:", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
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
                viewModel.renameMedia(item, renameInput)
                isRenameDialogOpen = false
                Toast.makeText(context, "Renamed to $renameInput", Toast.LENGTH_SHORT).show()
              }
            }
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

    // 8. EXIF Details & Custom Text Tags Dialog
    if (isInfoDialogOpen) {
      val fullDateFormatted = remember(item.dateAdded) {
        SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(item.dateAdded))
      }

      AlertDialog(
        onDismissRequest = { isInfoDialogOpen = false },
        title = {
          Text(text = "Media Details", fontWeight = FontWeight.Bold)
        },
        text = {
          Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            DetailItem(label = "Title", value = item.title)
            DetailItem(label = "Date & Time", value = fullDateFormatted)
            DetailItem(label = "Resolution", value = item.resolution)
            DetailItem(label = "File Size", value = item.formattedSize)
            DetailItem(label = "Location", value = item.location)
            DetailItem(label = "Album", value = item.album)
            DetailItem(label = "Storage Path", value = item.uri)
            Spacer(modifier = Modifier.height(8.dp))
            MediaTagsInfoSection(item = item, viewModel = viewModel)
          }
        },
        confirmButton = {
          TextButton(onClick = { isInfoDialogOpen = false }) {
            Text("Close", color = CyberCyan)
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaTagsInfoSection(
  item: MediaItem,
  viewModel: GalleryViewModel
) {
  var tagInput by remember { mutableStateOf("") }
  val context = LocalContext.current

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Custom Tags",
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(6.dp))

    if (item.tags.isEmpty()) {
      Text(
        text = "No tags yet. Add a custom tag below.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    } else {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item.tags.forEach { tag ->
          InputChip(
            selected = true,
            onClick = {},
            label = { Text(tag, fontSize = 11.sp) },
            trailingIcon = {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove tag $tag",
                modifier = Modifier
                  .size(14.dp)
                  .clickable { viewModel.removeCustomTag(item, tag) }
              )
            },
            colors = InputChipDefaults.inputChipColors(
              selectedContainerColor = Color(0xFF0066FF).copy(alpha = 0.14f),
              selectedLabelColor = MaterialTheme.colorScheme.onSurface
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = tagInput,
        onValueChange = { tagInput = it },
        placeholder = { Text("Add custom tag...", fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("custom_tag_input")
      )
      Button(
        onClick = {
          if (tagInput.isNotBlank()) {
            viewModel.addCustomTag(item, tagInput)
            tagInput = ""
            Toast.makeText(context, "Tag added", Toast.LENGTH_SHORT).show()
          }
        },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.testTag("add_custom_tag_btn")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Tag", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add", fontSize = 12.sp)
      }
    }
  }
}

@Composable
fun ViewerActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  onClick: () -> Unit,
  tint: Color = Color.White
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag("action_btn_${label.lowercase()}")
  ) {
    Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Medium)
  }
}

@Composable
fun DetailItem(label: String, value: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
  }
}
