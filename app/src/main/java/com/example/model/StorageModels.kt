package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class StorageCategory(val label: String, val color: Color) {
  APPS_DATA("Apps & Data", ColorApps),
  IMAGES("Images", ColorImages),
  VIDEOS("Videos", ColorVideos),
  AUDIO("Audio & Music", ColorAudio),
  DOCUMENTS("Documents", ColorDocs),
  APKS("Installation APKs", ColorApks),
  JUNK_CACHE("Cache & Temp", ColorJunk),
  SYSTEM_OTHER("System & Other", ColorSystem)
}

enum class JunkType(val title: String, val description: String) {
  APP_CACHE("App Cache", "Residual cache data safely clearable"),
  TEMP_FILES("Temporary Files", ".tmp, .bak and unfinished downloads"),
  LOG_FILES("Log & Crash Reports", "Old error logs and debug files"),
  THUMBNAIL_CACHE("Thumbnail Cache", "Cached gallery previews"),
  OBSOLETE_APK("Obsolete APK Packages", "Downloaded installer files already installed"),
  EMPTY_FOLDERS("Empty Directories", "Leftover folders from removed apps")
}

data class StorageBreakdown(
  val totalBytes: Long = 64L * 1024 * 1024 * 1024, // 64 GB default fallback
  val freeBytes: Long = 18L * 1024 * 1024 * 1024,
  val usedBytes: Long = 46L * 1024 * 1024 * 1024,
  val categorySizes: Map<StorageCategory, Long> = emptyMap(),
  val lowStorageWarningThresholdPercent: Float = 0.15f
) {
  val percentUsed: Float
    get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

  val percentFree: Float
    get() = if (totalBytes > 0) (freeBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

  val isLowStorage: Boolean
    get() = percentFree <= lowStorageWarningThresholdPercent
}

data class StorageFileItem(
  val id: String,
  val name: String,
  val path: String,
  val sizeBytes: Long,
  val category: StorageCategory,
  val lastModified: Long,
  val isDuplicate: Boolean = false,
  val duplicateGroupId: String? = null,
  val isDuplicateOriginal: Boolean = false,
  val isJunk: Boolean = false,
  val junkType: JunkType? = null,
  val uriString: String? = null,
  val isLargeFile: Boolean = false,
  val extension: String = ""
)

data class DuplicateGroup(
  val id: String,
  val fileName: String,
  val fileSizeBytes: Long,
  val items: List<StorageFileItem>,
  val recommendedKeepId: String = items.firstOrNull()?.id ?: ""
) {
  val wastedBytes: Long
    get() = if (items.size > 1) fileSizeBytes * (items.size - 1) else 0L
}

data class AppUsageInfo(
  val packageName: String,
  val appName: String,
  val appSizeBytes: Long,
  val dataSizeBytes: Long,
  val cacheSizeBytes: Long,
  val totalSizeBytes: Long,
  val lastUsedTimestamp: Long, // -1 if unknown / permission not granted
  val daysSinceLastUsed: Int, // -1 if unknown, 0 for today
  val isRarelyUsed: Boolean, // > 30 days or never
  val isSystemApp: Boolean = false,
  val installTimestamp: Long = 0L
)

enum class HealthStatus(val label: String, val color: Color) {
  OPTIMAL("Optimal Storage", EmeraldHealth),
  ATTENTION_NEEDED("Storage Attention", AmberWarning),
  CRITICAL("Low Storage Warning", CoralAlert)
}

enum class AdvisorActionType {
  CLEAN_JUNK,
  REVIEW_DUPLICATES,
  REVIEW_LARGE_FILES,
  OFFLOAD_APPS,
  ENABLE_USAGE_ACCESS
}

enum class AdvisorSeverity {
  HIGH,
  MEDIUM,
  LOW
}

data class AdvisorCard(
  val id: String,
  val title: String,
  val subtitle: String,
  val potentialReclaimBytes: Long,
  val actionType: AdvisorActionType,
  val severity: AdvisorSeverity
)

data class StorageHealthScore(
  val score: Int = 85, // 0 - 100
  val status: HealthStatus = HealthStatus.OPTIMAL,
  val freeSpacePercent: Int = 28,
  val totalReclaimableBytes: Long = 0L,
  val duplicateCount: Int = 0,
  val duplicateWastedBytes: Long = 0L,
  val junkBytes: Long = 0L,
  val unusedAppsCount: Int = 0,
  val unusedAppsBytes: Long = 0L,
  val largeFilesCount: Int = 0,
  val largeFilesTotalBytes: Long = 0L,
  val adviceCards: List<AdvisorCard> = emptyList()
)

enum class NavigationTab(val label: String) {
  OVERVIEW("Storage"),
  CLEANER("Cleaner"),
  APPS("App Usage"),
  HEALTH("Health Score"),
  ADVISOR("Advisor")
}

data class CleanResult(
  val filesDeletedCount: Int,
  val bytesReclaimed: Long,
  val message: String
)
