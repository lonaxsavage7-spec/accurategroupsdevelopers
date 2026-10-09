package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AdvisorActionType
import com.example.model.NavigationTab
import com.example.ui.components.CelebrationDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.CleanerSubTab
import com.example.viewmodel.SpaceLensViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceLensApp(
  viewModel: SpaceLensViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
  val scanProgressMessage by viewModel.scanProgressMessage.collectAsStateWithLifecycle()
  val storageBreakdown by viewModel.storageBreakdown.collectAsStateWithLifecycle()
  val healthScore by viewModel.healthScore.collectAsStateWithLifecycle()
  val cleanResult by viewModel.cleanResult.collectAsStateWithLifecycle()

  val junkItems by viewModel.junkItems.collectAsStateWithLifecycle()
  val selectedJunkIds by viewModel.selectedJunkIds.collectAsStateWithLifecycle()
  val duplicateGroups by viewModel.duplicateGroups.collectAsStateWithLifecycle()
  val selectedDuplicateIds by viewModel.selectedDuplicateIds.collectAsStateWithLifecycle()
  val largeFiles by viewModel.largeFiles.collectAsStateWithLifecycle()
  val selectedLargeIds by viewModel.selectedLargeFileIds.collectAsStateWithLifecycle()
  val cleanerSubTab by viewModel.cleanerSubTab.collectAsStateWithLifecycle()

  val appUsageList by viewModel.appUsageList.collectAsStateWithLifecycle()
  val isUsageAccessGranted by viewModel.isUsageAccessGranted.collectAsStateWithLifecycle()
  val appFilter by viewModel.appFilter.collectAsStateWithLifecycle()
  val appSearchQuery by viewModel.appSearchQuery.collectAsStateWithLifecycle()
  val autoCleanDuplicatesEnabled by viewModel.autoCleanDuplicatesEnabled.collectAsStateWithLifecycle()
  val autoCleanRule by viewModel.autoCleanRule.collectAsStateWithLifecycle()

  var showOptionsMenu by remember { mutableStateOf(false) }

  // Handle hardware back press
  BackHandler(enabled = currentTab != NavigationTab.OVERVIEW) {
    viewModel.setTab(NavigationTab.OVERVIEW)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CyanAccent.copy(alpha = 0.2f))
            ) {
              Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(18.dp)
              )
            }
            Text(
              text = "SpaceLens",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
              )
            )
          }
        },
        actions = {
          // Health score pill
          Surface(
            color = healthScore.status.color.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(end = 4.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(healthScore.status.color)
              )
              Text(
                text = "${healthScore.score}%",
                color = healthScore.status.color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }

          // Scan button
          IconButton(
            onClick = { viewModel.startScan() },
            modifier = Modifier.testTag("top_scan_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Scan Storage",
              tint = CyanAccent
            )
          }

          // More Options Menu (Demo test data)
          Box {
            IconButton(onClick = { showOptionsMenu = true }) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = TextWhitePrimary
              )
            }
            DropdownMenu(
              expanded = showOptionsMenu,
              onDismissRequest = { showOptionsMenu = false },
              modifier = Modifier.background(SpaceDarkSurface)
            ) {
              DropdownMenuItem(
                text = {
                  Text(
                    text = "Auto-Clean Duplicates: ${if (autoCleanDuplicatesEnabled) "ON" else "OFF"}",
                    color = TextWhitePrimary,
                    fontSize = 13.sp
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (autoCleanDuplicatesEnabled) EmeraldHealth else TextMuted
                  )
                },
                onClick = {
                  showOptionsMenu = false
                  viewModel.setAutoCleanDuplicatesEnabled(!autoCleanDuplicatesEnabled)
                }
              )
              DropdownMenuItem(
                text = { Text("Auto-Clean Duplicates Now", color = TextWhitePrimary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CyanAccent) },
                onClick = {
                  showOptionsMenu = false
                  viewModel.autoCleanAllDuplicatesNow()
                }
              )
              DropdownMenuItem(
                text = { Text("Generate Test Sandbox Data", color = TextWhitePrimary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = CyanAccent) },
                onClick = {
                  showOptionsMenu = false
                  viewModel.generateTestSandboxFiles(context)
                }
              )
              DropdownMenuItem(
                text = { Text("Deep Storage Rescan", color = TextWhitePrimary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = EmeraldHealth) },
                onClick = {
                  showOptionsMenu = false
                  viewModel.startScan()
                }
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = SpaceDarkBg,
          titleContentColor = TextWhitePrimary
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = SpaceDarkSurface,
        contentColor = TextWhitePrimary,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        val navItems = listOf(
          Triple(NavigationTab.OVERVIEW, "Storage", Icons.Default.PieChart),
          Triple(NavigationTab.CLEANER, "Cleaner", Icons.Default.CleaningServices),
          Triple(NavigationTab.APPS, "Apps", Icons.Default.Apps),
          Triple(NavigationTab.HEALTH, "Health", Icons.Default.Shield)
        )

        navItems.forEach { (tab, label, icon) ->
          val selected = currentTab == tab
          NavigationBarItem(
            selected = selected,
            onClick = { viewModel.setTab(tab) },
            icon = {
              Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) CyanAccent else TextMuted
              )
            },
            label = {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) CyanAccent else TextMuted
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = CyanAccent.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
          )
        }
      }
    },
    containerColor = SpaceDarkBg,
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentTab) {
        NavigationTab.OVERVIEW -> {
          StorageOverviewScreen(
            breakdown = storageBreakdown,
            healthScore = healthScore,
            onNavigateToCleaner = { viewModel.setTab(NavigationTab.CLEANER) },
            onNavigateToApps = { viewModel.setTab(NavigationTab.APPS) },
            onNavigateToHealth = { viewModel.setTab(NavigationTab.HEALTH) },
            onDeepScan = { viewModel.startScan() }
          )
        }
        NavigationTab.CLEANER -> {
          CleanerScreen(
            activeSubTab = cleanerSubTab,
            onSubTabChanged = { viewModel.setCleanerSubTab(it) },
            junkItems = junkItems,
            selectedJunkIds = selectedJunkIds,
            onToggleJunk = { viewModel.toggleJunkSelection(it) },
            onSelectAllJunk = { viewModel.selectAllJunk(it) },
            onCleanJunk = { viewModel.cleanSelectedJunk() },
            duplicateGroups = duplicateGroups,
            selectedDuplicateIds = selectedDuplicateIds,
            onToggleDuplicate = { viewModel.toggleDuplicateSelection(it) },
            onCleanDuplicates = { viewModel.deleteSelectedDuplicates() },
            autoCleanDuplicatesEnabled = autoCleanDuplicatesEnabled,
            onToggleAutoCleanDuplicates = { viewModel.setAutoCleanDuplicatesEnabled(it) },
            autoCleanRule = autoCleanRule,
            onRuleChanged = { viewModel.setAutoCleanRule(it) },
            onAutoCleanAllNow = { viewModel.autoCleanAllDuplicatesNow() },
            largeFiles = largeFiles,
            selectedLargeIds = selectedLargeIds,
            onToggleLargeFile = { viewModel.toggleLargeFileSelection(it) },
            onDeleteSelectedLargeFiles = { viewModel.deleteSelectedLargeFiles() },
            onDeleteSingleLargeFile = { viewModel.deleteSingleLargeFile(it) }
          )
        }
        NavigationTab.APPS -> {
          AppUsageScreen(
            apps = appUsageList,
            isUsageAccessGranted = isUsageAccessGranted,
            activeFilter = appFilter,
            onFilterSelected = { viewModel.setAppFilter(it) },
            searchQuery = appSearchQuery,
            onSearchQueryChanged = { viewModel.setAppSearchQuery(it) },
            onCheckUsageAccess = { viewModel.checkUsageAccess() }
          )
        }
        NavigationTab.HEALTH, NavigationTab.ADVISOR -> {
          HealthScoreScreen(
            healthScore = healthScore,
            breakdown = storageBreakdown,
            onActionSelected = { action ->
              when (action) {
                AdvisorActionType.CLEAN_JUNK -> {
                  viewModel.setTab(NavigationTab.CLEANER)
                  viewModel.setCleanerSubTab(CleanerSubTab.JUNK)
                }
                AdvisorActionType.REVIEW_DUPLICATES -> {
                  viewModel.setTab(NavigationTab.CLEANER)
                  viewModel.setCleanerSubTab(CleanerSubTab.DUPLICATES)
                }
                AdvisorActionType.REVIEW_LARGE_FILES -> {
                  viewModel.setTab(NavigationTab.CLEANER)
                  viewModel.setCleanerSubTab(CleanerSubTab.LARGE_FILES)
                }
                AdvisorActionType.OFFLOAD_APPS -> {
                  viewModel.setTab(NavigationTab.APPS)
                }
                AdvisorActionType.ENABLE_USAGE_ACCESS -> {
                  viewModel.setTab(NavigationTab.APPS)
                }
              }
            }
          )
        }
      }

      // Scanning indicator overlay
      AnimatedVisibility(
        visible = isScanning,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg.copy(alpha = 0.85f))
        ) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceDarkSurface),
            modifier = Modifier.padding(32.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.padding(24.dp)
            ) {
              CircularProgressIndicator(
                color = CyanAccent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
              )
              Text(
                text = "Scanning Storage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextWhitePrimary
              )
              Text(
                text = scanProgressMessage,
                color = TextMuted,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }

  // Celebration dialog upon cleaning
  cleanResult?.let { result ->
    CelebrationDialog(
      result = result,
      onDismiss = { viewModel.dismissCleanResult() }
    )
  }
}
