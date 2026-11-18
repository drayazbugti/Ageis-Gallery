package com.example.data

import com.example.data.model.MediaItem
import com.example.data.model.MediaType

object SampleMediaProvider {
  fun getInitialMedia(): List<MediaItem> {
    val now = System.currentTimeMillis()
    val hour = 3600_000L
    val day = 24 * hour

    return listOf(
      // --- Today's Section: User Album Media (Exact Match to AGS Gallery HTML Mockup) ---
      MediaItem(
        uri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500",
        thumbnailUri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500",
        title = "Beach_View.jpg",
        type = MediaType.PHOTO,
        album = "Albums",
        dateAdded = now - (1 * hour),
        sizeBytes = 3_780_000L,
        resolution = "3840x2160",
        location = "Bora Bora",
        isFavorite = false,
        tags = listOf("#Ocean", "#Beach", "#Travel")
      ),
      MediaItem(
        uri = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=500",
        thumbnailUri = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=500",
        title = "Ronaldo_Edit.mp4",
        type = MediaType.VIDEO,
        album = "Albums",
        dateAdded = now - (2 * hour),
        sizeBytes = 24_500_000L,
        durationMs = 56_000L, // 0:56
        resolution = "1920x1080",
        location = "Stadium",
        isFavorite = false,
        tags = listOf("#Video", "#Edit", "#Football")
      ),

      // --- Yesterday's Section: Real Architecture & House Videos ---
      MediaItem(
        uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        thumbnailUri = "https://images.unsplash.com/photo-1564013799919-ab600027ffc6",
        title = "Modern House Walkthrough",
        type = MediaType.VIDEO,
        album = "Video Clips",
        dateAdded = now - (1 * day) - (1 * hour),
        sizeBytes = 28_400_000L,
        durationMs = 596_000L, // 09:56
        resolution = "1920x1080 (HD)",
        location = "Scottsdale, AZ",
        isFavorite = false,
        tags = listOf("#House", "#Tour", "#Walkthrough", "#Video")
      ),
      MediaItem(
        uri = "https://images.unsplash.com/photo-1570129477492-45c003edd2be",
        thumbnailUri = "https://images.unsplash.com/photo-1570129477492-45c003edd2be",
        title = "Minimalist Interior Design",
        type = MediaType.PHOTO,
        album = "Modern Interior",
        dateAdded = now - (1 * day) - (3 * hour),
        sizeBytes = 3_150_000L,
        resolution = "3200x2400",
        location = "Austin, TX",
        isFavorite = false,
        tags = listOf("#Interior", "#Minimalist", "#Living")
      ),
      MediaItem(
        uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        thumbnailUri = "https://images.unsplash.com/photo-1513694203232-719a280e022f",
        title = "Villa Sunset Living Tour",
        type = MediaType.VIDEO,
        album = "Villa Architecture",
        dateAdded = now - (1 * day) - (5 * hour),
        sizeBytes = 34_200_000L,
        durationMs = 653_000L, // 10:53
        resolution = "1920x1080 (60fps)",
        location = "Malibu, CA",
        isFavorite = false,
        tags = listOf("#Villa", "#Sunset", "#Architecture", "#Video")
      ),

      // Additional album item for House Designs
      MediaItem(
        uri = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9",
        thumbnailUri = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9",
        title = "Poolside Modern Mansion",
        type = MediaType.PHOTO,
        album = "House Designs",
        dateAdded = now - (3 * day),
        sizeBytes = 4_400_000L,
        resolution = "4000x2800",
        location = "Palm Springs, CA",
        isFavorite = false,
        tags = listOf("#House", "#Pool", "#Modern")
      ),
      MediaItem(
        uri = "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c",
        thumbnailUri = "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c",
        title = "Glass Pavilion Residence",
        type = MediaType.PHOTO,
        album = "House Designs",
        dateAdded = now - (4 * day),
        sizeBytes = 3_800_000L,
        resolution = "3840x2400",
        location = "Aspen, CO",
        isFavorite = false,
        tags = listOf("#House", "#Glass", "#Architecture")
      ),

      // Additional interior item for Modern Interior
      MediaItem(
        uri = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6",
        thumbnailUri = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6",
        title = "Scandinavian Lounge",
        type = MediaType.PHOTO,
        album = "Modern Interior",
        dateAdded = now - (5 * day),
        sizeBytes = 2_900_000L,
        resolution = "3000x2000",
        location = "Copenhagen",
        isFavorite = false,
        tags = listOf("#Interior", "#Nordic", "#Design")
      ),
      MediaItem(
        uri = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace",
        thumbnailUri = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace",
        title = "Cozy Modern Living Room",
        type = MediaType.PHOTO,
        album = "Modern Interior",
        dateAdded = now - (6 * day),
        sizeBytes = 3_100_000L,
        resolution = "3200x2200",
        location = "Stockholm",
        isFavorite = false,
        tags = listOf("#Interior", "#LivingRoom")
      ),

      // Additional video clip for Video Clips
      MediaItem(
        uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        thumbnailUri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
        title = "Aerial Coast Drone Clip",
        type = MediaType.VIDEO,
        album = "Video Clips",
        dateAdded = now - (7 * day),
        sizeBytes = 18_900_000L,
        durationMs = 45_000L,
        resolution = "1920x1080",
        location = "Big Sur, CA",
        isFavorite = false,
        tags = listOf("#Drone", "#Coast", "#Video")
      ),

      // --- Vaulted / Safe Folder Item ---
      MediaItem(
        uri = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=1200&q=80",
        title = "Confidential Property Blueprint",
        type = MediaType.PHOTO,
        album = "SafeFolder",
        dateAdded = now - (15 * day),
        sizeBytes = 1_420_000L,
        resolution = "1920x1080",
        location = "Encrypted Vault",
        isVaulted = true,
        tags = listOf("#Vault", "#Confidential", "#Documents")
      ),

      // --- Trash / Recycle Bin (Exactly 1 item as in Mockup) ---
      MediaItem(
        uri = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80",
        title = "Outdated Blueprint Draft",
        type = MediaType.PHOTO,
        album = "House Designs",
        dateAdded = now - (14 * day),
        sizeBytes = 1_200_000L,
        resolution = "1920x1080",
        location = "Studio",
        isTrash = true,
        trashDate = now - (5 * day), // 25 days remaining
        tags = listOf("#Draft", "#Architecture")
      )
    )
  }
}
