package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.StorageScannerHelper
import com.example.model.*
import com.example.ui.components.StorageBreakdownBar
import com.example.ui.theme.*

@Composable
fun StorageOverviewScreen(
  breakdown: StorageBreakdown,
  healthScore: StorageHealthScore,
  onNavigateToCleaner: () -> Unit,
  onNavigateToApps: () -> Unit,
  onNavigateToHealth: () -> Unit,
  onDeepScan: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Hero Storage Card with generated illustration
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceDarkCard),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hero_storage_card")
      ) {
        Column {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_storage_hero_1791377006693),
              contentDescription = "SpaceLens Storage Hero",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
            // Gradient overlay
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    1.0f to SpaceDarkCard
                  )
                )
            )

            // Health Score badge in hero
            Surface(
              color = SpaceDarkBg.copy(alpha = 0.85f),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clickable { onNavigateToHealth() }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = "Health",
                  tint = healthScore.status.color,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Health: ${healthScore.score}%",
                  color = healthScore.status.color,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Bottom
            ) {
              Column {
                Text(
                  text = "Device Storage",
                  color = TextMuted,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "${StorageScannerHelper.formatBytes(breakdown.usedBytes)} Used",
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                  )
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Total ${StorageScannerHelper.formatBytes(breakdown.totalBytes)}",
                  color = TextMuted,
                  fontSize = 12.sp
                )
                Text(
                  text = "${StorageScannerHelper.formatBytes(breakdown.freeBytes)} Available",
                  color = CyanAccent,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp
                )
              }
            }

            // Segmented Breakdown Bar
            StorageBreakdownBar(breakdown = breakdown)
          }
        }
      }
    }

    // 2. Low Storage Warning Alert (if applicable)
    if (breakdown.isLowStorage) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CoralAlert.copy(alpha = 0.15f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CoralAlert.copy(alpha = 0.2f))
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = CoralAlert,
                modifier = Modifier.size(24.dp)
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Storage Space Running Low!",
                color = CoralAlert,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "Less than ${(breakdown.percentFree * 100).toInt()}% storage left. Free up space to maintain system speed.",
                color = TextWhitePrimary,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // 3. Quick Clean Action Banner
    if (healthScore.totalReclaimableBytes > 0) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_clean_banner")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Quick Clean Available",
                color = EmeraldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "Reclaim up to ${StorageScannerHelper.formatBytes(healthScore.totalReclaimableBytes)} from junk & duplicate files.",
                color = TextMuted,
                fontSize = 12.sp
              )
            }
            Button(
              onClick = onNavigateToCleaner,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldHealth),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("quick_clean_cta")
            ) {
              Text(
                text = "Clean",
                color = Color(0xFF003919),
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // 4. Category Grid Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Storage Categories",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextWhitePrimary
          )
        )
        TextButton(onClick = onDeepScan) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Scan",
            tint = CyanAccent,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Deep Scan", color = CyanAccent, fontSize = 13.sp)
        }
      }
    }

    // Category Cards
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val categories = listOf(
          Triple(StorageCategory.APPS_DATA, Icons.Default.Apps, onNavigateToApps),
          Triple(StorageCategory.IMAGES, Icons.Default.Image, onNavigateToCleaner),
          Triple(StorageCategory.VIDEOS, Icons.Default.VideoLibrary, onNavigateToCleaner),
          Triple(StorageCategory.AUDIO, Icons.Default.AudioFile, onNavigateToCleaner),
          Triple(StorageCategory.DOCUMENTS, Icons.Default.Description, onNavigateToCleaner),
          Triple(StorageCategory.APKS, Icons.Default.FolderZip, onNavigateToCleaner),
          Triple(StorageCategory.JUNK_CACHE, Icons.Default.DeleteSweep, onNavigateToCleaner),
          Triple(StorageCategory.SYSTEM_OTHER, Icons.Default.SettingsSystemDaydream, {})
        )

        categories.chunked(2).forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            rowItems.forEach { (category, icon, onClick) ->
              val size = breakdown.categorySizes[category] ?: 0L
              CategoryCard(
                category = category,
                icon = icon,
                sizeBytes = size,
                totalBytes = breakdown.totalBytes,
                onClick = onClick,
                modifier = Modifier.weight(1f)
              )
            }
            if (rowItems.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CategoryCard(
  category: StorageCategory,
  icon: ImageVector,
  sizeBytes: Long,
  totalBytes: Long,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val pct = if (totalBytes > 0) (sizeBytes.toFloat() / totalBytes.toFloat() * 100f).toInt() else 0

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
    modifier = modifier
      .clickable { onClick() }
      .testTag("category_card_${category.name.lowercase()}")
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(category.color.copy(alpha = 0.15f))
        ) {
          Icon(
            imageVector = icon,
            contentDescription = category.label,
            tint = category.color,
            modifier = Modifier.size(20.dp)
          )
        }
        Text(
          text = "$pct%",
          color = TextSubtle,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Column {
        Text(
          text = category.label,
          color = TextWhitePrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = StorageScannerHelper.formatBytes(sizeBytes),
          color = TextMuted,
          fontSize = 12.sp
        )
      }
    }
  }
}
