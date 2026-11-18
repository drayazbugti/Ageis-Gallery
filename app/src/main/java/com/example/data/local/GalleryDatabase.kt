package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Converters
import com.example.data.model.MediaItem

@Database(entities = [MediaItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GalleryDatabase : RoomDatabase() {
  abstract fun mediaDao(): MediaDao

  companion object {
    @Volatile
    private var INSTANCE: GalleryDatabase? = null

    fun getDatabase(context: Context): GalleryDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          GalleryDatabase::class.java,
          "aegis_gallery.db"
        ).fallbackToDestructiveMigration(true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
