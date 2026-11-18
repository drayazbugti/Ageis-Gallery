package com.example.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.example.data.local.MediaDao
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class GalleryRepository(
  private val context: Context,
  private val mediaDao: MediaDao
) {
  private val prefs = context.getSharedPreferences("nova_deleted_storage_prefs", Context.MODE_PRIVATE)

  val activeMedia: Flow<List<MediaItem>> = mediaDao.getActiveMedia()
  val trashMedia: Flow<List<MediaItem>> = mediaDao.getTrashMedia()
  val vaultedMedia: Flow<List<MediaItem>> = mediaDao.getVaultedMedia()
  val decoyVaultedMedia: Flow<List<MediaItem>> = mediaDao.getDecoyVaultedMedia()

  private fun getPermanentlyDeletedKeys(): Set<String> {
    return prefs.getStringSet("permanently_deleted_keys", emptySet()) ?: emptySet()
  }

  private fun trackPermanentlyDeleted(keys: Collection<String>) {
    val current = getPermanentlyDeletedKeys().toMutableSet()
    current.addAll(keys)
    prefs.edit().putStringSet("permanently_deleted_keys", current).apply()
  }

  private fun untrackPermanentlyDeleted(keys: Collection<String>) {
    val current = getPermanentlyDeletedKeys().toMutableSet()
    current.removeAll(keys.toSet())
    prefs.edit().putStringSet("permanently_deleted_keys", current).apply()
  }

  /**
   * Ensures legacy sample URLs are purged once so the gallery strictly reflects
   * real device/imported media (initially empty until scanned or imported).
   */
  suspend fun ensureInitialData() = withContext(Dispatchers.IO) {
    val samplePurged = prefs.getBoolean("sample_purge_v5", false)
    if (!samplePurged) {
      val sampleUris = SampleMediaProvider.getInitialMedia().map { it.uri }.toSet()
      val allExisting = mediaDao.getAllRaw()
      val toRemove = allExisting.filter { it.uri in sampleUris }
      if (toRemove.isNotEmpty()) {
        mediaDao.deleteByIds(toRemove.map { it.id })
      }
      prefs.edit().putBoolean("sample_purge_v5", true).apply()
    }
  }

  /**
   * Scans real app-scoped storage (`filesDir/imported_photos`, `filesDir/imported_videos`)
   * and app-accessible MediaStore entries (no broad storage permissions required),
   * while skipping anything recorded in the permanent trash/deletion blacklist.
   */
  suspend fun scanDeviceMedia() = withContext(Dispatchers.IO) {
    ensureInitialData()
    val deletedKeys = getPermanentlyDeletedKeys()
    val existingUris = mediaDao.getAllRaw().map { it.uri }.toMutableSet()
    val discovered = mutableListOf<MediaItem>()

    // 1. Scan internal imported media directories
    val dirs = listOf(
      File(context.filesDir, "imported_photos") to MediaType.PHOTO,
      File(context.filesDir, "imported_videos") to MediaType.VIDEO
    )
    dirs.forEach { (dir, mediaType) ->
      if (dir.exists() && dir.isDirectory) {
        dir.listFiles()?.forEach { file ->
          if (file.isFile && file.length() > 0) {
            val path = file.absolutePath
            if (!existingUris.contains(path) &&
              !deletedKeys.contains(path) &&
              !deletedKeys.contains(file.name)
            ) {
              var durationMs = 0L
              var resolution = "1920x1080"
              if (mediaType == MediaType.VIDEO) {
                try {
                  val retriever = MediaMetadataRetriever()
                  retriever.setDataSource(path)
                  durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
                  val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                  val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                  if (w != null && h != null) resolution = "${w}x${h}"
                  retriever.release()
                } catch (_: Exception) {}
              }
              existingUris.add(path)
              discovered.add(
                MediaItem(
                  uri = path,
                  thumbnailUri = path,
                  title = file.name,
                  type = mediaType,
                  album = if (mediaType == MediaType.VIDEO) "Videos" else "Camera",
                  dateAdded = file.lastModified().takeIf { it > 0L } ?: System.currentTimeMillis(),
                  sizeBytes = file.length(),
                  durationMs = durationMs,
                  resolution = resolution,
                  location = "Device Storage",
                  tags = listOf(if (mediaType == MediaType.VIDEO) "#Video" else "#Photo")
                )
              )
            }
          }
        }
      }
    }

    // 2. Query app-accessible MediaStore collections (returns only app-owned items without READ_MEDIA_*)
    scanMediaStoreCollection(
      uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      mediaType = MediaType.PHOTO,
      deletedKeys = deletedKeys,
      existingUris = existingUris,
      out = discovered
    )
    scanMediaStoreCollection(
      uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
      mediaType = MediaType.VIDEO,
      deletedKeys = deletedKeys,
      existingUris = existingUris,
      out = discovered
    )

    if (discovered.isNotEmpty()) {
      mediaDao.insertAll(discovered)
    }
  }

  private fun scanMediaStoreCollection(
    uri: Uri,
    mediaType: MediaType,
    deletedKeys: Set<String>,
    existingUris: MutableSet<String>,
    out: MutableList<MediaItem>
  ) {
    val projection = mutableListOf(
      MediaStore.MediaColumns._ID,
      MediaStore.MediaColumns.DISPLAY_NAME,
      MediaStore.MediaColumns.DATE_ADDED,
      MediaStore.MediaColumns.SIZE
    ).apply {
      if (mediaType == MediaType.VIDEO) {
        add(MediaStore.Video.VideoColumns.DURATION)
      }
    }.toTypedArray()

    try {
      context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${MediaStore.MediaColumns.DATE_ADDED} DESC"
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
        val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
        val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
        val durCol = if (mediaType == MediaType.VIDEO) {
          cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
        } else -1

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val contentUri = ContentUris.withAppendedId(uri, id).toString()
          val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Media_$id" else "Media_$id"
          val dateSec = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
          val sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
          val durationMs = if (durCol >= 0) cursor.getLong(durCol) else 0L

          if (!existingUris.contains(contentUri) &&
            !deletedKeys.contains(contentUri) &&
            !deletedKeys.contains(name)
          ) {
            existingUris.add(contentUri)
            out.add(
              MediaItem(
                uri = contentUri,
                thumbnailUri = contentUri,
                title = name,
                type = mediaType,
                album = if (mediaType == MediaType.VIDEO) "Videos" else "Camera",
                dateAdded = if (dateSec > 0L) dateSec * 1000L else System.currentTimeMillis(),
                sizeBytes = sizeBytes.coerceAtLeast(1_000_000L),
                durationMs = durationMs,
                resolution = "HD",
                location = "Device Storage",
                tags = listOf(if (mediaType == MediaType.VIDEO) "#Video" else "#Photo")
              )
            )
          }
        }
      }
    } catch (_: Exception) {
      // Safe fallback when zero broad permissions are requested
    }
  }

  suspend fun insertMediaItem(item: MediaItem): Long = mediaDao.insert(item)

  suspend fun getMediaById(id: Long): MediaItem? = mediaDao.getMediaById(id)

  suspend fun updateMedia(item: MediaItem) = mediaDao.update(item)

  suspend fun setFavorite(id: Long, isFav: Boolean) = mediaDao.setFavorite(id, isFav)

  suspend fun moveToTrash(id: Long) {
    val item = mediaDao.getMediaById(id)
    if (item != null) {
      // Track trashed item so app restarts or rescans never restore it automatically
      trackPermanentlyDeleted(listOf(item.id.toString(), item.uri, item.title))
      if (item.type == MediaType.VIDEO) {
        deletePermanently(item)
      } else {
        mediaDao.moveToTrash(id)
      }
    }
  }

  suspend fun restoreFromTrash(id: Long) {
    val item = mediaDao.getMediaById(id)
    if (item != null) {
      untrackPermanentlyDeleted(listOf(item.id.toString(), item.uri, item.title))
    }
    mediaDao.restoreFromTrash(id)
  }

  suspend fun restoreMultipleFromTrash(ids: List<Long>) {
    ids.forEach { id ->
      mediaDao.getMediaById(id)?.let { item ->
        untrackPermanentlyDeleted(listOf(item.id.toString(), item.uri, item.title))
      }
    }
    mediaDao.restoreMultipleFromTrash(ids)
  }

  suspend fun updateAlbum(ids: List<Long>, newAlbum: String) = mediaDao.updateAlbum(ids, newAlbum)

  val allAlbums: Flow<List<String>> = mediaDao.getAllAlbums()

  suspend fun deletePermanently(item: MediaItem) {
    trackPermanentlyDeleted(listOf(item.id.toString(), item.uri, item.title))
    mediaDao.deleteById(item.id)

    try {
      if (item.uri.startsWith("/") || item.uri.startsWith("file://")) {
        val path = item.uri.removePrefix("file://")
        val file = File(path)
        if (file.exists()) {
          file.delete()
        }
      }
    } catch (_: Exception) {}
  }

  suspend fun deletePermanently(ids: List<Long>) {
    ids.forEach { id ->
      val item = mediaDao.getMediaById(id)
      if (item != null) {
        deletePermanently(item)
      } else {
        trackPermanentlyDeleted(listOf(id.toString()))
        mediaDao.deleteById(id)
      }
    }
  }

  suspend fun emptyTrash() {
    val trash = mediaDao.getTrashList()
    trash.forEach { deletePermanently(it) }
    mediaDao.emptyTrash()
  }

  suspend fun moveToVault(id: Long) = mediaDao.moveToVault(id)

  suspend fun removeFromVault(id: Long) = mediaDao.removeFromVault(id)

  suspend fun importMedia(
    context: Context,
    uri: String,
    isVideo: Boolean,
    title: String,
    album: String = "Imported"
  ) = withContext(Dispatchers.IO) {
    var finalUri = uri
    var finalSize = if (isVideo) 15_000_000L else 3_200_000L
    var finalDuration = if (isVideo) 15_000L else 0L
    var finalResolution = "1920x1080"

    if (uri.startsWith("content://")) {
      try {
        val parsedUri = Uri.parse(uri)
        val subDir = if (isVideo) "imported_videos" else "imported_photos"
        val storageDir = File(context.filesDir, subDir).apply { mkdirs() }
        val extension = if (isVideo) "mp4" else "jpg"
        val destFile = File(storageDir, "media_${System.currentTimeMillis()}_${(100..999).random()}.$extension")

        context.contentResolver.openInputStream(parsedUri)?.use { input ->
          destFile.outputStream().use { output ->
            input.copyTo(output)
          }
        }

        if (destFile.exists() && destFile.length() > 0) {
          finalUri = destFile.absolutePath
          finalSize = destFile.length()
        }
      } catch (_: Exception) {}
    }

    if (isVideo) {
      try {
        val retriever = MediaMetadataRetriever()
        if (finalUri.startsWith("/")) {
          retriever.setDataSource(finalUri)
        } else {
          retriever.setDataSource(context, Uri.parse(finalUri))
        }
        val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (dur != null) {
          finalDuration = dur.toLongOrNull() ?: finalDuration
        }
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        if (width != null && height != null) {
          finalResolution = "${width}x${height}"
        }
        retriever.release()
      } catch (_: Exception) {}
    }

    val newItem = MediaItem(
      uri = finalUri,
      thumbnailUri = finalUri,
      title = title.ifBlank { if (isVideo) "Imported Video" else "Imported Photo" },
      type = if (isVideo) MediaType.VIDEO else MediaType.PHOTO,
      album = album,
      dateAdded = System.currentTimeMillis(),
      sizeBytes = finalSize,
      durationMs = finalDuration,
      resolution = finalResolution,
      location = "Device Storage",
      tags = listOf("#Imported", if (isVideo) "#Video" else "#Photo")
    )
    mediaDao.insert(newItem)
  }
}
