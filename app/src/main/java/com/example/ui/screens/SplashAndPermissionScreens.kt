package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ota.OtaSyncStatus
import com.example.ota.OtaUiState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onTimeout: () -> Unit,
  otaState: OtaUiState = OtaUiState(),
  isLightMode: Boolean = false,
  modifier: Modifier = Modifier
) {
  var startAnimation by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    startAnimation = true
    delay(1800L)
    onTimeout()
  }

  val infiniteTransition = rememberInfiniteTransition(label = "SplashPulse")
  val logoScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "LogoScale"
  )

  val titleAlpha by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 1000),
    label = "TitleAlpha"
  )
  val titleScale by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0.95f,
    animationSpec = tween(durationMillis = 1000),
    label = "TitleScale"
  )

  val parsedAccent = remember(otaState.accentColorHex) {
    runCatching {
      Color(android.graphics.Color.parseColor(otaState.accentColorHex))
    }.getOrDefault(Color(0xFF10B981))
  }

  val bgColor = if (isLightMode) Color(0xFFF2FBF7) else Color(0xFF0B0F19)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(90.dp)
          .graphicsLayer {
            scaleX = logoScale
            scaleY = logoScale
          }
          .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x6610B981))
          .clip(RoundedCornerShape(24.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(Color(0xFF10B981), Color(0xFF065F46))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Image,
          contentDescription = "Ayaz Baloch Gallery Logo",
          tint = Color.White,
          modifier = Modifier.size(45.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "AYAZ BALOCH",
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 3.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFF34D399), Color(0xFF60A5FA))
          )
        ),
        modifier = Modifier.graphicsLayer {
          alpha = titleAlpha
          scaleX = titleScale
          scaleY = titleScale
        }
      )

      Spacer(modifier = Modifier.height(10.dp))

      val statusLabel = when (otaState.status) {
        OtaSyncStatus.CHECKING, OtaSyncStatus.SYNCING -> "${otaState.appTitle} • Syncing..."
        OtaSyncStatus.UPDATED -> "${otaState.appTitle} • v${otaState.version} Live"
        OtaSyncStatus.OFFLINE -> "${otaState.appTitle} • v${otaState.version}"
      }
      Surface(
        color = parsedAccent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, parsedAccent.copy(alpha = 0.35f)),
        modifier = Modifier.graphicsLayer { alpha = titleAlpha }
      ) {
        Text(
          text = statusLabel,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = parsedAccent,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(22.dp))

      CircularProgressIndicator(
        modifier = Modifier.size(32.dp),
        color = Color(0xFF10B981),
        strokeWidth = 3.dp,
        trackColor = if (isLightMode) Color(0xFFD1FAE5) else Color(0x1AFFFFFF)
      )
    }
  }
}

@Composable
fun PermissionScreen(
  onAllowClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 40.dp)
      .testTag("permission_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 60.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(CircleShape)
          .background(Color(0xFFD1FAE5)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Storage Access",
          tint = Color(0xFF059669),
          modifier = Modifier.size(45.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Allow Storage Access",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0B1F17),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Ayaz Gallery needs access to your device storage to scan and display all your photos and videos cleanly.",
        fontSize = 14.sp,
        color = Color(0xFF4B6B5D),
        lineHeight = 21.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    Button(
      onClick = onAllowClicked,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0x4D10B981))
        .testTag("permission_allow_button"),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF10B981),
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(12.dp)
    ) {
      Text(
        text = "Allow & Scan Media",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
