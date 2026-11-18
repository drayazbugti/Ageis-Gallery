package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class VideoRepeatMode {
  OFF,
  ONE,
  ALL
}

object VideoPlaybackManager {
  private val scope = CoroutineScope(Dispatchers.Main + Job())
  private var sleepTimerJob: Job? = null

  private val _activeItem = MutableStateFlow<MediaItem?>(null)
  val activeItem: StateFlow<MediaItem?> = _activeItem.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _isBackgroundPlaybackEnabled = MutableStateFlow(true)
  val isBackgroundPlaybackEnabled: StateFlow<Boolean> = _isBackgroundPlaybackEnabled.asStateFlow()

  private val _currentPosition = MutableStateFlow(0L)
  val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

  private val _duration = MutableStateFlow(1000L)
  val duration: StateFlow<Long> = _duration.asStateFlow()

  private val _playbackSpeed = MutableStateFlow(1.0f)
  val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

  private val _repeatMode = MutableStateFlow(VideoRepeatMode.OFF)
  val repeatMode: StateFlow<VideoRepeatMode> = _repeatMode.asStateFlow()

  private val _isAudioOnlyMode = MutableStateFlow(false)
  val isAudioOnlyMode: StateFlow<Boolean> = _isAudioOnlyMode.asStateFlow()

  private val _isControlsLocked = MutableStateFlow(false)
  val isControlsLocked: StateFlow<Boolean> = _isControlsLocked.asStateFlow()

  private val _pointA = MutableStateFlow<Long?>(null)
  val pointA: StateFlow<Long?> = _pointA.asStateFlow()

  private val _pointB = MutableStateFlow<Long?>(null)
  val pointB: StateFlow<Long?> = _pointB.asStateFlow()

  private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
  val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

  private val _sleepTimerSecondsLeft = MutableStateFlow<Int?>(null)
  val sleepTimerSecondsLeft: StateFlow<Int?> = _sleepTimerSecondsLeft.asStateFlow()

  private val _bookmarks = MutableStateFlow<List<Long>>(emptyList())
  val bookmarks: StateFlow<List<Long>> = _bookmarks.asStateFlow()

  private val _equalizerPreset = MutableStateFlow("Flat")
  val equalizerPreset: StateFlow<String> = _equalizerPreset.asStateFlow()

  private val _isPlayerThemeWhite = MutableStateFlow(false)
  val isPlayerThemeWhite: StateFlow<Boolean> = _isPlayerThemeWhite.asStateFlow()

  var currentPlayer: MediaPlayer? = null
  private var appContext: Context? = null

  fun initialize(context: Context) {
    appContext = context.applicationContext
    val prefs = context.getSharedPreferences("video_player_prefs", Context.MODE_PRIVATE)
    _isBackgroundPlaybackEnabled.value = prefs.getBoolean("bg_playback_enabled", true)
  }

  fun setBackgroundPlaybackEnabled(enabled: Boolean) {
    _isBackgroundPlaybackEnabled.value = enabled
    appContext?.let { ctx ->
      val prefs = ctx.getSharedPreferences("video_player_prefs", Context.MODE_PRIVATE)
      prefs.edit().putBoolean("bg_playback_enabled", enabled).apply()
    }
    if (!enabled && !isAppInForeground) {
      pause()
      stopBackgroundService()
    }
  }

  var isAppInForeground: Boolean = true

  fun togglePlayerTheme() {
    _isPlayerThemeWhite.value = !_isPlayerThemeWhite.value
  }

  fun setActiveMedia(item: MediaItem?) {
    _activeItem.value = item
    _bookmarks.value = emptyList()
    _pointA.value = null
    _pointB.value = null
    _isAudioOnlyMode.value = false
    _isControlsLocked.value = false
  }

  fun updatePlaybackState(playing: Boolean, pos: Long, dur: Long) {
    _isPlaying.value = playing
    _currentPosition.value = pos
    if (dur > 0) _duration.value = dur

    // Check A-B repeat
    val pa = _pointA.value
    val pb = _pointB.value
    if (pa != null && pb != null && pb > pa && pos >= pb) {
      seekTo(pa)
    }
  }

  fun setPlaybackSpeed(speed: Float) {
    _playbackSpeed.value = speed
    currentPlayer?.let { player ->
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          val params = player.playbackParams ?: PlaybackParams()
          params.speed = speed
          player.playbackParams = params
        }
      } catch (_: Exception) {}
    }
  }

  fun toggleRepeatMode(): VideoRepeatMode {
    val next = when (_repeatMode.value) {
      VideoRepeatMode.OFF -> VideoRepeatMode.ONE
      VideoRepeatMode.ONE -> VideoRepeatMode.ALL
      VideoRepeatMode.ALL -> VideoRepeatMode.OFF
    }
    _repeatMode.value = next
    currentPlayer?.isLooping = (next == VideoRepeatMode.ONE)
    return next
  }

  fun setRepeatMode(mode: VideoRepeatMode) {
    _repeatMode.value = mode
    currentPlayer?.isLooping = (mode == VideoRepeatMode.ONE)
  }

  fun toggleControlsLock() {
    _isControlsLocked.value = !_isControlsLocked.value
  }

  fun setControlsLocked(locked: Boolean) {
    _isControlsLocked.value = locked
  }

  fun toggleAudioOnlyMode() {
    _isAudioOnlyMode.value = !_isAudioOnlyMode.value
  }

  fun setPointA() {
    _pointA.value = _currentPosition.value
  }

  fun setPointB() {
    val cur = _currentPosition.value
    val pa = _pointA.value ?: 0L
    if (cur > pa) {
      _pointB.value = cur
    } else {
      _pointB.value = pa + 5000L
    }
  }

  fun clearABRepeat() {
    _pointA.value = null
    _pointB.value = null
  }

  fun addBookmark(): Long {
    val current = _currentPosition.value
    val updated = (_bookmarks.value + current).distinct().sorted()
    _bookmarks.value = updated
    return current
  }

  fun removeBookmark(timestamp: Long) {
    _bookmarks.value = _bookmarks.value.filter { it != timestamp }
  }

  fun setEqualizerPreset(preset: String) {
    _equalizerPreset.value = preset
  }

  fun setSleepTimer(minutes: Int?) {
    _sleepTimerMinutes.value = minutes
    sleepTimerJob?.cancel()
    if (minutes == null || minutes <= 0) {
      _sleepTimerSecondsLeft.value = null
      return
    }

    val totalSeconds = minutes * 60
    _sleepTimerSecondsLeft.value = totalSeconds
    sleepTimerJob = scope.launch {
      var remaining = totalSeconds
      while (remaining > 0) {
        delay(1000)
        remaining--
        _sleepTimerSecondsLeft.value = remaining
      }
      pause()
      _sleepTimerSecondsLeft.value = null
      _sleepTimerMinutes.value = null
    }
  }

  fun seekTo(posMs: Long) {
    _currentPosition.value = posMs
    currentPlayer?.let {
      try {
        it.seekTo(posMs.toInt())
      } catch (_: Exception) {}
    }
  }

  fun seekDelta(deltaMs: Long) {
    val newPos = (_currentPosition.value + deltaMs).coerceIn(0L, _duration.value)
    seekTo(newPos)
  }

  fun play() {
    currentPlayer?.let {
      try {
        it.start()
        _isPlaying.value = true
      } catch (_: Exception) {}
    }
  }

  fun pause() {
    currentPlayer?.let {
      try {
        if (it.isPlaying) {
          it.pause()
        }
        _isPlaying.value = false
      } catch (_: Exception) {}
    }
  }

  fun togglePlayPause() {
    if (_isPlaying.value) {
      pause()
    } else {
      play()
    }
  }

  fun onAppBackgrounded(context: Context) {
    isAppInForeground = false
    if (_isBackgroundPlaybackEnabled.value && _isPlaying.value && _activeItem.value != null) {
      // Start foreground service for background audio playback
      val intent = Intent(context, VideoPlaybackService::class.java).apply {
        action = VideoPlaybackService.ACTION_START
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    } else {
      // Immediately stop playback if background playback is turned off
      pause()
      stopBackgroundService()
    }
  }

  fun onAppForegrounded(context: Context) {
    isAppInForeground = true
    stopBackgroundService()
  }

  fun stopBackgroundService() {
    appContext?.let { ctx ->
      val intent = Intent(ctx, VideoPlaybackService::class.java).apply {
        action = VideoPlaybackService.ACTION_STOP
      }
      ctx.stopService(intent)
    }
  }

  fun captureVideoFrame(context: Context, item: MediaItem, positionMs: Long): Bitmap? {
    return try {
      val retriever = MediaMetadataRetriever()
      val localFile = File(item.uri)
      if (localFile.exists()) {
        retriever.setDataSource(localFile.absolutePath)
      } else if (item.uri.startsWith("content://")) {
        retriever.setDataSource(context, Uri.parse(item.uri))
      } else {
        retriever.setDataSource(item.uri)
      }
      val frame = retriever.getFrameAtTime(positionMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
      retriever.release()
      frame
    } catch (_: Exception) {
      null
    }
  }
}

class VideoPlaybackService : Service() {
  companion object {
    const val CHANNEL_ID = "nova_video_playback_channel"
    const val NOTIFICATION_ID = 4040
    const val ACTION_START = "com.example.service.action.START"
    const val ACTION_STOP = "com.example.service.action.STOP"
    const val ACTION_TOGGLE = "com.example.service.action.TOGGLE"
  }

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_STOP -> {
        VideoPlaybackManager.pause()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        return START_NOT_STICKY
      }
      ACTION_TOGGLE -> {
        VideoPlaybackManager.togglePlayPause()
        updateNotification()
      }
      ACTION_START -> {
        startForegroundWithNotification()
      }
    }
    return START_STICKY
  }

  private fun startForegroundWithNotification() {
    val notification = buildNotification()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }
  }

  private fun updateNotification() {
    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.notify(NOTIFICATION_ID, buildNotification())
  }

  private fun buildNotification(): Notification {
    val item = VideoPlaybackManager.activeItem.value
    val isPlaying = VideoPlaybackManager.isPlaying.value

    val appIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val toggleIntent = Intent(this, VideoPlaybackService::class.java).apply {
      action = ACTION_TOGGLE
    }
    val togglePendingIntent = PendingIntent.getService(
      this, 1, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, VideoPlaybackService::class.java).apply {
      action = ACTION_STOP
    }
    val stopPendingIntent = PendingIntent.getService(
      this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle(item?.title ?: "Nova Gallery Video")
      .setContentText(if (isPlaying) "Playing audio in background" else "Audio playback paused")
      .setSmallIcon(android.R.drawable.ic_media_play)
      .setContentIntent(contentPendingIntent)
      .setOngoing(isPlaying)
      .addAction(
        if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
        if (isPlaying) "Pause" else "Play",
        togglePendingIntent
      )
      .addAction(
        android.R.drawable.ic_menu_close_clear_cancel,
        "Stop",
        stopPendingIntent
      )
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText(if (isPlaying) "Playing audio in background" else "Audio playback paused")
      )
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Nova Video Playback",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Background audio playback notifications for Nova Gallery"
        setShowBadge(false)
      }
      val nm = getSystemService(NotificationManager::class.java)
      nm?.createNotificationChannel(channel)
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    super.onDestroy()
  }
}
