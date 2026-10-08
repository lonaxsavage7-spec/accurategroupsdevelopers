package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StorageScannerHelper
import com.example.model.AppUsageInfo
import com.example.ui.theme.*
import com.example.viewmodel.AppFilterOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(
  apps: List<AppUsageInfo>,
  isUsageAccessGranted: Boolean,
  activeFilter: AppFilterOption,
  onFilterSelected: (AppFilterOption) -> Unit,
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  onCheckUsageAccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Filter apps
  val filteredApps = remember(apps, activeFilter, searchQuery) {
    apps.filter { app ->
      val matchesSearch = searchQuery.isBlank() ||
        app.appName.contains(searchQuery, ignoreCase = true) ||
        app.packageName.contains(searchQuery, ignoreCase = true)

      val matchesFilter = when (activeFilter) {
        AppFilterOption.RARELY_USED -> !app.isSystemApp && (app.isRarelyUsed || app.daysSinceLastUsed >= 30)
        AppFilterOption.ALL_APPS -> !app.isSystemApp
        AppFilterOption.LARGEST -> !app.isSystemApp
        AppFilterOption.SYSTEM -> app.isSystemApp
      }

      matchesSearch && matchesFilter
    }.let { list ->
      if (activeFilter == AppFilterOption.LARGEST) {
        list.sortedByDescending { it.totalSizeBytes }
      } else {
        list
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // 1. Usage Access Banner if not granted
    if (!isUsageAccessGranted) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.15f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("usage_access_banner")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = AmberWarning,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Enable Exact Last-Used Tracking",
              color = AmberWarning,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
          Text(
            text = "Grant Usage Access in Android Settings to let SpaceLens show exact last-used dates and detect apps you barely use.",
            color = TextWhitePrimary,
            fontSize = 12.sp
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = {
                StorageScannerHelper.openUsageAccessSettings(context)
                onCheckUsageAccess()
              },
              colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Open Settings", color = Color(0xFF332000), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    // 2. Search Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChanged,
      placeholder = { Text("Search installed apps...", color = TextMuted, fontSize = 13.sp) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchQueryChanged("") }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyanAccent,
        unfocusedBorderColor = BorderSubtle,
        focusedContainerColor = SpaceDarkSurfaceVariant,
        unfocusedContainerColor = SpaceDarkSurfaceVariant
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("app_search_field")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 3. Filter Chips Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      AppFilterOption.values().forEach { option ->
        val selected = activeFilter == option
        FilterChip(
          selected = selected,
          onClick = { onFilterSelected(option) },
          label = {
            Text(
              text = option.label,
              fontSize = 11.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
            selectedLabelColor = CyanAccent,
            containerColor = SpaceDarkSurfaceVariant,
            labelColor = TextMuted
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) CyanAccent else BorderSubtle
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 4. Apps List Header
    Text(
      text = "${filteredApps.size} apps found",
      color = TextMuted,
      fontSize = 12.sp,
      modifier = Modifier.padding(bottom = 6.dp)
    )

    // 5. Apps List
    if (filteredApps.isEmpty()) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        Text("No applications matching current filter.", color = TextMuted, fontSize = 14.sp)
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
      ) {
        items(filteredApps, key = { it.packageName }) { app ->
          AppUsageCard(
            app = app,
            onOpenDetails = { StorageScannerHelper.openAppDetailsSettings(context, app.packageName) },
            onUninstall = { StorageScannerHelper.requestUninstallApp(context, app.packageName) }
          )
        }
      }
    }
  }
}

@Composable
private fun AppUsageCard(
  app: AppUsageInfo,
  onOpenDetails: () -> Unit,
  onUninstall: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("app_card_${app.packageName.replace('.', '_')}")
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // App Icon Placeholder / Badge
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(IndigoPrimary.copy(alpha = 0.2f))
        ) {
          Icon(
            imageVector = Icons.Default.Android,
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(26.dp)
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = app.appName,
            color = TextWhitePrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = app.packageName,
            color = TextSubtle,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Total Size
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = StorageScannerHelper.formatBytes(app.totalSizeBytes),
            color = CyanAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "App: ${StorageScannerHelper.formatBytes(app.appSizeBytes)}",
            color = TextSubtle,
            fontSize = 10.sp
          )
        }
      }

      // Last Used & Status Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val lastUsedLabel = when {
          app.daysSinceLastUsed == 0 -> "Used Today"
          app.daysSinceLastUsed in 1..7 -> "Used ${app.daysSinceLastUsed}d ago"
          app.daysSinceLastUsed > 7 && app.daysSinceLastUsed < 30 -> "Used ${app.daysSinceLastUsed}d ago"
          app.daysSinceLastUsed >= 30 -> "Idle for ${app.daysSinceLastUsed} days"
          else -> "Not opened recently"
        }

        val badgeColor = when {
          app.isRarelyUsed -> CoralAlert
          app.daysSinceLastUsed in 0..7 -> EmeraldHealth
          else -> AmberWarning
        }

        Surface(
          color = badgeColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = if (app.isRarelyUsed) Icons.Default.Warning else Icons.Default.AccessTime,
              contentDescription = null,
              tint = badgeColor,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = lastUsedLabel,
              color = badgeColor,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = onOpenDetails,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text("Storage Info", fontSize = 11.sp, color = TextWhitePrimary)
          }

          if (!app.isSystemApp) {
            Button(
              onClick = onUninstall,
              colors = ButtonDefaults.buttonColors(containerColor = CoralAlert.copy(alpha = 0.2f)),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Text("Uninstall", fontSize = 11.sp, color = CoralAlert, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}
