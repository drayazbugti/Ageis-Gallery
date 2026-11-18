package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.MediaItem
import com.example.service.VideoPlaybackManager
import com.example.service.VideoRepeatMode
import com.example.ui.components.VideoPlayerMenuBottomSheet
import com.example.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoPlayerScreen(
  item: MediaItem,
  viewModel: GalleryViewModel,
  onBack: () -> Unit,
  onOpenVault: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
  val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
  val currentSystemVol = remember { audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() }

  // Shared playback states
  val isLocked by VideoPlaybackManager.isControlsLocked.collectAsState()
  val repeatMode by VideoPlaybackManager.repeatMode.collectAsState()
  val playbackSpeed by VideoPlaybackManager.playbackSpeed.collectAsState()
  val isAudioOnly by VideoPlaybackManager.isAudioOnlyMode.collectAsState()
  val isBgPlayback by VideoPlaybackManager.isBackgroundPlaybackEnabled.collectAsState()

  // Local UI state
  var isPlaying by remember { mutableStateOf(false) }
  var isBuffering by remember { mutableStateOf(true) }
  var playbackError by remember { mutableStateOf<String?>(null) }
  var reloadTrigger by remember { mutableIntStateOf(0) }
  var currentPosition by remember { mutableLongStateOf(0L) }
  var duration by remember { mutableLongStateOf(item.durationMs.coerceAtLeast(1000L)) }
  var isControlsVisible by remember { mutableStateOf(true) }
  var isMenuOpen by remember { mutableStateOf(false) }
  var isBottomPopupOpen by remember { mutableStateOf(false) }
  var isSpeedPanelVisible by remember { mutableStateOf(false) }
  var isInfoDialogOpen by remember { mutableStateOf(false) }

  // Gesture HUD live feedback
  var gestureText by remember { mutableStateOf<String?>(null) }
  var gestureIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
  var capturedFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var frameCaptureToast by remember { mutableStateOf(false) }

  // Hardware controls
  var volumeFraction by remember { mutableFloatStateOf((currentSystemVol / maxVolume.coerceAtLeast(1f)).coerceIn(0f, 1f)) }
  var brightnessFraction by remember { mutableFloatStateOf(0.85f) }

  // Zoom & Drag states (Double-Tap 2.5x / Pinch 1x..4x / Pan while zoomed)
  var scale by remember(item.id) { mutableFloatStateOf(1f) }
  var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
  val animatedScale by animateFloatAsState(targetValue = scale, label = "videoZoomScale")
  var videoAspectRatio by remember(item.id) { mutableFloatStateOf(16f / 9f) }

  val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
    if (!isLocked && !isAudioOnly) {
      val nextScale = (scale * zoomChange).coerceIn(1f, 4f)
      scale = nextScale
      if (nextScale > 1.01f) {
        offset += offsetChange
      } else {
        offset = Offset.Zero
      }
    }
  }

  // Theme palette aligned with Nova Gallery Video Player (#0b0f19 dark cinema + #ff3b30 accent)
  val cinemaBg = Color(0xFF0B0F19)
  val crimsonAccent = Color(0xFFFF3B30)

  // Back navigation handling
  BackHandler {
    if (isSpeedPanelVisible) {
      isSpeedPanelVisible = false
    } else if (isLocked) {
      Toast.makeText(context, "Tap the unlock button to exit", Toast.LENGTH_SHORT).show()
    } else {
      if (!isBgPlayback) {
        VideoPlaybackManager.pause()
        VideoPlaybackManager.stopBackgroundService()
      }
      onBack()
    }
  }

  // Register active item with VideoPlaybackManager & reset zoom
  LaunchedEffect(item.id) {
    VideoPlaybackManager.setActiveMedia(item)
    currentPosition = 0L
    duration = item.durationMs.coerceAtLeast(1000L)
    scale = 1f
    offset = Offset.Zero
    videoAspectRatio = 16f / 9f
  }

  // Auto-hide controls timer (4s)
  LaunchedEffect(isControlsVisible, isPlaying, isLocked, isSpeedPanelVisible) {
    if (isControlsVisible && isPlaying && !isLocked && !isSpeedPanelVisible) {
      delay(4000)
      isControlsVisible = false
    }
  }

  // Screen brightness adjustment
  LaunchedEffect(brightnessFraction) {
    val activity = context as? Activity
    activity?.let {
      val layoutParams = it.window.attributes
      layoutParams.screenBrightness = brightnessFraction.coerceIn(0.05f, 1.0f)
      it.window.attributes = layoutParams
    }
  }

  // Frame captured banner dismiss timer
  LaunchedEffect(frameCaptureToast) {
    if (frameCaptureToast) {
      delay(3000)
      frameCaptureToast = false
      capturedFrameBitmap = null
    }
  }

  // Gesture feedback dismiss timer
  LaunchedEffect(gestureText) {
    if (gestureText != null) {
      delay(1000)
      gestureText = null
      gestureIcon = null
    }
  }

  // Clean up on exit
  DisposableEffect(item.uri, reloadTrigger) {
    onDispose {
      if (!VideoPlaybackManager.isBackgroundPlaybackEnabled.value) {
        try {
          VideoPlaybackManager.currentPlayer?.stop()
          VideoPlaybackManager.currentPlayer?.release()
          VideoPlaybackManager.currentPlayer = null
        } catch (_: Exception) {}
      }
    }
  }

  val enterPipAction: () -> Unit = {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val activity = context as? Activity
      activity?.let {
        try {
          val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
          it.enterPictureInPictureMode(params)
        } catch (e: Exception) {
          Toast.makeText(context, "Minimize / PiP mode active", Toast.LENGTH_SHORT).show()
        }
      }
    } else {
      Toast.makeText(context, "Pop-Up player requires Android 8.0+", Toast.LENGTH_SHORT).show()
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(cinemaBg)
      .clipToBounds()
  ) {
    val containerRatio = (constraints.maxWidth.toFloat() / constraints.maxHeight.coerceAtLeast(1).toFloat())
      .coerceAtLeast(0.1f)
    val videoFitModifier = if (videoAspectRatio > containerRatio) {
      Modifier.fillMaxWidth().aspectRatio(videoAspectRatio)
    } else {
      Modifier.fillMaxHeight().aspectRatio(videoAspectRatio)
    }

    // 1. Zoomable & Draggable TextureView Video Canvas
    if (!isAudioOnly) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clipToBounds()
          .graphicsLayer(
            scaleX = animatedScale,
            scaleY = animatedScale,
            translationX = offset.x,
            translationY = offset.y
          ),
        contentAlignment = Alignment.Center
      ) {
        key(item.uri, reloadTrigger) {
          AndroidView(
            factory = { ctx ->
              TextureView(ctx).apply {
                var activeSurface: Surface? = null
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                  override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                    try {
                      isBuffering = true
                      playbackError = null
                      activeSurface?.release()
                      val surface = Surface(surfaceTexture)
                      activeSurface = surface

                      val player = MediaPlayer().apply {
                        setSurface(surface)
                        val uriStr = item.uri
                        val localFile = File(uriStr)
                        if (localFile.exists() && localFile.length() > 0) {
                          setDataSource(localFile.absolutePath)
                        } else if (uriStr.startsWith("content://")) {
                          try {
                            val pfd = ctx.contentResolver.openFileDescriptor(Uri.parse(uriStr), "r")
                            if (pfd != null) {
                              setDataSource(pfd.fileDescriptor)
                              pfd.close()
                            } else {
                              setDataSource(ctx, Uri.parse(uriStr))
                            }
                          } catch (_: Exception) {
                            setDataSource(ctx, Uri.parse(uriStr))
                          }
                        } else {
                          setDataSource(ctx, Uri.parse(uriStr))
                        }

                        isLooping = (VideoPlaybackManager.repeatMode.value == VideoRepeatMode.ONE)

                        setOnVideoSizeChangedListener { _, vw, vh ->
                          if (vw > 0 && vh > 0) {
                            videoAspectRatio = vw.toFloat() / vh.toFloat()
                          }
                        }

                        setOnPreparedListener { mp ->
                          isBuffering = false
                          playbackError = null
                          if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                            videoAspectRatio = mp.videoWidth.toFloat() / mp.videoHeight.toFloat()
                          }
                          if (mp.duration > 0) {
                            duration = mp.duration.toLong()
                          }
                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && playbackSpeed != 1.0f) {
                            try {
                              val params = mp.playbackParams ?: android.media.PlaybackParams()
                              params.speed = playbackSpeed
                              mp.playbackParams = params
                            } catch (_: Exception) {}
                          }
                          mp.start()
                          isPlaying = true
                          VideoPlaybackManager.updatePlaybackState(true, mp.currentPosition.toLong(), duration)
                        }

                        setOnCompletionListener {
                          if (VideoPlaybackManager.repeatMode.value == VideoRepeatMode.ALL) {
                            it.seekTo(0)
                            it.start()
                            isPlaying = true
                          } else if (VideoPlaybackManager.repeatMode.value == VideoRepeatMode.ONE) {
                            // Loop handled by isLooping
                          } else {
                            isPlaying = false
                            currentPosition = duration
                            VideoPlaybackManager.updatePlaybackState(false, duration, duration)
                          }
                        }

                        setOnErrorListener { _, what, extra ->
                          isBuffering = false
                          playbackError = "Unable to play video ($what:$extra)"
                          true
                        }

                        prepareAsync()
                      }
                      VideoPlaybackManager.currentPlayer = player
                    } catch (e: Exception) {
                      isBuffering = false
                      playbackError = e.localizedMessage ?: "Playback initialization failed"
                    }
                  }

                  override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                  override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    if (!VideoPlaybackManager.isBackgroundPlaybackEnabled.value) {
                      try {
                        VideoPlaybackManager.currentPlayer?.release()
                        VideoPlaybackManager.currentPlayer = null
                      } catch (_: Exception) {}
                    } else {
                      try {
                        VideoPlaybackManager.currentPlayer?.setSurface(null)
                      } catch (_: Exception) {}
                    }
                    activeSurface?.release()
                    activeSurface = null
                    return true
                  }

                  override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
              }
            },
            modifier = videoFitModifier
          )
        }
      }
    } else {
      // Audio-Only Listening Mode Screen
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(cinemaBg),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(24.dp)
        ) {
          Box(
            modifier = Modifier
              .size(120.dp)
              .background(crimsonAccent.copy(alpha = 0.15f), CircleShape)
              .border(2.dp, crimsonAccent, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Headphones,
              contentDescription = null,
              tint = crimsonAccent,
              modifier = Modifier.size(56.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))
          Text(
            text = item.title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Playing in Audio-Only Mode",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // Playback Polling Loop
    LaunchedEffect(isPlaying) {
      while (isPlaying) {
        VideoPlaybackManager.currentPlayer?.let { player ->
          try {
            if (player.isPlaying) {
              val pos = player.currentPosition.toLong()
              currentPosition = pos
              if (player.duration > 0) {
                duration = player.duration.toLong()
              }
              VideoPlaybackManager.updatePlaybackState(true, pos, duration)
            }
          } catch (_: Exception) {}
        }
        delay(300)
      }
    }

    // 2. Full-Screen Gestures Detector Overlay (Tap/Double-Tap Zoom, Pinch Zoom, Drag Pan, Seek, Vol/Bright)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(isLocked, isAudioOnly) {
          if (isLocked) {
            detectTapGestures(
              onTap = {
                Toast.makeText(context, "Controls locked! Tap unlock button to restore", Toast.LENGTH_SHORT).show()
              }
            )
          } else {
            detectTapGestures(
              onDoubleTap = { tapOffset ->
                val halfWidth = size.width / 2f
                if (tapOffset.x < halfWidth * 0.6f) {
                  VideoPlaybackManager.seekDelta(-10_000L)
                  gestureText = "Seek Backward ⏪ -10s"
                  gestureIcon = Icons.Default.FastRewind
                } else if (tapOffset.x > halfWidth * 1.4f) {
                  VideoPlaybackManager.seekDelta(10_000L)
                  gestureText = "Seek Forward ⏩ +10s"
                  gestureIcon = Icons.Default.FastForward
                } else if (!isAudioOnly) {
                  if (scale > 1.05f) {
                    scale = 1f
                    offset = Offset.Zero
                    gestureText = "Zoom Reset"
                    gestureIcon = Icons.Default.ZoomOut
                  } else {
                    scale = 2.5f
                    gestureText = "Zoomed In (2.5x)"
                    gestureIcon = Icons.Default.ZoomIn
                  }
                } else {
                  VideoPlaybackManager.togglePlayPause()
                  isPlaying = VideoPlaybackManager.isPlaying.value
                }
              },
              onTap = {
                if (isSpeedPanelVisible) {
                  isSpeedPanelVisible = false
                } else {
                  isControlsVisible = !isControlsVisible
                }
              }
            )
          }
        }
        .pointerInput(isLocked, scale, isAudioOnly) {
          if (!isLocked) {
            var dragStartX = 0f
            var dragStartY = 0f
            var accumulatedDx = 0f
            var accumulatedDy = 0f
            var isDismissSwipe = false
            var isEdgeSwipe = false

            detectDragGestures(
              onDragStart = { startOffset ->
                dragStartX = startOffset.x
                dragStartY = startOffset.y
                accumulatedDx = 0f
                accumulatedDy = 0f
                isDismissSwipe = false
                isEdgeSwipe = false
              },
              onDragEnd = {
                if (scale <= 1.05f) {
                  if (isDismissSwipe && accumulatedDy > 110f) {
                    if (!isBgPlayback) {
                      VideoPlaybackManager.pause()
                      VideoPlaybackManager.stopBackgroundService()
                    }
                    onBack()
                  } else if (isEdgeSwipe) {
                    if (dragStartX < size.width * 0.2f && accumulatedDx > 50f) {
                      viewModel.navigateToPreviousMedia()
                    } else if (dragStartX > size.width * 0.8f && accumulatedDx < -50f) {
                      viewModel.navigateToNextMedia()
                    }
                  }
                }
                isDismissSwipe = false
                isEdgeSwipe = false
              },
              onDragCancel = {
                isDismissSwipe = false
                isEdgeSwipe = false
              },
              onDrag = { change, dragAmount ->
                if (scale > 1.05f && !isAudioOnly) {
                  // Drag/Pan around zoomed video smoothly
                  change.consume()
                  val maxPanX = (size.width * (scale - 1f)) / 2f
                  val maxPanY = (size.height * (scale - 1f)) / 2f
                  offset = Offset(
                    x = (offset.x + dragAmount.x).coerceIn(-maxPanX, maxPanX),
                    y = (offset.y + dragAmount.y).coerceIn(-maxPanY, maxPanY)
                  )
                } else {
                  change.consume()
                  accumulatedDx += dragAmount.x
                  accumulatedDy += dragAmount.y

                  val isHorizontal = kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)

                  if (!isDismissSwipe && !isEdgeSwipe) {
                    // Middle vertical swipe down -> close video
                    val isMiddleX = dragStartX > size.width * 0.25f && dragStartX < size.width * 0.75f
                    if (!isHorizontal && dragAmount.y > 0 && accumulatedDy > 24f && isMiddleX) {
                      isDismissSwipe = true
                    } else if (isHorizontal && kotlin.math.abs(accumulatedDx) > 25f && (dragStartX < size.width * 0.16f || dragStartX > size.width * 0.84f)) {
                      isEdgeSwipe = true
                    }
                  }

                  if (isDismissSwipe) {
                    gestureText = "Closing Video..."
                    gestureIcon = Icons.AutoMirrored.Filled.ArrowBack
                  } else if (isEdgeSwipe) {
                    val isPrev = dragStartX < size.width * 0.2f
                    gestureText = if (isPrev) "Previous Video" else "Next Video"
                    gestureIcon = if (isPrev) Icons.Default.SkipPrevious else Icons.Default.SkipNext
                  } else if (isHorizontal) {
                    // Horizontal swipe anywhere: Seek video forward/backward
                    val deltaSec = (dragAmount.x / 8).toLong()
                    val deltaMs = deltaSec * 1000L
                    VideoPlaybackManager.seekDelta(deltaMs)
                    currentPosition = VideoPlaybackManager.currentPosition.value
                    gestureText = if (dragAmount.x > 0) {
                      "Seek Forward ⏩ (${formatTime(currentPosition)})"
                    } else {
                      "Seek Backward ⏪ (${formatTime(currentPosition)})"
                    }
                    gestureIcon = if (dragAmount.x > 0) Icons.Default.FastForward else Icons.Default.FastRewind
                  } else {
                    // Left side vertical swipe -> Volume control; Right side vertical swipe -> Brightness control
                    val isLeftHalf = dragStartX < (size.width / 2f)
                    val deltaFraction = -dragAmount.y / size.height

                    if (isLeftHalf) {
                      volumeFraction = (volumeFraction + deltaFraction).coerceIn(0f, 1f)
                      val streamVol = (volumeFraction * maxVolume).toInt()
                      audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0)
                      VideoPlaybackManager.currentPlayer?.setVolume(volumeFraction, volumeFraction)
                      val percent = (volumeFraction * 100).toInt()
                      gestureText = "Volume Control: $percent%"
                      gestureIcon = if (volumeFraction > 0.5f) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeDown
                    } else {
                      brightnessFraction = (brightnessFraction + deltaFraction).coerceIn(0.05f, 1.0f)
                      val percent = (brightnessFraction * 100).toInt()
                      gestureText = "Brightness Control: $percent%"
                      gestureIcon = Icons.Default.BrightnessMedium
                    }
                  }
                }
              }
            )
          }
        }
        .transformable(state = transformState, enabled = !isLocked && !isAudioOnly)
    )

    // 3. Gesture HUD Feedback Center Pill (.gesture-indicator)
    if (gestureText != null) {
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.65f))
          .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (gestureIcon != null) {
            Icon(gestureIcon!!, contentDescription = null, tint = crimsonAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(text = gestureText!!, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 4. Buffering Indicator
    if (isBuffering && playbackError == null) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = crimsonAccent, modifier = Modifier.size(48.dp))
      }
    }

    // 5. Playback Error with Retry Button
    if (playbackError != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Video Playback Issue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
          Spacer(modifier = Modifier.height(6.dp))
          Text(playbackError ?: "Format unsupported", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = {
              playbackError = null
              reloadTrigger++
            },
            colors = ButtonDefaults.buttonColors(containerColor = crimsonAccent)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Retry Video")
          }
        }
      }
    }

    // 6. Screenshot / Frame Captured Notification Banner
    AnimatedVisibility(
      visible = frameCaptureToast,
      enter = slideInVertically(initialOffsetY = { -it }),
      exit = slideOutVertically(targetOffsetY = { -it }),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(top = 64.dp)
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (capturedFrameBitmap != null) {
            Image(
              bitmap = capturedFrameBitmap!!.asImageBitmap(),
              contentDescription = "Captured Frame",
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
          }
          Column {
            Text(text = "Frame Grabbed!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111111))
            Text(text = "Captured at ${formatTime(currentPosition)}", fontSize = 11.sp, color = Color(0xFF666666))
          }
        }
      }
    }

    // 7. Floating Unlock Button (When controls are locked)
    if (isLocked) {
      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .statusBarsPadding()
          .padding(20.dp)
      ) {
        Button(
          onClick = {
            VideoPlaybackManager.setControlsLocked(false)
            Toast.makeText(context, "Controls unlocked", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500), contentColor = Color.White),
          shape = RoundedCornerShape(24.dp)
        ) {
          Icon(Icons.Default.LockOpen, contentDescription = "Unlock", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Unlock Controls")
        }
      }
    }

    // 8. Top Bar (.top-bar): Back button, Title, Minimize badge, Speed chip, Top-Right Three-Dot Menu
    AnimatedVisibility(
      visible = isControlsVisible && !isLocked,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.TopCenter)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)
            )
          )
          .statusBarsPadding()
          .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left: Back button + Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          IconButton(
            onClick = {
              if (!isBgPlayback) {
                VideoPlaybackManager.pause()
                VideoPlaybackManager.stopBackgroundService()
              }
              onBack()
            },
            modifier = Modifier.testTag("video_back_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Text(
            text = item.title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
        }

        // Right: Minimize badge, Speed pill, Favorite, Top-Right Three-Dot Menu
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Minimize Badge
          Surface(
            color = Color.Black.copy(alpha = 0.35f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
            modifier = Modifier
              .clickable { enterPipAction() }
              .testTag("video_minimize_badge")
          ) {
            Text(
              text = "Minimize",
              color = Color.White,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          // Speed Overlay Toggle Pill
          Surface(
            color = if (isSpeedPanelVisible) crimsonAccent else Color.Black.copy(alpha = 0.35f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, if (isSpeedPanelVisible) crimsonAccent else Color.White.copy(alpha = 0.4f)),
            modifier = Modifier
              .clickable { isSpeedPanelVisible = !isSpeedPanelVisible }
              .testTag("video_speed_pill")
          ) {
            Text(
              text = if (playbackSpeed % 1f == 0f) "${playbackSpeed.toInt()}x" else "${playbackSpeed}x",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          // Video Details & Custom Tags Button
          IconButton(
            onClick = { isInfoDialogOpen = true },
            modifier = Modifier
              .size(36.dp)
              .testTag("video_info_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Video Details & Tags",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          // Top-Right Three-Dot Menu
          IconButton(
            onClick = { isMenuOpen = true },
            modifier = Modifier
              .size(40.dp)
              .testTag("video_three_dots_menu_btn")
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Options Menu",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }

    // Floating Reset Zoom Pill when zoomed in
    if (scale > 1.05f && !isLocked && !isAudioOnly) {
      Surface(
        color = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .statusBarsPadding()
          .padding(top = 68.dp, end = 16.dp)
          .clickable {
            scale = 1f
            offset = Offset.Zero
            gestureText = "Zoom Reset"
            gestureIcon = Icons.Default.ZoomOut
          }
          .testTag("video_reset_zoom_pill")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ZoomOut,
            contentDescription = "Reset Zoom",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${String.format(Locale.US, "%.1f", scale)}x • Reset",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // 9. Speed Adjustment Overlay Panel (.speed-panel)
    AnimatedVisibility(
      visible = isSpeedPanelVisible && !isLocked,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(top = 86.dp, start = 20.dp, end = 20.dp)
    ) {
      Surface(
        color = Color(0xFF14141E).copy(alpha = 0.90f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 380.dp)
          .testTag("video_speed_panel")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Playback speed",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Normal
            )
            IconButton(
              onClick = { isSpeedPanelVisible = false },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close Speed Panel", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "${String.format(Locale.US, "%.2f", playbackSpeed).trimEnd('0').trimEnd('.').let { if (!it.contains('.')) "$it.0" else it }}x",
            color = crimsonAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(6.dp))

          Slider(
            value = playbackSpeed,
            onValueChange = { newVal ->
              val snapped = (kotlin.math.round(newVal * 20f) / 20f).coerceIn(0.25f, 2.0f)
              VideoPlaybackManager.setPlaybackSpeed(snapped)
            },
            valueRange = 0.25f..2.0f,
            colors = SliderDefaults.colors(
              thumbColor = crimsonAccent,
              activeTrackColor = crimsonAccent,
              inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("video_speed_slider")
          )

          Spacer(modifier = Modifier.height(10.dp))

          val speedPresets = listOf(
            listOf(0.25f, 0.5f, 0.75f, 1.0f),
            listOf(1.25f, 1.5f, 1.75f, 2.0f)
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            speedPresets.forEach { rowPresets ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                rowPresets.forEach { preset ->
                  val isActive = kotlin.math.abs(playbackSpeed - preset) < 0.03f
                  val label = if (preset == 1.0f) "1x" else if (preset == 2.0f) "2x" else "${preset}x"
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (isActive) crimsonAccent else Color.White.copy(alpha = 0.1f))
                      .clickable { VideoPlaybackManager.setPlaybackSpeed(preset) }
                      .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      color = Color.White,
                      fontSize = 14.sp,
                      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 10. Bottom Controls (.bottom-controls): Timeline & Control Buttons Row
    AnimatedVisibility(
      visible = isControlsVisible && !isLocked,
      enter = slideInVertically(initialOffsetY = { it }),
      exit = slideOutVertically(targetOffsetY = { it }),
      modifier = Modifier.align(Alignment.BottomCenter)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
            )
          )
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 16.dp)
          .testTag("video_control_bar")
      ) {
        // Timeline Scrubber with Red #ff3b30 progress & thumb
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = formatTime(currentPosition),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )

          Slider(
            value = if (duration > 0) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f,
            onValueChange = { frac ->
              val newPos = (frac * duration).toLong()
              currentPosition = newPos
              VideoPlaybackManager.seekTo(newPos)
            },
            colors = SliderDefaults.colors(
              thumbColor = crimsonAccent,
              activeTrackColor = crimsonAccent,
              inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 10.dp)
              .testTag("video_progress_timeline")
          )

          Text(
            text = formatTime(duration),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control Buttons Row: Queue/Screenshot, Prev, Play/Pause, Next, Repeat/Shuffle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left: Frame Grabber / Queue Options
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { isSpeedPanelVisible = !isSpeedPanelVisible },
              modifier = Modifier.testTag("btn_toggle_speed_overlay")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                contentDescription = "Playback Options",
                tint = if (isSpeedPanelVisible) crimsonAccent else Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            IconButton(
              onClick = {
                coroutineScope.launch {
                  val bmp = VideoPlaybackManager.captureVideoFrame(context, item, currentPosition)
                  capturedFrameBitmap = bmp
                  frameCaptureToast = true
                }
              },
              modifier = Modifier.testTag("btn_frame_grabber")
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture Frame",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Prev Video
          IconButton(
            onClick = { viewModel.navigateToPreviousMedia() },
            modifier = Modifier.testTag("btn_video_prev")
          ) {
            Icon(
              imageVector = Icons.Default.SkipPrevious,
              contentDescription = "Previous Video",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          // Play / Pause Circular Button (.play-pause-btn)
          IconButton(
            onClick = {
              VideoPlaybackManager.togglePlayPause()
              isPlaying = VideoPlaybackManager.isPlaying.value
            },
            modifier = Modifier
              .size(48.dp)
              .background(Color.White.copy(alpha = 0.22f), CircleShape)
              .testTag("video_center_play_pause")
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.White,
              modifier = Modifier.size(26.dp)
            )
          }

          // Next Video
          IconButton(
            onClick = { viewModel.navigateToNextMedia() },
            modifier = Modifier.testTag("btn_video_next")
          ) {
            Icon(
              imageVector = Icons.Default.SkipNext,
              contentDescription = "Next Video",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          // Right: Repeat / Loop & Favorite
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                val newMode = VideoPlaybackManager.toggleRepeatMode()
                val label = when (newMode) {
                  VideoRepeatMode.OFF -> "Repeat Off"
                  VideoRepeatMode.ONE -> "Repeat One"
                  VideoRepeatMode.ALL -> "Repeat All"
                }
                Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.testTag("btn_repeat_loop")
            ) {
              Icon(
                imageVector = when (repeatMode) {
                  VideoRepeatMode.ONE -> Icons.Default.RepeatOne
                  VideoRepeatMode.ALL -> Icons.Default.Repeat
                  VideoRepeatMode.OFF -> Icons.Default.Shuffle
                },
                contentDescription = "Repeat / Loop",
                tint = if (repeatMode != VideoRepeatMode.OFF) crimsonAccent else Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(onClick = { viewModel.toggleFavorite(item) }) {
              Icon(
                imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (item.isFavorite) crimsonAccent else Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            // Bottom Three-Dots Popup Menu (.popup-menu)
            Box {
              IconButton(
                onClick = { isBottomPopupOpen = !isBottomPopupOpen },
                modifier = Modifier.testTag("video_bottom_popup_menu_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More Video Options",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              DropdownMenu(
                expanded = isBottomPopupOpen,
                onDismissRequest = { isBottomPopupOpen = false },
                modifier = Modifier.background(Color(0xFF1E293B))
              ) {
                DropdownMenuItem(
                  text = { Text("Details & Tags", color = Color(0xFFF1F5F9)) },
                  leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8)) },
                  onClick = {
                    isBottomPopupOpen = false
                    isInfoDialogOpen = true
                  }
                )
                DropdownMenuItem(
                  text = { Text("Share Video", color = Color(0xFFF1F5F9)) },
                  leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF38BDF8)) },
                  onClick = {
                    isBottomPopupOpen = false
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                      type = "video/*"
                      putExtra(android.content.Intent.EXTRA_TEXT, "Shared from Nova Gallery: ${item.title}")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Video"))
                  }
                )
                DropdownMenuItem(
                  text = { Text("Move to Vault", color = Color(0xFFF1F5F9)) },
                  leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF59E0B)) },
                  onClick = {
                    isBottomPopupOpen = false
                    viewModel.moveToVault(item.id)
                    Toast.makeText(context, "Moved to Safe Vault", Toast.LENGTH_SHORT).show()
                    onOpenVault()
                  }
                )
                DropdownMenuItem(
                  text = { Text("Delete Permanently", color = Color(0xFFEF4444)) },
                  leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                  onClick = {
                    isBottomPopupOpen = false
                    viewModel.deleteMediaPermanently(item)
                    Toast.makeText(context, "Deleted permanently", Toast.LENGTH_SHORT).show()
                    onBack()
                  }
                )
              }
            }
          }
        }
      }
    }

    // 11. Clean Solid White Popup Menu Bottom Sheet (opened from Top-Right 3-dot icon)
    if (isMenuOpen) {
      VideoPlayerMenuBottomSheet(
        item = item,
        onDismiss = { isMenuOpen = false },
        onEnterPip = enterPipAction,
        onSaveToPlaylist = {
          viewModel.toggleFavorite(item)
          Toast.makeText(context, "Saved to Favorites Playlist", Toast.LENGTH_SHORT).show()
        },
        onAddTag = { tag -> viewModel.addCustomTag(item, tag) },
        onRemoveTag = { tag -> viewModel.removeCustomTag(item, tag) }
      )
    }

    // 12. Video Details Info Dialog with Custom Tags
    if (isInfoDialogOpen) {
      val fullDateFormatted = remember(item.dateAdded) {
        SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(item.dateAdded))
      }

      AlertDialog(
        onDismissRequest = { isInfoDialogOpen = false },
        title = { Text(text = "Video Details", fontWeight = FontWeight.Bold) },
        text = {
          Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            DetailItem(label = "Title", value = item.title)
            DetailItem(label = "Date & Time", value = fullDateFormatted)
            DetailItem(label = "Duration", value = item.formattedDuration)
            DetailItem(label = "Resolution", value = item.resolution)
            DetailItem(label = "File Size", value = item.formattedSize)
            DetailItem(label = "Album", value = item.album)
            DetailItem(label = "Storage Path", value = item.uri)
            Spacer(modifier = Modifier.height(8.dp))
            MediaTagsInfoSection(item = item, viewModel = viewModel)
          }
        },
        confirmButton = {
          TextButton(onClick = { isInfoDialogOpen = false }) {
            Text("Close", color = crimsonAccent)
          }
        }
      )
    }
  }
}

private fun formatTime(millis: Long): String {
  val totalSeconds = (millis / 1000).coerceAtLeast(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return String.format("%02d:%02d", minutes, seconds)
}
