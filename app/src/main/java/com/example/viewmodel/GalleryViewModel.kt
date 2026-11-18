package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GalleryRepository
import com.example.data.local.GalleryDatabase
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.security.AuthResult
import com.example.security.LockType
import com.example.security.VaultSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VaultStatus {
  LOCKED,
  UNLOCKED_MASTER,
  UNLOCKED_DECOY
}

enum class GridSpan(val count: Int) {
  TWO(2),
  THREE(3),
  FOUR(4),
  LIST(1)
}

enum class SortOrder(val label: String) {
  DATE_ADDED("Date Added"),
  NAME("Name"),
  SIZE("Size")
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: GalleryRepository
  val vaultSecurity: VaultSecurityManager = VaultSecurityManager(application)

  init {
    val db = GalleryDatabase.getDatabase(application)
    repository = GalleryRepository(application, db.mediaDao())
    viewModelScope.launch {
      repository.ensureInitialData()
      if (prefs.getBoolean("rimzim_permission_granted", false)) {
        repository.scanDeviceMedia()
      }
    }
  }

  private val prefs = application.getSharedPreferences("nova_gallery_prefs", android.content.Context.MODE_PRIVATE)

  // --- UI Filter & Search States ---
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedTag = MutableStateFlow<String?>(null)
  val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

  private val _selectedAlbum = MutableStateFlow<String?>(null)
  val selectedAlbum: StateFlow<String?> = _selectedAlbum.asStateFlow()

  private val _typeFilter = MutableStateFlow<MediaType?>(null)
  val typeFilter: StateFlow<MediaType?> = _typeFilter.asStateFlow()

  private val _onlyFavorites = MutableStateFlow(false)
  val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

  // --- Theme Mode State (false = Dark #0F141C Main Interface default, true = Light mode) ---
  private val _isLightMode = MutableStateFlow(prefs.getBoolean("nova_v3_is_light", false))
  val isLightMode: StateFlow<Boolean> = _isLightMode.asStateFlow()

  fun setLightMode(enabled: Boolean) {
    _isLightMode.value = enabled
    prefs.edit().putBoolean("nova_v3_is_light", enabled).apply()
  }

  fun toggleTheme() {
    val next = !_isLightMode.value
    _isLightMode.value = next
    prefs.edit().putBoolean("nova_v3_is_light", next).apply()
  }

  // Backwards compatibility alias
  val isWhiteTheme: StateFlow<Boolean> = _isLightMode.asStateFlow()

  // --- Storage Scanned & Permission State (Persisted across restarts) ---
  private val _isStorageScanned = MutableStateFlow(prefs.getBoolean("rimzim_permission_granted", false))
  val isStorageScanned: StateFlow<Boolean> = _isStorageScanned.asStateFlow()

  private val _isScanningNow = MutableStateFlow(false)
  val isScanningNow: StateFlow<Boolean> = _isScanningNow.asStateFlow()

  fun performStorageScan() {
    _isStorageScanned.value = true
    prefs.edit().putBoolean("rimzim_permission_granted", true).apply()
    viewModelScope.launch {
      _isScanningNow.value = true
      repository.scanDeviceMedia()
      kotlinx.coroutines.delay(500L)
      _isScanningNow.value = false
    }
  }

  fun resetStorageScan() {
    _isStorageScanned.value = false
    _isScanningNow.value = false
    prefs.edit().putBoolean("rimzim_permission_granted", false).apply()
  }

  private val _gridSpan = MutableStateFlow(GridSpan.THREE)

  val gridSpan: StateFlow<GridSpan> = _gridSpan.asStateFlow()

  private val _selectedMediaIds = MutableStateFlow<Set<Long>>(emptySet())
  val selectedMediaIds: StateFlow<Set<Long>> = _selectedMediaIds.asStateFlow()

  private val _isSelectionMode = MutableStateFlow(false)
  val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

  // --- Safe Vault State ---
  private val _vaultStatus = MutableStateFlow(VaultStatus.LOCKED)
  val vaultStatus: StateFlow<VaultStatus> = _vaultStatus.asStateFlow()

  // --- Active Viewing / Editing Item ---
  private val _viewingItem = MutableStateFlow<MediaItem?>(null)
  val viewingItem: StateFlow<MediaItem?> = _viewingItem.asStateFlow()

  private val _isEditingPhoto = MutableStateFlow(false)
  val isEditingPhoto: StateFlow<Boolean> = _isEditingPhoto.asStateFlow()

  // --- Flows from Repository ---
  val allActiveMedia: StateFlow<List<MediaItem>> = repository.activeMedia
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val trashMedia: StateFlow<List<MediaItem>> = repository.trashMedia
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val vaultedMedia: StateFlow<List<MediaItem>> = repository.vaultedMedia
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val decoyVaultedMedia: StateFlow<List<MediaItem>> = repository.decoyVaultedMedia
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
  val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

  private val _sortAscending = MutableStateFlow(false)
  val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

  fun setSortOrder(order: SortOrder) {
    if (_sortOrder.value == order) {
      _sortAscending.value = !_sortAscending.value
    } else {
      _sortOrder.value = order
      _sortAscending.value = (order == SortOrder.NAME)
    }
  }

  fun toggleSortDirection() {
    _sortAscending.value = !_sortAscending.value
  }

  fun sortMediaList(list: List<MediaItem>, order: SortOrder = _sortOrder.value, ascending: Boolean = _sortAscending.value): List<MediaItem> {
    return when (order) {
      SortOrder.DATE_ADDED -> if (ascending) list.sortedBy { it.dateAdded } else list.sortedByDescending { it.dateAdded }
      SortOrder.NAME -> if (ascending) list.sortedBy { it.title.lowercase() } else list.sortedByDescending { it.title.lowercase() }
      SortOrder.SIZE -> if (ascending) list.sortedBy { it.sizeBytes } else list.sortedByDescending { it.sizeBytes }
    }
  }

  private val _antiApkFilterEnabled = MutableStateFlow(true)
  val antiApkFilterEnabled: StateFlow<Boolean> = _antiApkFilterEnabled.asStateFlow()

data class BaseFilter(
  val query: String = "",
  val tag: String? = null,
  val album: String? = null,
  val type: MediaType? = null,
  val onlyFavorites: Boolean = false
)

data class GalleryFilterState(
  val query: String = "",
  val tag: String? = null,
  val album: String? = null,
  val type: MediaType? = null,
  val onlyFavorites: Boolean = false,
  val antiApkFilter: Boolean = true,
  val sortOrder: SortOrder = SortOrder.DATE_ADDED,
  val sortAscending: Boolean = false
)

  private val baseFilterState = combine(
    searchQuery,
    selectedTag,
    selectedAlbum,
    typeFilter,
    onlyFavorites
  ) { query, tag, album, type, favs ->
    BaseFilter(query, tag, album, type, favs)
  }

  private val sortState = combine(sortOrder, sortAscending) { order, asc -> order to asc }

  private val filterState = combine(
    baseFilterState,
    antiApkFilterEnabled,
    sortState
  ) { base, antiApk, (order, asc) ->
    GalleryFilterState(base.query, base.tag, base.album, base.type, base.onlyFavorites, antiApk, order, asc)
  }

  private val searchDateFormat1 = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
  private val searchDateFormat2 = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
  private val searchDateFormat3 = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())

  // --- Filtered Media for Home Feed ---
  val filteredMedia: StateFlow<List<MediaItem>> = combine(
    allActiveMedia,
    filterState
  ) { list, filter ->
    val filtered = list.filter { item ->
      val q = filter.query.trim()
      val matchesQuery = q.isBlank() || run {
        val dateObj = java.util.Date(item.dateAdded)
        val formattedDate = "${searchDateFormat1.format(dateObj)} ${searchDateFormat2.format(dateObj)} ${searchDateFormat3.format(dateObj)}"
        item.title.contains(q, ignoreCase = true) ||
          item.album.contains(q, ignoreCase = true) ||
          item.tags.any { it.contains(q, ignoreCase = true) || it.removePrefix("#").contains(q.removePrefix("#"), ignoreCase = true) } ||
          formattedDate.contains(q, ignoreCase = true)
      }

      val matchesTag = filter.tag == null || item.tags.any { it.equals(filter.tag, ignoreCase = true) }
      val matchesAlbum = filter.album == null || item.album.equals(filter.album, ignoreCase = true)
      val matchesType = filter.type == null || item.type == filter.type
      val matchesFav = !filter.onlyFavorites || item.isFavorite

      val passesAntiApk = !filter.antiApkFilter || (
        !item.title.endsWith(".apk", ignoreCase = true) &&
        !item.title.contains(".apk", ignoreCase = true) &&
        !item.uri.endsWith(".apk", ignoreCase = true)
      )

      matchesQuery && matchesTag && matchesAlbum && matchesType && matchesFav && passesAntiApk
    }
    sortMediaList(filtered, filter.sortOrder, filter.sortAscending)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // --- Filter Setters ---
  fun toggleAntiApkFilter() { _antiApkFilterEnabled.value = !_antiApkFilterEnabled.value }
  fun setSearchQuery(query: String) { _searchQuery.value = query }
  fun setSelectedTag(tag: String?) { _selectedTag.value = tag }
  fun setSelectedAlbum(album: String?) { _selectedAlbum.value = album }
  fun setTypeFilter(type: MediaType?) { _typeFilter.value = type }
  fun toggleOnlyFavorites() { _onlyFavorites.value = !_onlyFavorites.value }
  fun setGridSpan(span: GridSpan) { _gridSpan.value = span }

  // --- Selection Mode ---
  fun toggleSelection(id: Long) {
    val current = _selectedMediaIds.value.toMutableSet()
    if (current.contains(id)) {
      current.remove(id)
    } else {
      current.add(id)
    }
    _selectedMediaIds.value = current
    _isSelectionMode.value = current.isNotEmpty()
  }

  fun clearSelection() {
    _selectedMediaIds.value = emptySet()
    _isSelectionMode.value = false
  }

  fun selectAll() {
    val ids = filteredMedia.value.map { it.id }.toSet()
    _selectedMediaIds.value = ids
    _isSelectionMode.value = ids.isNotEmpty()
  }

  // --- Media Actions ---
  fun toggleFavorite(item: MediaItem) {
    viewModelScope.launch {
      repository.setFavorite(item.id, !item.isFavorite)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = _viewingItem.value?.copy(isFavorite = !item.isFavorite)
      }
    }
  }

  fun moveToTrash(id: Long) {
    viewModelScope.launch {
      repository.moveToTrash(id)
      if (_viewingItem.value?.id == id) {
        _viewingItem.value = null
      }
    }
  }

  fun moveSelectedToTrash() {
    val ids = _selectedMediaIds.value
    viewModelScope.launch {
      ids.forEach { repository.moveToTrash(it) }
      clearSelection()
    }
  }

  fun restoreFromTrash(id: Long) {
    viewModelScope.launch {
      repository.restoreFromTrash(id)
    }
  }

  fun restoreAllTrash() {
    val ids = trashMedia.value.map { it.id }
    viewModelScope.launch {
      repository.restoreMultipleFromTrash(ids)
    }
  }

  fun deletePermanently(ids: List<Long>) {
    viewModelScope.launch {
      repository.deletePermanently(ids)
    }
  }

  fun deleteMediaPermanently(item: MediaItem) {
    viewModelScope.launch {
      repository.deletePermanently(item)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = null
      }
    }
  }

  fun renameMedia(item: MediaItem, newName: String) {
    if (newName.isBlank()) return
    viewModelScope.launch {
      val updated = item.copy(title = newName.trim())
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun addCustomTag(item: MediaItem, rawTag: String) {
    val cleanTag = rawTag.trim()
    if (cleanTag.isBlank()) return
    viewModelScope.launch {
      val updatedTags = (item.tags + cleanTag).distinct()
      val updated = item.copy(tags = updatedTags)
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun removeCustomTag(item: MediaItem, tagToRemove: String) {
    viewModelScope.launch {
      val updatedTags = item.tags.filterNot { it.equals(tagToRemove, ignoreCase = true) }
      val updated = item.copy(tags = updatedTags)
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun emptyTrash() {
    viewModelScope.launch {
      repository.emptyTrash()
    }
  }

  fun moveToVault(id: Long) {
    viewModelScope.launch {
      repository.moveToVault(id)
      if (_viewingItem.value?.id == id) {
        _viewingItem.value = null
      }
    }
  }

  fun moveSelectedToVault() {
    val ids = _selectedMediaIds.value
    viewModelScope.launch {
      ids.forEach { repository.moveToVault(it) }
      clearSelection()
    }
  }

  fun removeFromVault(id: Long) {
    viewModelScope.launch {
      repository.removeFromVault(id)
      if (_viewingItem.value?.id == id) {
        _viewingItem.value = null
      }
    }
  }

  fun importMedia(uri: String, isVideo: Boolean, title: String, album: String = "Imported") {
    viewModelScope.launch {
      repository.importMedia(getApplication(), uri, isVideo, title, album)
    }
  }

  fun moveSelectedToAlbum(newAlbum: String) {
    val ids = _selectedMediaIds.value.toList()
    if (ids.isEmpty() || newAlbum.isBlank()) return
    viewModelScope.launch {
      repository.updateAlbum(ids, newAlbum.trim())
      clearSelection()
    }
  }

  fun moveMediaToAlbum(item: MediaItem, newAlbum: String) {
    if (newAlbum.isBlank()) return
    viewModelScope.launch {
      repository.updateAlbum(listOf(item.id), newAlbum.trim())
      val updated = item.copy(album = newAlbum.trim())
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun createAlbumWithMedia(albumName: String, mediaUri: String? = null, isVideo: Boolean = false) {
    if (albumName.isBlank()) return
    viewModelScope.launch {
      val name = albumName.trim()
      if (_selectedMediaIds.value.isNotEmpty()) {
        repository.updateAlbum(_selectedMediaIds.value.toList(), name)
        clearSelection()
      } else if (!mediaUri.isNullOrBlank()) {
        repository.importMedia(
          context = getApplication(),
          uri = mediaUri,
          isVideo = isVideo,
          title = if (isVideo) "Video in $name" else "Photo in $name",
          album = name
        )
      }
    }
  }

  fun saveNewMasterPin(newPin: String): Boolean {
    return vaultSecurity.saveNewMasterPin(newPin)
  }

  // --- Photo Editor Actions ---
  fun setViewingItem(item: MediaItem?) {
    _viewingItem.value = item
    _isEditingPhoto.value = false
  }

  fun navigateToNextMedia() {
    val current = _viewingItem.value ?: return
    val list = allActiveMedia.value
    val currentIndex = list.indexOfFirst { it.id == current.id }
    if (currentIndex != -1 && currentIndex < list.size - 1) {
      _viewingItem.value = list[currentIndex + 1]
    }
  }

  fun navigateToPreviousMedia() {
    val current = _viewingItem.value ?: return
    val list = allActiveMedia.value
    val currentIndex = list.indexOfFirst { it.id == current.id }
    if (currentIndex > 0) {
      _viewingItem.value = list[currentIndex - 1]
    }
  }

  fun setEditingPhoto(editing: Boolean) {
    _isEditingPhoto.value = editing
  }

  fun saveEditedPhoto(
    filter: String,
    rotation: Float,
    brightness: Float,
    contrast: Float,
    newTags: List<String>
  ) {
    val current = _viewingItem.value ?: return
    val updated = current.copy(
      filterApplied = filter,
      rotationDegrees = rotation,
      brightness = brightness,
      contrast = contrast,
      tags = (current.tags + newTags).distinct()
    )
    viewModelScope.launch {
      repository.updateMedia(updated)
      _viewingItem.value = updated
      _isEditingPhoto.value = false
    }
  }

  // --- Safe Vault Authentication ---
  fun unlockVaultWithSecret(secret: String): AuthResult {
    val result = vaultSecurity.verifySecret(secret)
    when (result) {
      is AuthResult.SuccessMaster -> _vaultStatus.value = VaultStatus.UNLOCKED_MASTER
      is AuthResult.SuccessDecoy -> _vaultStatus.value = VaultStatus.UNLOCKED_DECOY
      is AuthResult.Error -> { /* keep locked */ }
    }
    return result
  }

  fun unlockVaultWithBiometrics() {
    _vaultStatus.value = VaultStatus.UNLOCKED_MASTER
  }

  fun lockVault() {
    _vaultStatus.value = VaultStatus.LOCKED
  }

  // --- VIP Local Pro Quick Tools ---
  fun applyFlipHorizontally(item: MediaItem) {
    viewModelScope.launch {
      val isFlipped = item.filterApplied == "FLIP_H"
      val updated = item.copy(
        filterApplied = if (isFlipped) "NORMAL" else "FLIP_H"
      )
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun applyFlipVertically(item: MediaItem) {
    viewModelScope.launch {
      val isFlipped = item.filterApplied == "FLIP_V"
      val updated = item.copy(
        filterApplied = if (isFlipped) "NORMAL" else "FLIP_V"
      )
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun applyMirror(item: MediaItem) {
    viewModelScope.launch {
      val isMirror = item.filterApplied == "MIRROR"
      val updated = item.copy(
        filterApplied = if (isMirror) "NORMAL" else "MIRROR"
      )
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun applyHdClear(item: MediaItem) {
    viewModelScope.launch {
      val isHd = item.contrast > 1.2f
      val updated = item.copy(
        contrast = if (isHd) 1.0f else 1.35f,
        brightness = if (isHd) 0.0f else 0.08f,
        filterApplied = if (isHd) "NORMAL" else "VIVID"
      )
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun applyVideoFlip(item: MediaItem) {
    viewModelScope.launch {
      val nextRotation = (item.rotationDegrees + 90f) % 360f
      val updated = item.copy(rotationDegrees = nextRotation)
      repository.updateMedia(updated)
      if (_viewingItem.value?.id == item.id) {
        _viewingItem.value = updated
      }
    }
  }

  fun exportDesignerStudio(
    sourceItem: MediaItem,
    frameStyle: String,
    aspectRatio: String,
    watermark: Boolean
  ) {
    viewModelScope.launch {
      val exportedItem = sourceItem.copy(
        id = 0,
        title = "${sourceItem.title} ($frameStyle)",
        album = "Designer Studio",
        dateAdded = System.currentTimeMillis(),
        tags = (sourceItem.tags + listOf("#DesignerStudio", "#$frameStyle", "#OfflineHD")).distinct(),
        filterApplied = when (frameStyle) {
          "Cyber Neon" -> "VIVID"
          "Sunset Mirage" -> "WARM"
          "Emerald Aura" -> "COOL"
          else -> sourceItem.filterApplied
        }
      )
      repository.insertMediaItem(exportedItem)
    }
  }
}
