package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class MediaType {
  PHOTO,
  VIDEO
}

@Entity(tableName = "media_items")
@TypeConverters(Converters::class)
data class MediaItem(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val uri: String,
  val thumbnailUri: String = uri,
  val title: String,
  val type: MediaType = MediaType.PHOTO,
  val album: String = "Camera",
  val dateAdded: Long = System.currentTimeMillis(),
  val sizeBytes: Long = 2_450_000L,
  val durationMs: Long = 0L,
  val resolution: String = "1920x1080",
  val location: String = "Unknown",
  val isFavorite: Boolean = false,
  val isVaulted: Boolean = false,
  val isDecoy: Boolean = false,
  val isTrash: Boolean = false,
  val trashDate: Long = 0L,
  val tags: List<String> = emptyList(),
  val filterApplied: String = "NORMAL",
  val rotationDegrees: Float = 0f,
  val brightness: Float = 0f,
  val contrast: Float = 1f
) {
  val remainingTrashDays: Int
    get() {
      if (!isTrash || trashDate <= 0L) return 30
      val elapsed = System.currentTimeMillis() - trashDate
      val days = 30 - (elapsed / (1000 * 60 * 60 * 24)).toInt()
      return days.coerceAtLeast(0)
    }

  val formattedSize: String
    get() {
      val kb = sizeBytes / 1024.0
      val mb = kb / 1024.0
      return if (mb >= 1.0) {
        String.format("%.1f MB", mb)
      } else {
        String.format("%.0f KB", kb)
      }
    }

  val formattedDuration: String
    get() {
      if (type != MediaType.VIDEO || durationMs <= 0) return ""
      val totalSecs = durationMs / 1000
      val mins = totalSecs / 60
      val secs = totalSecs % 60
      return String.format("%02d:%02d", mins, secs)
    }
}

class Converters {
  @TypeConverter
  fun fromTagsList(tags: List<String>?): String {
    return tags?.joinToString(",") ?: ""
  }

  @TypeConverter
  fun toTagsList(data: String?): List<String> {
    if (data.isNullOrBlank()) return emptyList()
    return data.split(",").map { it.trim() }.filter { it.isNotEmpty() }
  }

  @TypeConverter
  fun fromMediaType(type: MediaType): String = type.name

  @TypeConverter
  fun toMediaType(value: String): MediaType = try {
    MediaType.valueOf(value)
  } catch (e: Exception) {
    MediaType.PHOTO
  }
}
