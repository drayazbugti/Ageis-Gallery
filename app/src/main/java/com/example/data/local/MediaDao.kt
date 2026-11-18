package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
  @Query("SELECT * FROM media_items WHERE isTrash = 0 AND isVaulted = 0 ORDER BY dateAdded DESC")
  fun getActiveMedia(): Flow<List<MediaItem>>

  @Query("SELECT * FROM media_items WHERE isTrash = 1 ORDER BY trashDate DESC")
  fun getTrashMedia(): Flow<List<MediaItem>>

  @Query("SELECT * FROM media_items WHERE isTrash = 1")
  suspend fun getTrashList(): List<MediaItem>

  @Query("SELECT * FROM media_items")
  suspend fun getAllRaw(): List<MediaItem>

  @Query("SELECT * FROM media_items WHERE isVaulted = 1 AND isTrash = 0 ORDER BY dateAdded DESC")
  fun getVaultedMedia(): Flow<List<MediaItem>>

  @Query("SELECT * FROM media_items WHERE isVaulted = 1 AND isDecoy = 1 AND isTrash = 0 ORDER BY dateAdded DESC")
  fun getDecoyVaultedMedia(): Flow<List<MediaItem>>

  @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
  suspend fun getMediaById(id: Long): MediaItem?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: MediaItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<MediaItem>)

  @Update
  suspend fun update(item: MediaItem)

  @Delete
  suspend fun delete(item: MediaItem)

  @Query("DELETE FROM media_items WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM media_items WHERE id IN (:ids)")
  suspend fun deleteByIds(ids: List<Long>)

  @Query("DELETE FROM media_items WHERE isTrash = 1")
  suspend fun emptyTrash()

  @Query("UPDATE media_items SET isTrash = 1, trashDate = :currentTime WHERE id = :id")
  suspend fun moveToTrash(id: Long, currentTime: Long = System.currentTimeMillis())

  @Query("UPDATE media_items SET isTrash = 0, trashDate = 0 WHERE id = :id")
  suspend fun restoreFromTrash(id: Long)

  @Query("UPDATE media_items SET isTrash = 0, trashDate = 0 WHERE id IN (:ids)")
  suspend fun restoreMultipleFromTrash(ids: List<Long>)

  @Query("UPDATE media_items SET isVaulted = 1, isFavorite = 0 WHERE id = :id")
  suspend fun moveToVault(id: Long)

  @Query("UPDATE media_items SET isVaulted = 0 WHERE id = :id")
  suspend fun removeFromVault(id: Long)

  @Query("UPDATE media_items SET isFavorite = :isFav WHERE id = :id")
  suspend fun setFavorite(id: Long, isFav: Boolean)

  @Query("UPDATE media_items SET album = :newAlbum WHERE id IN (:ids)")
  suspend fun updateAlbum(ids: List<Long>, newAlbum: String)

  @Query("SELECT DISTINCT album FROM media_items WHERE isTrash = 0 AND isVaulted = 0")
  fun getAllAlbums(): Flow<List<String>>

  @Query("SELECT COUNT(*) FROM media_items")
  suspend fun countTotal(): Int
}
