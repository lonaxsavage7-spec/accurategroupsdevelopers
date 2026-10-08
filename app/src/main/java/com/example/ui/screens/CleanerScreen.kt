package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StorageScannerHelper
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.CleanerSubTab

@Composable
fun CleanerScreen(
  activeSubTab: CleanerSubTab,
  onSubTabChanged: (CleanerSubTab) -> Unit,
  junkItems: List<StorageFileItem>,
  selectedJunkIds: Set<String>,
  onToggleJunk: (String) -> Unit,
  onSelectAllJunk: (Boolean) -> Unit,
  onCleanJunk: () -> Unit,
  duplicateGroups: List<DuplicateGroup>,
  selectedDuplicateIds: Set<String>,
  onToggleDuplicate: (String) -> Unit,
  onCleanDuplicates: () -> Unit,
  largeFiles: List<StorageFileItem>,
  selectedLargeIds: Set<String>,
  onToggleLargeFile: (String) -> Unit,
  onDeleteSelectedLargeFiles: () -> Unit,
  onDeleteSingleLargeFile: (StorageFileItem) -> Unit,
  modifier: Modifier = Modifier
) {
  var fileToDeleteConfirm by remember { mutableStateOf<StorageFileItem?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Segmented Sub-Tabs
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
      SegmentedButton(
        selected = activeSubTab == CleanerSubTab.JUNK,
        onClick = { onSubTabChanged(CleanerSubTab.JUNK) },
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
        colors = SegmentedButtonDefaults.colors(
          activeContainerColor = CyanAccent.copy(alpha = 0.2f),
          activeContentColor = CyanAccent,
          inactiveContainerColor = SpaceDarkSurfaceVariant,
          inactiveContentColor = TextMuted
        ),
        modifier = Modifier.testTag("subtab_junk")
      ) {
        Text("Junk (${junkItems.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }

      SegmentedButton(
        selected = activeSubTab == CleanerSubTab.DUPLICATES,
        onClick = { onSubTabChanged(CleanerSubTab.DUPLICATES) },
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
        colors = SegmentedButtonDefaults.colors(
          activeContainerColor = CyanAccent.copy(alpha = 0.2f),
          activeContentColor = CyanAccent,
          inactiveContainerColor = SpaceDarkSurfaceVariant,
          inactiveContentColor = TextMuted
        ),
        modifier = Modifier.testTag("subtab_duplicates")
      ) {
        Text("Duplicates (${duplicateGroups.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }

      SegmentedButton(
        selected = activeSubTab == CleanerSubTab.LARGE_FILES,
        onClick = { onSubTabChanged(CleanerSubTab.LARGE_FILES) },
        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
        colors = SegmentedButtonDefaults.colors(
          activeContainerColor = CyanAccent.copy(alpha = 0.2f),
          activeContentColor = CyanAccent,
          inactiveContainerColor = SpaceDarkSurfaceVariant,
          inactiveContentColor = TextMuted
        ),
        modifier = Modifier.testTag("subtab_large_files")
      ) {
        Text("Large (${largeFiles.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Content based on sub-tab
    Box(modifier = Modifier.weight(1f)) {
      when (activeSubTab) {
        CleanerSubTab.JUNK -> {
          JunkCleanerContent(
            items = junkItems,
            selectedIds = selectedJunkIds,
            onToggle = onToggleJunk,
            onSelectAll = onSelectAllJunk,
            onClean = onCleanJunk
          )
        }
        CleanerSubTab.DUPLICATES -> {
          DuplicatesCleanerContent(
            groups = duplicateGroups,
            selectedIds = selectedDuplicateIds,
            onToggle = onToggleDuplicate,
            onClean = onCleanDuplicates
          )
        }
        CleanerSubTab.LARGE_FILES -> {
          LargeFilesCleanerContent(
            files = largeFiles,
            selectedIds = selectedLargeIds,
            onToggle = onToggleLargeFile,
            onDeleteSelected = onDeleteSelectedLargeFiles,
            onRequestDeleteSingle = { fileToDeleteConfirm = it }
          )
        }
      }
    }
  }

  // Confirmation dialog for single large file deletion
  fileToDeleteConfirm?.let { file ->
    AlertDialog(
      onDismissRequest = { fileToDeleteConfirm = null },
      title = { Text("Delete Large File?", color = TextWhitePrimary) },
      text = {
        Text(
          "Are you sure you want to permanently delete '${file.name}' (${StorageScannerHelper.formatBytes(file.sizeBytes)})?",
          color = TextMuted
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteSingleLargeFile(file)
            fileToDeleteConfirm = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CoralAlert)
        ) {
          Text("Delete", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { fileToDeleteConfirm = null }) {
          Text("Cancel", color = TextMuted)
        }
      },
      containerColor = SpaceDarkSurface
    )
  }
}

@Composable
private fun JunkCleanerContent(
  items: List<StorageFileItem>,
  selectedIds: Set<String>,
  onToggle: (String) -> Unit,
  onSelectAll: (Boolean) -> Unit,
  onClean: () -> Unit
) {
  val selectedBytes = items.filter { selectedIds.contains(it.id) }.sumOf { it.sizeBytes }
  val allSelected = items.isNotEmpty() && selectedIds.size == items.size

  if (items.isEmpty()) {
    EmptyCleanState(
      title = "No Junk Detected!",
      description = "Your system caches and temporary folders are clean and optimized."
    )
    return
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Header with Select All
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Select items to safely purge",
        color = TextMuted,
        fontSize = 13.sp
      )
      TextButton(onClick = { onSelectAll(!allSelected) }) {
        Text(
          text = if (allSelected) "Deselect All" else "Select All",
          color = CyanAccent,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      items(items, key = { it.id }) { item ->
        val isSelected = selectedIds.contains(item.id)
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SpaceDarkSurfaceVariant else SpaceDarkCard
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(item.id) }
            .testTag("junk_item_${item.id.hashCode()}")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Checkbox(
              checked = isSelected,
              onCheckedChange = { onToggle(item.id) },
              colors = CheckboxDefaults.colors(
                checkedColor = CyanAccent,
                checkmarkColor = Color(0xFF001F28),
                uncheckedColor = TextSubtle
              )
            )

            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(ColorJunk.copy(alpha = 0.15f))
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = ColorJunk,
                modifier = Modifier.size(20.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.name,
                color = TextWhitePrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = item.junkType?.description ?: item.path,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Text(
              text = StorageScannerHelper.formatBytes(item.sizeBytes),
              color = EmeraldHealth,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }

    // Bottom Action Button
    Button(
      onClick = onClean,
      enabled = selectedIds.isNotEmpty(),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = EmeraldHealth,
        disabledContainerColor = SpaceDarkSurfaceVariant
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .height(52.dp)
        .testTag("clean_junk_btn")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.CleaningServices,
          contentDescription = null,
          tint = if (selectedIds.isNotEmpty()) Color(0xFF003919) else TextMuted
        )
        Text(
          text = if (selectedIds.isNotEmpty()) {
            "Clean ${selectedIds.size} Items (${StorageScannerHelper.formatBytes(selectedBytes)})"
          } else {
            "Select Items to Clean"
          },
          color = if (selectedIds.isNotEmpty()) Color(0xFF003919) else TextMuted,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
    }
  }
}

@Composable
private fun DuplicatesCleanerContent(
  groups: List<DuplicateGroup>,
  selectedIds: Set<String>,
  onToggle: (String) -> Unit,
  onClean: () -> Unit
) {
  val selectedBytes = groups.flatMap { it.items }
    .filter { selectedIds.contains(it.id) }
    .sumOf { it.sizeBytes }

  if (groups.isEmpty()) {
    EmptyCleanState(
      title = "No Duplicate Files Found!",
      description = "SpaceLens found no duplicate videos, photos, or documents wasting storage."
    )
    return
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Header Banner
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = IndigoPrimary.copy(alpha = 0.15f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = null,
          tint = CyanAccent,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = "Smart Recommendation: Keep the original copy and delete identical duplicates.",
          color = TextWhitePrimary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      items(groups, key = { it.id }) { group ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = group.fileName,
                  color = TextWhitePrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${group.items.size} copies (${StorageScannerHelper.formatBytes(group.fileSizeBytes)} each)",
                  color = TextMuted,
                  fontSize = 12.sp
                )
              }
              Surface(
                color = CoralAlert.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "Waste: ${StorageScannerHelper.formatBytes(group.wastedBytes)}",
                  color = CoralAlert,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

            // Duplicate items in group
            group.items.forEachIndexed { index, item ->
              val isSelected = selectedIds.contains(item.id)
              val isOriginal = item.isDuplicateOriginal

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) SpaceDarkCard else Color.Transparent)
                  .clickable { if (!isOriginal) onToggle(item.id) }
                  .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                if (isOriginal) {
                  Surface(
                    color = EmeraldHealth.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = "KEEP ORIGINAL",
                      color = EmeraldLight,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }
                } else {
                  Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle(item.id) },
                    colors = CheckboxDefaults.colors(
                      checkedColor = CoralAlert,
                      checkmarkColor = Color.White,
                      uncheckedColor = TextSubtle
                    )
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isOriginal) "Original File" else "Duplicate Copy #${index}",
                    color = if (isOriginal) EmeraldLight else TextWhitePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                  Text(
                    text = item.path,
                    color = TextSubtle,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }
    }

    // Clean Button
    Button(
      onClick = onClean,
      enabled = selectedIds.isNotEmpty(),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CoralAlert,
        disabledContainerColor = SpaceDarkSurfaceVariant
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .height(52.dp)
        .testTag("clean_duplicates_btn")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = null,
          tint = if (selectedIds.isNotEmpty()) Color.White else TextMuted
        )
        Text(
          text = if (selectedIds.isNotEmpty()) {
            "Delete ${selectedIds.size} Duplicate Copies (${StorageScannerHelper.formatBytes(selectedBytes)})"
          } else {
            "Select Copies to Delete"
          },
          color = if (selectedIds.isNotEmpty()) Color.White else TextMuted,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }
    }
  }
}

@Composable
private fun LargeFilesCleanerContent(
  files: List<StorageFileItem>,
  selectedIds: Set<String>,
  onToggle: (String) -> Unit,
  onDeleteSelected: () -> Unit,
  onRequestDeleteSingle: (StorageFileItem) -> Unit
) {
  val selectedBytes = files.filter { selectedIds.contains(it.id) }.sumOf { it.sizeBytes }

  if (files.isEmpty()) {
    EmptyCleanState(
      title = "No Very Large Files",
      description = "No single files exceeding 25 MB were detected."
    )
    return
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${files.size} files larger than 25 MB",
        color = TextMuted,
        fontSize = 13.sp
      )
      if (selectedIds.isNotEmpty()) {
        TextButton(onClick = onDeleteSelected) {
          Text(
            "Delete Selected (${StorageScannerHelper.formatBytes(selectedBytes)})",
            color = CoralAlert,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      items(files, key = { it.id }) { file ->
        val isSelected = selectedIds.contains(file.id)

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Checkbox(
              checked = isSelected,
              onCheckedChange = { onToggle(file.id) },
              colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
            )

            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(file.category.color.copy(alpha = 0.15f))
            ) {
              Icon(
                imageVector = when (file.category) {
                  StorageCategory.VIDEOS -> Icons.Default.Movie
                  StorageCategory.DOCUMENTS -> Icons.Default.Description
                  StorageCategory.APKS -> Icons.Default.FolderZip
                  else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = file.category.color,
                modifier = Modifier.size(22.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = file.name,
                color = TextWhitePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = file.path,
                color = TextSubtle,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = StorageScannerHelper.formatBytes(file.sizeBytes),
                color = CyanAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              IconButton(
                onClick = { onRequestDeleteSingle(file) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Delete",
                  tint = TextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyCleanState(title: String, description: String) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(24.dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(EmeraldHealth.copy(alpha = 0.15f))
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircleOutline,
          contentDescription = null,
          tint = EmeraldHealth,
          modifier = Modifier.size(36.dp)
        )
      }
      Text(
        text = title,
        color = TextWhitePrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp
      )
      Text(
        text = description,
        color = TextMuted,
        fontSize = 13.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  }
}
