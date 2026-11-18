package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.security.AuthResult
import com.example.security.LockType
import com.example.security.VaultSecurityManager
import com.example.ui.components.MediaThumbnailCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.viewmodel.GalleryViewModel
import com.example.viewmodel.VaultStatus
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
  viewModel: GalleryViewModel,
  onMediaClick: (MediaItem) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val vaultStatus by viewModel.vaultStatus.collectAsState()
  val vaultedMedia by viewModel.vaultedMedia.collectAsState()
  val decoyMedia by viewModel.decoyVaultedMedia.collectAsState()
  val securityManager = viewModel.vaultSecurity

  var activeLockTab by remember { mutableStateOf(securityManager.getLockType()) }
  var isSettingsOpen by remember { mutableStateOf(false) }
  var isSetPinDialogOpen by remember { mutableStateOf(false) }
  var isCalculatorMode by remember { mutableStateOf(securityManager.isCalculatorDisguiseEnabled()) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background, // Solid clean background
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (vaultStatus == VaultStatus.LOCKED) Icons.Default.Lock else Icons.Default.LockOpen,
              contentDescription = null,
              tint = CyberCyan
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = when (vaultStatus) {
                VaultStatus.LOCKED -> if (isCalculatorMode) "Calculator" else "Safe Vault"
                VaultStatus.UNLOCKED_MASTER -> "Master Vault (Encrypted)"
                VaultStatus.UNLOCKED_DECOY -> "Private Vault"
              },
              fontWeight = FontWeight.Bold
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (vaultStatus != VaultStatus.LOCKED) {
            IconButton(onClick = { isSettingsOpen = true }) {
              Icon(Icons.Default.Settings, contentDescription = "Vault Settings")
            }
            IconButton(onClick = { viewModel.lockVault() }) {
              Icon(Icons.Default.Lock, contentDescription = "Lock Now", tint = CyberCyan)
            }
          } else {
            // Option to change PIN directly from lock screen
            IconButton(onClick = { isSetPinDialogOpen = true }) {
              Icon(Icons.Default.Key, contentDescription = "Configure PIN", tint = CyberCyan)
            }

            // Toggle disguised calculator shortcut
            IconButton(onClick = { isCalculatorMode = !isCalculatorMode }) {
              Icon(
                imageVector = if (isCalculatorMode) Icons.Default.Lock else Icons.Default.Calculate,
                contentDescription = "Toggle Disguise",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
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
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (vaultStatus == VaultStatus.LOCKED) {
        if (isCalculatorMode) {
          // --- Disguised Calculator Mode ---
          DisguisedCalculatorView(
            onEvaluateSecret = { secret ->
              val result = viewModel.unlockVaultWithSecret(secret)
              if (result is AuthResult.Error) {
                errorMessage = "Invalid key"
              }
            }
          )
        } else {
          // --- Standard Vault Lock Screen ---
          VaultLockView(
            securityManager = securityManager,
            selectedLockType = activeLockTab,
            onLockTypeChange = { activeLockTab = it },
            onSecretEntered = { secret ->
              val res = viewModel.unlockVaultWithSecret(secret)
              if (res is AuthResult.Error) {
                errorMessage = res.message
              } else {
                errorMessage = null
              }
            },
            onBiometricAuth = {
              viewModel.unlockVaultWithBiometrics()
            },
            onOpenSetPinDialog = {
              isSetPinDialogOpen = true
            },
            errorMessage = errorMessage
          )
        }
      } else {
        // --- Vault Contents (Master or Decoy) ---
        val displayItems = if (vaultStatus == VaultStatus.UNLOCKED_DECOY) {
          decoyMedia
        } else {
          vaultedMedia.filter { !it.isDecoy }
        }

        VaultContentsView(
          items = displayItems,
          isDecoy = vaultStatus == VaultStatus.UNLOCKED_DECOY,
          onMediaClick = onMediaClick,
          onUnhide = { viewModel.removeFromVault(it.id) },
          onDeletePermanent = { viewModel.deletePermanently(listOf(it.id)) }
        )
      }
    }

    // Direct Set/Change PIN Dialog
    if (isSetPinDialogOpen) {
      SetPinDialog(
        securityManager = securityManager,
        onDismiss = { isSetPinDialogOpen = false },
        onPinSaved = {
          errorMessage = null
          isSetPinDialogOpen = false
        }
      )
    }

    // Vault Settings Dialog
    if (isSettingsOpen) {
      VaultSettingsDialog(
        securityManager = securityManager,
        onDismiss = { isSettingsOpen = false },
        onSettingsUpdated = {
          activeLockTab = securityManager.getLockType()
          isCalculatorMode = securityManager.isCalculatorDisguiseEnabled()
        }
      )
    }
  }
}

@Composable
fun VaultLockView(
  securityManager: VaultSecurityManager,
  selectedLockType: LockType,
  onLockTypeChange: (LockType) -> Unit,
  onSecretEntered: (String) -> Unit,
  onBiometricAuth: () -> Unit,
  onOpenSetPinDialog: () -> Unit,
  errorMessage: String?
) {
  var pinInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 8.dp)
      .padding(bottom = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top
  ) {
    Box(
      modifier = Modifier
        .size(54.dp)
        .clip(CircleShape)
        .background(Brush.linearGradient(listOf(CyberCyan, ElectricViolet))),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black, modifier = Modifier.size(30.dp))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Nomi Protected Vault",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    // Dynamic PIN status indicator
    Text(
      text = if (securityManager.isCustomPinConfigured()) {
        "Enter your Master PIN or Decoy PIN"
      } else {
        "Default PIN: 1234 • Decoy: 0000"
      },
      style = MaterialTheme.typography.bodySmall,
      color = CyberCyan
    )

    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Lock Type Tabs
    TabRow(
      selectedTabIndex = selectedLockType.ordinal,
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = CyberCyan,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
    ) {
      Tab(
        selected = selectedLockType == LockType.PIN,
        onClick = { onLockTypeChange(LockType.PIN) },
        text = { Text("PIN") },
        icon = { Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp)) }
      )
      Tab(
        selected = selectedLockType == LockType.PASSWORD,
        onClick = { onLockTypeChange(LockType.PASSWORD) },
        text = { Text("Password") },
        icon = { Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp)) }
      )
      Tab(
        selected = selectedLockType == LockType.PATTERN,
        onClick = { onLockTypeChange(LockType.PATTERN) },
        text = { Text("Pattern") },
        icon = { Icon(Icons.Default.Pattern, contentDescription = null, modifier = Modifier.size(16.dp)) }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Active Input Interface
    when (selectedLockType) {
      LockType.PIN -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // PIN Indicator Dots
          Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(vertical = 8.dp)
          ) {
            repeat(4) { idx ->
              Box(
                modifier = Modifier
                  .size(15.dp)
                  .clip(CircleShape)
                  .background(
                    if (idx < pinInput.length) CyberCyan else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .border(1.dp, CyberCyan, CircleShape)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 3x4 Fully Visible Numeric Pad
          val keypad = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("BIO", "0", "DEL")
          )

          Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.width(270.dp)
          ) {
            keypad.forEach { row ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                row.forEach { key ->
                  Box(
                    modifier = Modifier
                      .size(56.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                      .clickable {
                        when (key) {
                          "DEL" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                          "BIO" -> onBiometricAuth()
                          else -> {
                            if (pinInput.length < 6) {
                              val newPin = pinInput + key
                              pinInput = newPin
                              if (newPin.length == 4) {
                                onSecretEntered(newPin)
                                pinInput = ""
                              }
                            }
                          }
                        }
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    when (key) {
                      "DEL" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                      "BIO" -> Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = CyberCyan, modifier = Modifier.size(24.dp))
                      else -> Text(text = key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Change or Set PIN Shortcut
          TextButton(onClick = onOpenSetPinDialog) {
            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp), tint = CyberCyan)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (securityManager.isCustomPinConfigured()) "Change Master PIN" else "Set Custom PIN",
              fontSize = 12.sp,
              color = CyberCyan
            )
          }
        }
      }

      LockType.PASSWORD -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
          OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            placeholder = { Text("Enter Master or Decoy Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              onSecretEntered(passwordInput)
              passwordInput = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Unlock Vault")
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = onBiometricAuth,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CyberCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Unlock with Biometrics")
          }
        }
      }

      LockType.PATTERN -> {
        PatternLockCanvas(
          onPatternComplete = { pattern ->
            onSecretEntered(pattern)
          }
        )
      }
    }
  }
}

@Composable
fun SetPinDialog(
  securityManager: VaultSecurityManager,
  onDismiss: () -> Unit,
  onPinSaved: () -> Unit
) {
  var newPin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Set Permanent Master PIN", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Enter a 4-digit PIN that will permanently protect your private vault:", fontSize = 12.sp)

        OutlinedTextField(
          value = newPin,
          onValueChange = { if (it.length <= 4) newPin = it },
          label = { Text("New 4-Digit PIN") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = confirmPin,
          onValueChange = { if (it.length <= 4) confirmPin = it },
          label = { Text("Confirm 4-Digit PIN") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (errorText != null) {
          Text(text = errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (newPin.length < 4) {
            errorText = "PIN must be at least 4 digits"
          } else if (newPin != confirmPin) {
            errorText = "PINs do not match"
          } else {
            securityManager.saveNewMasterPin(newPin)
            onPinSaved()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
      ) {
        Text("Save Permanent PIN")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun PatternLockCanvas(
  onPatternComplete: (String) -> Unit
) {
  val selectedDots = remember { mutableStateListOf<Int>() }
  var currentDragOffset by remember { mutableStateOf<Offset?>(null) }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "Connect dots to unlock (Default: L-shape 0-1-2-5-8)",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(14.dp))

    Box(
      modifier = Modifier
        .size(260.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset ->
              selectedDots.clear()
              checkHitDot(offset, size.width.toFloat(), size.height.toFloat())?.let {
                selectedDots.add(it)
              }
              currentDragOffset = offset
            },
            onDrag = { change, _ ->
              change.consume()
              currentDragOffset = change.position
              checkHitDot(change.position, size.width.toFloat(), size.height.toFloat())?.let { dot ->
                if (!selectedDots.contains(dot)) {
                  selectedDots.add(dot)
                }
              }
            },
            onDragEnd = {
              currentDragOffset = null
              if (selectedDots.isNotEmpty()) {
                val patternStr = selectedDots.joinToString(",")
                onPatternComplete(patternStr)
                selectedDots.clear()
              }
            },
            onDragCancel = {
              currentDragOffset = null
              selectedDots.clear()
            }
          )
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cellW = size.width / 3f
        val cellH = size.height / 3f

        fun getCenter(index: Int): Offset {
          val row = index / 3
          val col = index % 3
          return Offset((col + 0.5f) * cellW, (row + 0.5f) * cellH)
        }

        // Draw connecting lines
        if (selectedDots.size > 1) {
          for (i in 0 until selectedDots.size - 1) {
            val start = getCenter(selectedDots[i])
            val end = getCenter(selectedDots[i + 1])
            drawLine(
              color = CyberCyan,
              start = start,
              end = end,
              strokeWidth = 6.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        }

        // Draw line to current finger drag
        currentDragOffset?.let { finger ->
          if (selectedDots.isNotEmpty()) {
            val lastCenter = getCenter(selectedDots.last())
            drawLine(
              color = CyberCyan.copy(alpha = 0.6f),
              start = lastCenter,
              end = finger,
              strokeWidth = 4.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        }

        // Draw 9 dots
        for (i in 0 until 9) {
          val center = getCenter(i)
          val isSelected = selectedDots.contains(i)
          drawCircle(
            color = if (isSelected) CyberCyan else Color.Gray.copy(alpha = 0.5f),
            radius = if (isSelected) 14.dp.toPx() else 8.dp.toPx(),
            center = center
          )
        }
      }
    }
  }
}

private fun checkHitDot(offset: Offset, width: Float, height: Float): Int? {
  val cellW = width / 3f
  val cellH = height / 3f
  for (i in 0 until 9) {
    val row = i / 3
    val col = i % 3
    val center = Offset((col + 0.5f) * cellW, (row + 0.5f) * cellH)
    val distance = hypot(offset.x - center.x, offset.y - center.y)
    if (distance < cellW * 0.35f) {
      return i
    }
  }
  return null
}

@Composable
fun DisguisedCalculatorView(
  onEvaluateSecret: (String) -> Unit
) {
  var displayText by remember { mutableStateOf("0") }
  var pendingExpression by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // LCD Screen
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        .padding(16.dp),
      contentAlignment = Alignment.BottomEnd
    ) {
      Column(horizontalAlignment = Alignment.End) {
        if (pendingExpression.isNotEmpty()) {
          Text(text = pendingExpression, color = Color.Gray, fontSize = 16.sp)
        }
        Text(
          text = displayText,
          fontSize = 40.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          maxLines = 1
        )
      }
    }

    // Calculator Grid
    val buttons = listOf(
      listOf("C", "±", "%", "÷"),
      listOf("7", "8", "9", "×"),
      listOf("4", "5", "6", "-"),
      listOf("1", "2", "3", "+"),
      listOf("0", ".", "=")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      buttons.forEach { row ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          row.forEach { btn ->
            val isEquals = btn == "="
            val isZero = btn == "0"
            val weight = if (isZero) 2f else 1f

            Box(
              modifier = Modifier
                .weight(weight)
                .height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                  when {
                    isEquals -> CyberCyan
                    btn in listOf("+", "-", "×", "÷") -> ElectricViolet
                    btn in listOf("C", "±", "%") -> MaterialTheme.colorScheme.outlineVariant
                    else -> MaterialTheme.colorScheme.surfaceVariant
                  }
                )
                .clickable {
                  when (btn) {
                    "C" -> {
                      displayText = "0"
                      pendingExpression = ""
                    }
                    "=" -> {
                      // Check secret PIN condition
                      onEvaluateSecret(displayText)
                      displayText = try {
                        val num = displayText.toDoubleOrNull() ?: 0.0
                        if (num == 1234.0) "UNLOCKED" else String.format("%.0f", num)
                      } catch (e: Exception) {
                        "Error"
                      }
                    }
                    else -> {
                      if (displayText == "0" && btn != ".") {
                        displayText = btn
                      } else {
                        displayText += btn
                      }
                    }
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = btn,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEquals) Color.Black else Color.White
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun VaultContentsView(
  items: List<MediaItem>,
  isDecoy: Boolean,
  onMediaClick: (MediaItem) -> Unit,
  onUnhide: (MediaItem) -> Unit,
  onDeletePermanent: (MediaItem) -> Unit
) {
  var selectedItemForAction by remember { mutableStateOf<MediaItem?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    if (isDecoy) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Text(
          text = "Decoy Vault Mode Active • Innocuous Alternate Space",
          color = Color(0xFFF59E0B),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    if (items.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = CyberCyan.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "Safe Vault is Empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(
            text = "Select photos or videos in your gallery and tap 'Safe Vault' to hide them securely.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(items, key = { it.id }) { item ->
          MediaThumbnailCard(
            item = item,
            isSelected = false,
            isSelectionMode = false,
            onClick = { onMediaClick(item) },
            onLongClick = { selectedItemForAction = item }
          )
        }
      }
    }

    // Action dialog for selected vault item
    selectedItemForAction?.let { item ->
      AlertDialog(
        onDismissRequest = { selectedItemForAction = null },
        title = { Text(item.title) },
        text = { Text("Choose an action for this secure item:") },
        confirmButton = {
          TextButton(
            onClick = {
              onUnhide(item)
              selectedItemForAction = null
            }
          ) {
            Icon(Icons.Default.Restore, contentDescription = null, tint = CyberCyan)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Unhide to Gallery", color = CyberCyan)
          }
        },
        dismissButton = {
          TextButton(
            onClick = {
              onDeletePermanent(item)
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
fun VaultSettingsDialog(
  securityManager: VaultSecurityManager,
  onDismiss: () -> Unit,
  onSettingsUpdated: () -> Unit
) {
  var newPin by remember { mutableStateOf("") }
  var newDecoyPin by remember { mutableStateOf("") }
  var isCalculatorEnabled by remember { mutableStateOf(securityManager.isCalculatorDisguiseEnabled()) }
  var isBiometricEnabled by remember { mutableStateOf(securityManager.isBiometricEnabled()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Vault Security Settings", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Disguised Calculator Mode Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Calculator Disguise", fontWeight = FontWeight.Medium)
            Text("Opens vault as working calculator", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Switch(
            checked = isCalculatorEnabled,
            onCheckedChange = {
              isCalculatorEnabled = it
              securityManager.setCalculatorDisguiseEnabled(it)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
          )
        }

        // Biometric Unlock Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Biometric Authentication", fontWeight = FontWeight.Medium)
            Text("Fingerprint / Face Unlock", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Switch(
            checked = isBiometricEnabled,
            onCheckedChange = {
              isBiometricEnabled = it
              securityManager.setBiometricEnabled(it)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
          )
        }

        // Change Master PIN
        OutlinedTextField(
          value = newPin,
          onValueChange = { newPin = it },
          label = { Text("Set/Change Master PIN") },
          placeholder = { Text("Enter 4 digits (e.g. 5678)") },
          singleLine = true,
          shape = RoundedCornerShape(8.dp)
        )

        // Set Decoy PIN
        OutlinedTextField(
          value = newDecoyPin,
          onValueChange = { newDecoyPin = it },
          label = { Text("Configure Decoy PIN") },
          placeholder = { Text("Enter decoy PIN (e.g. 0000)") },
          singleLine = true,
          shape = RoundedCornerShape(8.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (newPin.isNotBlank()) {
            securityManager.saveNewMasterPin(newPin)
          }
          if (newDecoyPin.isNotBlank()) {
            securityManager.setDecoySecret(newDecoyPin)
          }
          onSettingsUpdated()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
      ) {
        Text("Save Settings")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
