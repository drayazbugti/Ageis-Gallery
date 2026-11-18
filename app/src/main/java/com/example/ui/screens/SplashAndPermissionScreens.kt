package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onTimeout: () -> Unit,
  isLightMode: Boolean = false,
  modifier: Modifier = Modifier
) {
  LaunchedEffect(Unit) {
    delay(1500L)
    onTimeout()
  }

  val bgColor = if (isLightMode) Color(0xFFF5F6FA) else Color(0xFF0B0F15)
  val titleColor = if (isLightMode) Color(0xFF111111) else Color.White
  val subtitleColor = if (isLightMode) Color(0xFF666666) else Color(0xFF8C96A5)

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
          .size(80.dp)
          .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x4D0066FF))
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF0066FF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Image,
          contentDescription = "Rimzim Gallery Logo",
          tint = Color.White,
          modifier = Modifier.size(45.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "रिमझिम",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = titleColor
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Rimzim Gallery",
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = subtitleColor
      )

      Spacer(modifier = Modifier.height(30.dp))

      CircularProgressIndicator(
        modifier = Modifier.size(32.dp),
        color = Color(0xFF0066FF),
        strokeWidth = 3.dp,
        trackColor = if (isLightMode) Color(0xFFE5E7EB) else Color(0xFF1A2230)
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
    // Upper Content (.permission-content)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 60.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Permission Icon Container (.permission-icon: 90x90px, circular #eef4ff)
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(CircleShape)
          .background(Color(0xFFEEF4FF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Storage Access",
          tint = Color(0xFF0066FF),
          modifier = Modifier.size(45.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Title (.permission-title: 22px, bold)
      Text(
        text = "Allow Storage Access",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1A1A),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Description (.permission-desc: 14px, line-height 1.5, max-width 300px)
      Text(
        text = "Nova Gallery needs access to your device storage to scan and display all your photos and videos cleanly.",
        fontSize = 14.sp,
        color = Color(0xFF666666),
        lineHeight = 21.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // Bottom Action Button (.permission-btn)
    Button(
      onClick = onAllowClicked,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0x4D0066FF))
        .testTag("permission_allow_button"),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF0066FF),
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
