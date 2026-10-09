package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StorageScannerHelper
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class CleanerSubTab(val title: String) {
  JUNK("Junk & Cache"),
  DUPLICATES("Duplicate Files"),
  LARGE_FILES("Large Files")
}

enum class AppFilterOption(val label: String) {
  RARELY_USED("Rarely Used (>30d)"),
  ALL_APPS("All Apps"),
  LARGEST("Largest First"),
  SYSTEM("System Apps")
}

class SpaceLensViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = getApplication<Application>().getSharedPreferences("spacelens_prefs", Context.MODE_PRIVATE)

  private val _isScanning = MutableStateFlow(false)
  val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

  private val _scanProgressMessage = MutableStateFlow("Initializing SpaceLens...")
  val scanProgressMessage: StateFlow<String> = _scanProgressMessage.asStateFlow()

  private val _storageBreakdown = MutableStateFlow(StorageBreakdown())
  val storageBreakdown: StateFlow<StorageBreakdown> = _storageBreakdown.asStateFlow()

  private val _largeFiles = MutableStateFlow<List<StorageFileItem>>(emptyList())
  val largeFiles: StateFlow<List<StorageFileItem>> = _largeFiles.asStateFlow()

  private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
  val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

  private val _junkItems = MutableStateFlow<List<StorageFileItem>>(emptyList())
  val junkItems: StateFlow<List<StorageFileItem>> = _junkItems.asStateFlow()

  private val _appUsageList = MutableStateFlow<List<AppUsageInfo>>(emptyList())
  val appUsageList: StateFlow<List<AppUsageInfo>> = _appUsageList.asStateFlow()

  private val _healthScore = MutableStateFlow(StorageHealthScore())
  val healthScore: StateFlow<StorageHealthScore> = _healthScore.asStateFlow()

  private val _isUsageAccessGranted = MutableStateFlow(false)
  val isUsageAccessGranted: StateFlow<Boolean> = _isUsageAccessGranted.asStateFlow()

  private val _currentTab = MutableStateFlow(NavigationTab.OVERVIEW)
  val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

  private val _cleanerSubTab = MutableStateFlow(CleanerSubTab.JUNK)
  val cleanerSubTab: StateFlow<CleanerSubTab> = _cleanerSubTab.asStateFlow()

  private val _selectedDuplicateIds = MutableStateFlow<Set<String>>(emptySet())
  val selectedDuplicateIds: StateFlow<Set<String>> = _selectedDuplicateIds.asStateFlow()

  private val _selectedJunkIds = MutableStateFlow<Set<String>>(emptySet())
  val selectedJunkIds: StateFlow<Set<String>> = _selectedJunkIds.asStateFlow()

  private val _selectedLargeFileIds = MutableStateFlow<Set<String>>(emptySet())
  val selectedLargeFileIds: StateFlow<Set<String>> = _selectedLargeFileIds.asStateFlow()

  private val _appFilter = MutableStateFlow(AppFilterOption.RARELY_USED)
  val appFilter: StateFlow<AppFilterOption> = _appFilter.asStateFlow()

  private val _appSearchQuery = MutableStateFlow("")
  val appSearchQuery: StateFlow<String> = _appSearchQuery.asStateFlow()

  private val _cleanResult = MutableStateFlow<CleanResult?>(null)
  val cleanResult: StateFlow<CleanResult?> = _cleanResult.asStateFlow()

  // Auto-clean duplicates setting (persisted, defaults to true as requested)
  private val _autoCleanDuplicatesEnabled = MutableStateFlow(
    prefs.getBoolean("key_auto_clean_duplicates", true)
  )
  val autoCleanDuplicatesEnabled: StateFlow<Boolean> = _autoCleanDuplicatesEnabled.asStateFlow()

  // Auto-clean rule (Keep oldest original vs keep newest)
  private val _autoCleanRule = MutableStateFlow(
    try {
      val saved = prefs.getString("key_auto_clean_rule", DuplicateKeepRule.KEEP_OLDEST.name)
      DuplicateKeepRule.valueOf(saved ?: DuplicateKeepRule.KEEP_OLDEST.name)
    } catch (_: Exception) {
      DuplicateKeepRule.KEEP_OLDEST
    }
  )
  val autoCleanRule: StateFlow<DuplicateKeepRule> = _autoCleanRule.asStateFlow()

  init {
    startScan()
  }

  fun setTab(tab: NavigationTab) {
    _currentTab.value = tab
  }

  fun setCleanerSubTab(subTab: CleanerSubTab) {
    _cleanerSubTab.value = subTab
  }

  fun setAppFilter(filter: AppFilterOption) {
    _appFilter.value = filter
  }

  fun setAppSearchQuery(query: String) {
    _appSearchQuery.value = query
  }

  fun dismissCleanResult() {
    _cleanResult.value = null
  }

  fun checkUsageAccess() {
    _isUsageAccessGranted.value = StorageScannerHelper.hasUsageAccess(getApplication())
  }

  fun setAutoCleanDuplicatesEnabled(enabled: Boolean) {
    _autoCleanDuplicatesEnabled.value = enabled
    prefs.edit().putBoolean("key_auto_clean_duplicates", enabled).apply()
  }

  fun setAutoCleanRule(rule: DuplicateKeepRule) {
    _autoCleanRule.value = rule
    prefs.edit().putString("key_auto_clean_rule", rule.name).apply()

    // Re-apply rule to currently displayed duplicate groups
    val updated = _duplicateGroups.value.map { it.withKeepRule(rule) }
    _duplicateGroups.value = updated

    val copyIds = mutableSetOf<String>()
    updated.forEach { group ->
      group.items.forEach { item ->
        if (!item.isDuplicateOriginal) {
          copyIds.add(item.id)
        }
      }
    }
    _selectedDuplicateIds.value = copyIds
  }

  fun startScan() {
    viewModelScope.launch {
      _isScanning.value = true
      val context = getApplication<Application>()
      _isUsageAccessGranted.value = StorageScannerHelper.hasUsageAccess(context)

      _scanProgressMessage.value = "Analyzing storage partitions..."
      delay(200)
      val breakdown = StorageScannerHelper.getDeviceStorageBreakdown(context)
      _storageBreakdown.value = breakdown

      _scanProgressMessage.value = "Scanning file system & media..."
      delay(250)
      val (large, rawDups, junk) = StorageScannerHelper.scanFilesAndJunk(context)
      val currentRule = _autoCleanRule.value
      val dups = rawDups.map { it.withKeepRule(currentRule) }
      _largeFiles.value = large
      _junkItems.value = junk

      // Auto-select all junk items by default
      _selectedJunkIds.value = junk.map { it.id }.toSet()

      // Inspect applications
      _scanProgressMessage.value = "Inspecting installed applications & usage..."
      delay(200)
      val apps = StorageScannerHelper.scanInstalledApps(context)
      _appUsageList.value = apps

      // Check Auto-Clean Duplicates setting
      if (_autoCleanDuplicatesEnabled.value && dups.isNotEmpty()) {
        _scanProgressMessage.value = "⚡ Auto-cleaning ${dups.size} duplicate file groups..."
        delay(250)
        performAutoCleanDuplicates(dups, currentRule, isAutomatic = true)
      } else {
        _duplicateGroups.value = dups
        // Auto-select duplicate non-original copies by default
        val copyIds = mutableSetOf<String>()
        dups.forEach { group ->
          group.items.forEach { item ->
            if (!item.isDuplicateOriginal) {
              copyIds.add(item.id)
            }
          }
        }
        _selectedDuplicateIds.value = copyIds
      }

      _scanProgressMessage.value = "Calculating Storage Health Score..."
      delay(150)
      refreshHealth()

      _isScanning.value = false
    }
  }

  private suspend fun performAutoCleanDuplicates(
    groups: List<DuplicateGroup>,
    rule: DuplicateKeepRule,
    isAutomatic: Boolean = false
  ) {
    if (groups.isEmpty()) return

    var reclaimedBytes = 0L
    var deletedCount = 0

    groups.forEach { rawGroup ->
      val group = rawGroup.withKeepRule(rule)
      group.items.forEach { item ->
        if (!item.isDuplicateOriginal) {
          reclaimedBytes += item.sizeBytes
          deletedCount++
          try {
            val f = File(item.path)
            if (f.exists()) f.delete()
          } catch (_: Exception) {}
        }
      }
    }

    _duplicateGroups.value = emptyList()
    _selectedDuplicateIds.value = emptySet()

    // Update storage breakdown
    val currentBreakdown = _storageBreakdown.value
    val newFree = currentBreakdown.freeBytes + reclaimedBytes
    val newUsed = (currentBreakdown.usedBytes - reclaimedBytes).coerceAtLeast(0L)
    _storageBreakdown.value = currentBreakdown.copy(
      freeBytes = newFree,
      usedBytes = newUsed
    )

    refreshHealth()

    val prefix = if (isAutomatic) "⚡ Auto-Clean Triggered:" else "Cleaned"
    _cleanResult.value = CleanResult(
      filesDeletedCount = deletedCount,
      bytesReclaimed = reclaimedBytes,
      message = "$prefix Safely removed $deletedCount duplicate copies and freed ${StorageScannerHelper.formatBytes(reclaimedBytes)} automatically!"
    )
  }

  fun autoCleanAllDuplicatesNow() {
    viewModelScope.launch {
      performAutoCleanDuplicates(_duplicateGroups.value, _autoCleanRule.value, isAutomatic = false)
    }
  }

  fun toggleDuplicateSelection(id: String) {
    val current = _selectedDuplicateIds.value.toMutableSet()
    if (current.contains(id)) {
      current.remove(id)
    } else {
      current.add(id)
    }
    _selectedDuplicateIds.value = current
  }

  fun selectAllCopiesForGroup(groupId: String, select: Boolean) {
    val group = _duplicateGroups.value.find { it.id == groupId } ?: return
    val current = _selectedDuplicateIds.value.toMutableSet()
    group.items.forEach { item ->
      if (!item.isDuplicateOriginal) {
        if (select) current.add(item.id) else current.remove(item.id)
      }
    }
    _selectedDuplicateIds.value = current
  }

  fun toggleJunkSelection(id: String) {
    val current = _selectedJunkIds.value.toMutableSet()
    if (current.contains(id)) {
      current.remove(id)
    } else {
      current.add(id)
    }
    _selectedJunkIds.value = current
  }

  fun selectAllJunk(select: Boolean) {
    _selectedJunkIds.value = if (select) _junkItems.value.map { it.id }.toSet() else emptySet()
  }

  fun toggleLargeFileSelection(id: String) {
    val current = _selectedLargeFileIds.value.toMutableSet()
    if (current.contains(id)) {
      current.remove(id)
    } else {
      current.add(id)
    }
    _selectedLargeFileIds.value = current
  }

  fun cleanSelectedJunk() {
    viewModelScope.launch {
      val selectedIds = _selectedJunkIds.value
      val itemsToClean = _junkItems.value.filter { selectedIds.contains(it.id) }
      if (itemsToClean.isEmpty()) return@launch

      var reclaimedBytes = 0L
      var deletedCount = 0

      itemsToClean.forEach { item ->
        reclaimedBytes += item.sizeBytes
        deletedCount++
        try {
          val f = File(item.path)
          if (f.exists()) f.delete()
        } catch (_: Exception) {}
      }

      val remaining = _junkItems.value.filterNot { selectedIds.contains(it.id) }
      _junkItems.value = remaining
      _selectedJunkIds.value = emptySet()

      val currentBreakdown = _storageBreakdown.value
      val newFree = currentBreakdown.freeBytes + reclaimedBytes
      val newUsed = (currentBreakdown.usedBytes - reclaimedBytes).coerceAtLeast(0L)
      val newCategorySizes = currentBreakdown.categorySizes.toMutableMap()
      val currentJunk = newCategorySizes[StorageCategory.JUNK_CACHE] ?: 0L
      newCategorySizes[StorageCategory.JUNK_CACHE] = (currentJunk - reclaimedBytes).coerceAtLeast(0L)

      _storageBreakdown.value = currentBreakdown.copy(
        freeBytes = newFree,
        usedBytes = newUsed,
        categorySizes = newCategorySizes
      )

      refreshHealth()

      _cleanResult.value = CleanResult(
        filesDeletedCount = deletedCount,
        bytesReclaimed = reclaimedBytes,
        message = "Successfully cleared ${StorageScannerHelper.formatBytes(reclaimedBytes)} of unnecessary junk!"
      )
    }
  }

  fun deleteSelectedDuplicates() {
    viewModelScope.launch {
      val selectedIds = _selectedDuplicateIds.value
      if (selectedIds.isEmpty()) return@launch

      var reclaimedBytes = 0L
      var deletedCount = 0

      val itemsToDelete = mutableListOf<StorageFileItem>()
      _duplicateGroups.value.forEach { group ->
        group.items.forEach { item ->
          if (selectedIds.contains(item.id)) {
            itemsToDelete.add(item)
          }
        }
      }

      itemsToDelete.forEach { item ->
        reclaimedBytes += item.sizeBytes
        deletedCount++
        try {
          val f = File(item.path)
          if (f.exists()) f.delete()
        } catch (_: Exception) {}
      }

      val remainingGroups = mutableListOf<DuplicateGroup>()
      _duplicateGroups.value.forEach { group ->
        val remainingItems = group.items.filterNot { selectedIds.contains(it.id) }
        if (remainingItems.size > 1) {
          remainingGroups.add(group.copy(items = remainingItems))
        }
      }

      _duplicateGroups.value = remainingGroups
      _selectedDuplicateIds.value = emptySet()

      val currentBreakdown = _storageBreakdown.value
      val newFree = currentBreakdown.freeBytes + reclaimedBytes
      val newUsed = (currentBreakdown.usedBytes - reclaimedBytes).coerceAtLeast(0L)
      _storageBreakdown.value = currentBreakdown.copy(
        freeBytes = newFree,
        usedBytes = newUsed
      )

      refreshHealth()

      _cleanResult.value = CleanResult(
        filesDeletedCount = deletedCount,
        bytesReclaimed = reclaimedBytes,
        message = "Cleaned $deletedCount duplicate copies and freed ${StorageScannerHelper.formatBytes(reclaimedBytes)}!"
      )
    }
  }

  fun deleteSelectedLargeFiles() {
    viewModelScope.launch {
      val selectedIds = _selectedLargeFileIds.value
      if (selectedIds.isEmpty()) return@launch

      var reclaimedBytes = 0L
      var deletedCount = 0

      val itemsToDelete = _largeFiles.value.filter { selectedIds.contains(it.id) }
      itemsToDelete.forEach { item ->
        reclaimedBytes += item.sizeBytes
        deletedCount++
        try {
          val f = File(item.path)
          if (f.exists()) f.delete()
        } catch (_: Exception) {}
      }

      _largeFiles.value = _largeFiles.value.filterNot { selectedIds.contains(it.id) }
      _selectedLargeFileIds.value = emptySet()

      val currentBreakdown = _storageBreakdown.value
      val newFree = currentBreakdown.freeBytes + reclaimedBytes
      val newUsed = (currentBreakdown.usedBytes - reclaimedBytes).coerceAtLeast(0L)
      _storageBreakdown.value = currentBreakdown.copy(
        freeBytes = newFree,
        usedBytes = newUsed
      )

      refreshHealth()

      _cleanResult.value = CleanResult(
        filesDeletedCount = deletedCount,
        bytesReclaimed = reclaimedBytes,
        message = "Removed $deletedCount large files and reclaimed ${StorageScannerHelper.formatBytes(reclaimedBytes)}!"
      )
    }
  }

  fun deleteSingleLargeFile(item: StorageFileItem) {
    viewModelScope.launch {
      try {
        val f = File(item.path)
        if (f.exists()) f.delete()
      } catch (_: Exception) {}

      _largeFiles.value = _largeFiles.value.filterNot { it.id == item.id }

      val currentBreakdown = _storageBreakdown.value
      val newFree = currentBreakdown.freeBytes + item.sizeBytes
      val newUsed = (currentBreakdown.usedBytes - item.sizeBytes).coerceAtLeast(0L)
      _storageBreakdown.value = currentBreakdown.copy(
        freeBytes = newFree,
        usedBytes = newUsed
      )

      refreshHealth()

      _cleanResult.value = CleanResult(
        filesDeletedCount = 1,
        bytesReclaimed = item.sizeBytes,
        message = "Removed ${item.name} and reclaimed ${StorageScannerHelper.formatBytes(item.sizeBytes)}!"
      )
    }
  }

  fun generateTestSandboxFiles(context: Context) {
    viewModelScope.launch {
      val created = StorageScannerHelper.generateTestSandboxFiles(context)
      startScan()
      _cleanResult.value = CleanResult(
        filesDeletedCount = created,
        bytesReclaimed = 0L,
        message = "Generated $created test files (duplicates & cache) in sandbox for testing SpaceLens!"
      )
    }
  }

  private fun refreshHealth() {
    val health = StorageScannerHelper.calculateStorageHealth(
      breakdown = _storageBreakdown.value,
      duplicateGroups = _duplicateGroups.value,
      unusedApps = _appUsageList.value,
      junkItems = _junkItems.value,
      largeFiles = _largeFiles.value
    )
    _healthScore.value = health
  }
}
