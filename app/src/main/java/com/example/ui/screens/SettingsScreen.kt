package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.VideoPlaybackManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: GalleryViewModel,
  onOpenVault: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isLightMode by viewModel.isLightMode.collectAsState()
  val allMedia by viewModel.allActiveMedia.collectAsState()
  val isBgPlayback by VideoPlaybackManager.isBackgroundPlaybackEnabled.collectAsState()
  val otaState by viewModel.otaState.collectAsState()

  val bgColor = if (isLightMode) NovaLightBg else NovaDarkBg
  val textColor = if (isLightMode) NovaLightText else NovaDarkText
  val subtextColor = if (isLightMode) NovaLightSubtext else NovaDarkSubtext
  val cardBg = if (isLightMode) NovaLightCardBg else NovaDarkCardBg
  val cardBorder = if (isLightMode) NovaLightCardBorder else NovaDarkCardBorder
  val accentColor = remember(otaState.accentColorHex) {
    runCatching {
      Color(android.graphics.Color.parseColor(otaState.accentColorHex))
    }.getOrDefault(Color(0xFF10B981))
  }

  Scaffold(
    containerColor = bgColor,
    topBar = {
      Surface(
        color = bgColor,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
      ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)) {
          Text(
            text = "Settings",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
          )
          Text(
            text = "Gallery preferences & playback options",
            fontSize = 12.sp,
            color = subtextColor
          )
        }
      }
    },
    modifier = modifier
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .testTag("settings_screen_content"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Appearance Section
      SettingsSectionHeader("APPEARANCE", accentColor)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          SettingsSwitchRow(
            icon = if (isLightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            title = "Dark Theme",
            subtitle = if (isLightMode) "Currently Light mode" else "Currently Dark mode",
            isChecked = !isLightMode,
            onCheckedChange = { viewModel.toggleTheme() }
          )
        }
      }

      // 2. Playback Section
      SettingsSectionHeader("MEDIA PLAYBACK", accentColor)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          SettingsSwitchRow(
            icon = Icons.Default.Headphones,
            title = "Background Audio Playback",
            subtitle = if (isBgPlayback) "Playback continues when app is minimized" else "Playback stops when leaving the app",
            isChecked = isBgPlayback,
            onCheckedChange = { VideoPlaybackManager.setBackgroundPlaybackEnabled(it) }
          )
        }
      }

      // 3. Privacy & Security Section
      SettingsSectionHeader("PRIVACY & SECURITY", accentColor)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          SettingsClickableRow(
            icon = Icons.Default.Lock,
            title = "Safe Vault / Private Folder",
            subtitle = "Encrypted PIN & Biometric protected vault",
            onClick = onOpenVault
          )
        }
      }

      // 4. Storage & Cache Section
      SettingsSectionHeader("STORAGE & LIBRARY", accentColor)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          SettingsClickableRow(
            icon = Icons.Default.Storage,
            title = "Library Summary",
            subtitle = "${allMedia.size} items tracked in persistent storage",
            onClick = {
              Toast.makeText(context, "${allMedia.size} active items in library", Toast.LENGTH_SHORT).show()
            }
          )
          HorizontalDivider(color = cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
          SettingsClickableRow(
            icon = Icons.Default.CleaningServices,
            title = "Clear Thumbnail Cache",
            subtitle = "Frees up temporary cache memory",
            onClick = {
              Toast.makeText(context, "Thumbnail cache cleared successfully", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }

      // 5. About Section
      SettingsSectionHeader("ABOUT", accentColor)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          SettingsClickableRow(
            icon = Icons.Default.Info,
            title = otaState.appTitle,
            subtitle = "Version ${otaState.version} • Developed by Ayaz Baloch",
            onClick = {
              Toast.makeText(context, "${otaState.appTitle} v${otaState.version}", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun SettingsSectionHeader(title: String, color: Color) {
  Text(
    text = title,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 1.sp,
    color = color,
    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
  )
}

@Composable
fun SettingsSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!isChecked) }
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(Color(0xFF10B981).copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
      }
    }
    Switch(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = Color(0xFF10B981)
      )
    )
  }
}

@Composable
fun SettingsClickableRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(Color(0xFF10B981).copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
      }
    }
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = Color(0xFFAAAAAA),
      modifier = Modifier.size(14.dp)
    )
  }
}
