package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.service.VideoPlaybackManager
import com.example.service.VideoRepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerMenuBottomSheet(
  item: MediaItem,
  onDismiss: () -> Unit,
  onEnterPip: () -> Unit,
  onSaveToPlaylist: () -> Unit,
  onAddTag: (String) -> Unit = {},
  onRemoveTag: (String) -> Unit = {}
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val isBgPlayback by VideoPlaybackManager.isBackgroundPlaybackEnabled.collectAsState()
  val repeatMode by VideoPlaybackManager.repeatMode.collectAsState()
  val playbackSpeed by VideoPlaybackManager.playbackSpeed.collectAsState()
  val sleepMinutes by VideoPlaybackManager.sleepTimerMinutes.collectAsState()
  val sleepSecondsLeft by VideoPlaybackManager.sleepTimerSecondsLeft.collectAsState()
  val isAudioOnly by VideoPlaybackManager.isAudioOnlyMode.collectAsState()
  val isControlsLocked by VideoPlaybackManager.isControlsLocked.collectAsState()
  val pointA by VideoPlaybackManager.pointA.collectAsState()
  val pointB by VideoPlaybackManager.pointB.collectAsState()
  val bookmarks by VideoPlaybackManager.bookmarks.collectAsState()
  val eqPreset by VideoPlaybackManager.equalizerPreset.collectAsState()

  // Sub-dialogs states
  var showSpeedDialog by remember { mutableStateOf(false) }
  var showSleepDialog by remember { mutableStateOf(false) }
  var showJumpDialog by remember { mutableStateOf(false) }
  var showEqualizerDialog by remember { mutableStateOf(false) }
  var showInfoDialog by remember { mutableStateOf(false) }
  var showBookmarksDialog by remember { mutableStateOf(false) }
  var showABRepeatDialog by remember { mutableStateOf(false) }
  var showTipsDialog by remember { mutableStateOf(false) }
  var showControlSettingsDialog by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    tonalElevation = 8.dp,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("video_player_popup_menu")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Video Options",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111111)
          )
          Text(
            text = item.title,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(260.dp)
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(36.dp)
            .background(Color(0xFFF4F4F4), CircleShape)
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF444444), modifier = Modifier.size(20.dp))
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Dedicated Background Playback Toggle Switch Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isBgPlayback) Color(0xFFEEF4FF) else Color(0xFFF8F9FA)),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isBgPlayback) Color(0xFF0066FF).copy(alpha = 0.3f) else Color(0xFFE9ECEF)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            VideoPlaybackManager.setBackgroundPlaybackEnabled(!isBgPlayback)
          }
          .testTag("toggle_background_playback")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(if (isBgPlayback) Color(0xFF0066FF) else Color(0xFFDDDDDD), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "Background Audio Playback",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
              )
              Text(
                text = if (isBgPlayback) "Playback stays active when app is minimized" else "Playback stops immediately when app is closed",
                fontSize = 11.sp,
                color = if (isBgPlayback) Color(0xFF0052CC) else Color(0xFF777777)
              )
            }
          }

          Switch(
            checked = isBgPlayback,
            onCheckedChange = { VideoPlaybackManager.setBackgroundPlaybackEnabled(it) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = Color(0xFF0066FF),
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = Color(0xFFCCCCCC)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Options Grid / List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Pop-Up Player (PiP)
        item {
          MenuOptionRow(
            icon = Icons.Default.PictureInPicture,
            title = "Pop-Up Player (PiP)",
            subtitle = "Floating window while using other apps",
            badge = "Picture-in-Picture",
            onClick = {
              onDismiss()
              onEnterPip()
            }
          )
        }

        // Lock Controls
        item {
          MenuOptionRow(
            icon = Icons.Default.Lock,
            title = if (isControlsLocked) "Unlock Controls" else "Lock Screen Controls",
            subtitle = if (isControlsLocked) "Controls locked to avoid touches" else "Disable accidental touches while watching",
            badge = if (isControlsLocked) "Locked" else null,
            badgeColor = Color(0xFFFF9500),
            onClick = {
              VideoPlaybackManager.toggleControlsLock()
              onDismiss()
              Toast.makeText(
                context,
                if (!isControlsLocked) "Screen locked! Tap unlock icon to restore" else "Controls unlocked",
                Toast.LENGTH_SHORT
              ).show()
            }
          )
        }

        // Sleep Timer
        item {
          val timerLabel = if (sleepSecondsLeft != null) {
            val min = sleepSecondsLeft!! / 60
            val sec = sleepSecondsLeft!! % 60
            String.format("%02d:%02d left", min, sec)
          } else if (sleepMinutes != null) {
            "${sleepMinutes}m"
          } else null

          MenuOptionRow(
            icon = Icons.Default.Timer,
            title = "Sleep Timer",
            subtitle = "Automatically pause after a set time",
            badge = timerLabel,
            badgeColor = Color(0xFF0066FF),
            onClick = { showSleepDialog = true }
          )
        }

        // Playback Speed
        item {
          MenuOptionRow(
            icon = Icons.Default.Speed,
            title = "Playback Speed",
            subtitle = "Current: ${playbackSpeed}x",
            badge = "${playbackSpeed}x",
            onClick = { showSpeedDialog = true }
          )
        }

        // Jump to Time
        item {
          MenuOptionRow(
            icon = Icons.Default.Schedule,
            title = "Jump to Time",
            subtitle = "Jump directly to specific minute & second",
            onClick = { showJumpDialog = true }
          )
        }

        // Play as Audio
        item {
          MenuOptionRow(
            icon = Icons.Default.MusicNote,
            title = "Play as Audio Only",
            subtitle = "Turn off video surface and save power",
            badge = if (isAudioOnly) "Active" else null,
            badgeColor = Color(0xFF34C759),
            onClick = {
              VideoPlaybackManager.toggleAudioOnlyMode()
              onDismiss()
              Toast.makeText(context, if (!isAudioOnly) "Audio-only mode activated" else "Video mode restored", Toast.LENGTH_SHORT).show()
            }
          )
        }

        // Equalizer / Audio Effects
        item {
          MenuOptionRow(
            icon = Icons.Default.GraphicEq,
            title = "Equalizer & Audio",
            subtitle = "Sound preset: $eqPreset",
            badge = eqPreset,
            onClick = { showEqualizerDialog = true }
          )
        }

        // Repeat Mode
        item {
          val repeatLabel = when (repeatMode) {
            VideoRepeatMode.OFF -> "Off"
            VideoRepeatMode.ONE -> "Single Loop"
            VideoRepeatMode.ALL -> "Loop All"
          }
          MenuOptionRow(
            icon = if (repeatMode == VideoRepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
            title = "Repeat Mode",
            subtitle = "Change loop playback setting",
            badge = repeatLabel,
            onClick = {
              val newMode = VideoPlaybackManager.toggleRepeatMode()
              Toast.makeText(context, "Repeat: $newMode", Toast.LENGTH_SHORT).show()
            }
          )
        }

        // A-B Repeat Loop
        item {
          val abText = if (pointA != null && pointB != null) {
            "${formatTimeMs(pointA!!)} - ${formatTimeMs(pointB!!)}"
          } else if (pointA != null) {
            "A: ${formatTimeMs(pointA!!)}"
          } else "Off"

          MenuOptionRow(
            icon = Icons.Default.Loop,
            title = "A-B Repeat Segment",
            subtitle = "Set points to loop a specific clip segment",
            badge = if (pointA != null) abText else null,
            badgeColor = Color(0xFF8E44AD),
            onClick = { showABRepeatDialog = true }
          )
        }

        // Bookmarks
        item {
          MenuOptionRow(
            icon = Icons.Default.Bookmark,
            title = "Bookmarks",
            subtitle = "${bookmarks.size} saved key moments",
            badge = if (bookmarks.isNotEmpty()) "${bookmarks.size}" else null,
            onClick = { showBookmarksDialog = true }
          )
        }

        // Video Information
        item {
          MenuOptionRow(
            icon = Icons.Default.Info,
            title = "Video Information & Tags",
            subtitle = "${item.resolution} • ${item.formattedSize} • ${item.tags.size} tags",
            onClick = { showInfoDialog = true }
          )
        }

        // Save Playlist / Favorite
        item {
          MenuOptionRow(
            icon = Icons.Default.PlaylistAdd,
            title = "Save to Playlist",
            subtitle = "Add to custom favorites or album list",
            onClick = {
              onDismiss()
              onSaveToPlaylist()
            }
          )
        }

        // Control Settings
        item {
          MenuOptionRow(
            icon = Icons.Default.Tune,
            title = "Control Settings",
            subtitle = "Configure swipe gestures and sensitivity",
            onClick = { showControlSettingsDialog = true }
          )
        }

        // Video Player Tips
        item {
          MenuOptionRow(
            icon = Icons.Default.Lightbulb,
            title = "Video Player Tips",
            subtitle = "Learn quick swipe, seek & tap shortcuts",
            badge = "Guide",
            badgeColor = Color(0xFFF39C12),
            onClick = { showTipsDialog = true }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }
  }

  // Speed Dialog
  if (showSpeedDialog) {
    SpeedSelectionDialog(
      currentSpeed = playbackSpeed,
      onSpeedSelected = {
        VideoPlaybackManager.setPlaybackSpeed(it)
        showSpeedDialog = false
      },
      onDismiss = { showSpeedDialog = false }
    )
  }

  // Sleep Timer Dialog
  if (showSleepDialog) {
    SleepTimerDialog(
      currentMinutes = sleepMinutes,
      onSelectMinutes = {
        VideoPlaybackManager.setSleepTimer(it)
        showSleepDialog = false
      },
      onDismiss = { showSleepDialog = false }
    )
  }

  // Jump to Time Dialog
  if (showJumpDialog) {
    JumpToTimeDialog(
      durationMs = VideoPlaybackManager.duration.value,
      onJump = { posMs ->
        VideoPlaybackManager.seekTo(posMs)
        showJumpDialog = false
      },
      onDismiss = { showJumpDialog = false }
    )
  }

  // Equalizer Dialog
  if (showEqualizerDialog) {
    EqualizerDialog(
      activePreset = eqPreset,
      onSelectPreset = {
        VideoPlaybackManager.setEqualizerPreset(it)
        showEqualizerDialog = false
      },
      onDismiss = { showEqualizerDialog = false }
    )
  }

  // Video Info Dialog
  if (showInfoDialog) {
    VideoInfoDialog(
      item = item,
      onAddTag = onAddTag,
      onRemoveTag = onRemoveTag,
      onDismiss = { showInfoDialog = false }
    )
  }

  // Bookmarks Dialog
  if (showBookmarksDialog) {
    BookmarksDialog(
      bookmarks = bookmarks,
      currentPosMs = VideoPlaybackManager.currentPosition.value,
      onAddBookmark = { VideoPlaybackManager.addBookmark() },
      onJump = {
        VideoPlaybackManager.seekTo(it)
        showBookmarksDialog = false
        onDismiss()
      },
      onDelete = { VideoPlaybackManager.removeBookmark(it) },
      onDismiss = { showBookmarksDialog = false }
    )
  }

  // A-B Repeat Dialog
  if (showABRepeatDialog) {
    ABRepeatDialog(
      pointA = pointA,
      pointB = pointB,
      currentPosMs = VideoPlaybackManager.currentPosition.value,
      onSetPointA = { VideoPlaybackManager.setPointA() },
      onSetPointB = { VideoPlaybackManager.setPointB() },
      onClear = { VideoPlaybackManager.clearABRepeat() },
      onDismiss = { showABRepeatDialog = false }
    )
  }

  // Tips Dialog
  if (showTipsDialog) {
    PlayerTipsDialog(onDismiss = { showTipsDialog = false })
  }

  // Control Settings Dialog
  if (showControlSettingsDialog) {
    ControlSettingsDialog(onDismiss = { showControlSettingsDialog = false })
  }
}

@Composable
fun MenuOptionRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  badge: String? = null,
  badgeColor: Color = Color(0xFF0066FF),
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
        Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
      }
    }

    if (badge != null) {
      Box(
        modifier = Modifier
          .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(text = badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
      }
    }
  }
}

@Composable
fun SpeedSelectionDialog(
  currentSpeed: Float,
  onSpeedSelected: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("Playback Speed", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column {
        speeds.forEach { speed ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSpeedSelected(speed) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentSpeed == speed,
              onClick = { onSpeedSelected(speed) },
              colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0066FF))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
              fontSize = 15.sp,
              fontWeight = if (currentSpeed == speed) FontWeight.Bold else FontWeight.Normal,
              color = Color(0xFF111111)
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Color(0xFF0066FF))
      }
    }
  )
}

@Composable
fun SleepTimerDialog(
  currentMinutes: Int?,
  onSelectMinutes: (Int?) -> Unit,
  onDismiss: () -> Unit
) {
  val options = listOf(
    null to "Off",
    15 to "15 minutes",
    30 to "30 minutes",
    45 to "45 minutes",
    60 to "60 minutes"
  )
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("Sleep Timer", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column {
        options.forEach { (mins, label) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectMinutes(mins) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentMinutes == mins,
              onClick = { onSelectMinutes(mins) },
              colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0066FF))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = label,
              fontSize = 15.sp,
              fontWeight = if (currentMinutes == mins) FontWeight.Bold else FontWeight.Normal,
              color = Color(0xFF111111)
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = Color(0xFF0066FF))
      }
    }
  )
}

@Composable
fun JumpToTimeDialog(
  durationMs: Long,
  onJump: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  var minutesText by remember { mutableStateOf("") }
  var secondsText by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("Jump to Timestamp", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column {
        Text(
          text = "Enter target position (Total: ${formatTimeMs(durationMs)}):",
          fontSize = 13.sp,
          color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = minutesText,
            onValueChange = { if (it.length <= 3) minutesText = it.filter { c -> c.isDigit() } },
            label = { Text("Min") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          Text(":", fontWeight = FontWeight.Bold, fontSize = 20.sp)
          OutlinedTextField(
            value = secondsText,
            onValueChange = { if (it.length <= 2) secondsText = it.filter { c -> c.isDigit() } },
            label = { Text("Sec") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val m = minutesText.toLongOrNull() ?: 0L
          val s = secondsText.toLongOrNull() ?: 0L
          val targetMs = (m * 60 + s) * 1000L
          onJump(targetMs.coerceIn(0L, durationMs))
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
      ) {
        Text("Jump")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Color(0xFF666666))
      }
    }
  )
}

@Composable
fun EqualizerDialog(
  activePreset: String,
  onSelectPreset: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val presets = listOf("Flat", "Bass Boost", "Vocal Clarity", "Rock", "Pop", "Electronic")
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("Audio Equalizer Presets", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column {
        presets.forEach { preset ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectPreset(preset) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = activePreset == preset,
              onClick = { onSelectPreset(preset) },
              colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0066FF))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = preset,
              fontSize = 15.sp,
              fontWeight = if (activePreset == preset) FontWeight.Bold else FontWeight.Normal,
              color = Color(0xFF111111)
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = Color(0xFF0066FF))
      }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoInfoDialog(
  item: MediaItem,
  onAddTag: (String) -> Unit = {},
  onRemoveTag: (String) -> Unit = {},
  onDismiss: () -> Unit
) {
  var tagInput by remember { mutableStateOf("") }
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("Video Information", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
      ) {
        InfoRow("Title", item.title)
        InfoRow("Resolution", item.resolution)
        InfoRow("Duration", item.formattedDuration)
        InfoRow("File Size", item.formattedSize)
        InfoRow("Format / Codec", "MP4 / H.264 / AAC")
        InfoRow("Path", item.uri)

        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Custom Tags", fontSize = 11.sp, color = Color(0xFF888888))
        if (item.tags.isEmpty()) {
          Text(
            text = "No tags yet. Add a custom tag below.",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
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
                label = { Text(tag, fontSize = 11.sp, color = Color(0xFF1E293B)) },
                trailingIcon = {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove tag $tag",
                    tint = Color(0xFF475569),
                    modifier = Modifier
                      .size(14.dp)
                      .clickable { onRemoveTag(tag) }
                  )
                },
                colors = InputChipDefaults.inputChipColors(
                  selectedContainerColor = Color(0xFF0066FF).copy(alpha = 0.12f),
                  selectedLabelColor = Color(0xFF1E293B)
                )
              )
            }
          }
        }

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
              .testTag("video_sheet_tag_input")
          )
          Button(
            onClick = {
              if (tagInput.isNotBlank()) {
                onAddTag(tagInput)
                tagInput = ""
                Toast.makeText(context, "Tag added", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("video_sheet_add_tag_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Tag", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add", fontSize = 12.sp)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = Color(0xFF0066FF))
      }
    }
  )
}

@Composable
fun InfoRow(label: String, value: String) {
  Column {
    Text(text = label, fontSize = 11.sp, color = Color(0xFF888888))
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF222222))
  }
}

@Composable
fun BookmarksDialog(
  bookmarks: List<Long>,
  currentPosMs: Long,
  onAddBookmark: () -> Unit,
  onJump: (Long) -> Unit,
  onDelete: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Video Bookmarks", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
        Button(
          onClick = onAddBookmark,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ ${formatTimeMs(currentPosMs)}", fontSize = 12.sp)
        }
      }
    },
    text = {
      if (bookmarks.isEmpty()) {
        Text("No bookmarks yet. Tap the button above to bookmark the current timestamp.", fontSize = 13.sp, color = Color.Gray)
      } else {
        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
          items(bookmarks) { mark ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clickable { onJump(mark) }
                  .weight(1f)
              ) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF0066FF), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(formatTimeMs(mark), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111111))
              }
              IconButton(onClick = { onDelete(mark) }) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = Color(0xFF0066FF))
      }
    }
  )
}

@Composable
fun ABRepeatDialog(
  pointA: Long?,
  pointB: Long?,
  currentPosMs: Long,
  onSetPointA: () -> Unit,
  onSetPointB: () -> Unit,
  onClear: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("A-B Repeat Segment", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Loop a specific segment of the video continuously:", fontSize = 13.sp, color = Color.Gray)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Point A (Start): ${if (pointA != null) formatTimeMs(pointA) else "Not set"}", fontWeight = FontWeight.Medium)
          Button(
            onClick = onSetPointA,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text("Set at ${formatTimeMs(currentPosMs)}")
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Point B (End): ${if (pointB != null) formatTimeMs(pointB) else "Not set"}", fontWeight = FontWeight.Medium)
          Button(
            onClick = onSetPointB,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text("Set at ${formatTimeMs(currentPosMs)}")
          }
        }

        if (pointA != null || pointB != null) {
          OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
            Text("Clear A-B Repeat Loop", color = Color.Red)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = Color(0xFF0066FF))
      }
    }
  )
}

@Composable
fun PlayerTipsDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("Gesture Controls & Tips", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TipItem("↔ Swipe Left / Right", "Fast-forward or rewind smoothly by sliding anywhere on screen")
        TipItem("↕ Left Edge Vertical Swipe", "Slide up/down on left edge to adjust display brightness")
        TipItem("↕ Right Edge Vertical Swipe", "Slide up/down on right edge to adjust audio volume")
        TipItem("⏪ Double Tap Left", "Instantly rewind 10 seconds")
        TipItem("⏩ Double Tap Right", "Instantly jump forward 10 seconds")
        TipItem("⏯ Single Tap Center", "Toggle control overlays on and off")
        TipItem("🎧 Background Playback", "Toggle inside the 3-dots menu to keep audio playing when app is minimized")
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Got it", color = Color(0xFF0066FF))
      }
    }
  )
}

@Composable
fun TipItem(title: String, desc: String) {
  Column {
    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0066FF))
    Text(text = desc, fontSize = 11.sp, color = Color(0xFF555555))
  }
}

@Composable
fun ControlSettingsDialog(onDismiss: () -> Unit) {
  var gesturesEnabled by remember { mutableStateOf(true) }
  var autoHideFast by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Text("Control Settings", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Swipe Gestures", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Enable seek, volume and brightness swipe controls", fontSize = 11.sp, color = Color.Gray)
          }
          Switch(checked = gesturesEnabled, onCheckedChange = { gesturesEnabled = it })
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Fast Auto-Hide", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Hide controls after 2s instead of 4s", fontSize = 11.sp, color = Color.Gray)
          }
          Switch(checked = autoHideFast, onCheckedChange = { autoHideFast = it })
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Save", color = Color(0xFF0066FF))
      }
    }
  )
}

private fun formatTimeMs(millis: Long): String {
  val totalSec = (millis / 1000).coerceAtLeast(0)
  val m = totalSec / 60
  val s = totalSec % 60
  return String.format("%02d:%02d", m, s)
}
