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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StorageScannerHelper
import com.example.model.*
import com.example.ui.components.HealthGauge
import com.example.ui.theme.*

@Composable
fun HealthScoreScreen(
  healthScore: StorageHealthScore,
  breakdown: StorageBreakdown,
  onActionSelected: (AdvisorActionType) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Health Score Gauge Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceDarkCard),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("health_score_card")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = "Storage Health Score",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = TextWhitePrimary
            )
          )

          HealthGauge(
            score = healthScore.score,
            status = healthScore.status,
            sizeDp = 180.dp,
            strokeWidthDp = 16.dp
          )

          Text(
            text = when (healthScore.status) {
              HealthStatus.OPTIMAL -> "Your device has ample storage headroom and minimal junk accumulation."
              HealthStatus.ATTENTION_NEEDED -> "Storage is filling up. Review suggested cleanups to keep your phone running fast."
              HealthStatus.CRITICAL -> "Storage is critically low! Immediate cleanup is recommended to avoid performance degradation."
            },
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    }

    // 2. Health Factors Summary Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Health Score Factors",
            color = TextWhitePrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )

          FactorRow(
            label = "Free Space Availability",
            value = "${healthScore.freeSpacePercent}% available",
            isHealthy = healthScore.freeSpacePercent >= 20,
            icon = Icons.Default.PieChart
          )

          FactorRow(
            label = "Duplicate Files Waste",
            value = if (healthScore.duplicateWastedBytes > 0) {
              "-${StorageScannerHelper.formatBytes(healthScore.duplicateWastedBytes)} wasted"
            } else {
              "None detected"
            },
            isHealthy = healthScore.duplicateWastedBytes < 50L * 1024 * 1024,
            icon = Icons.Default.ContentCopy
          )

          FactorRow(
            label = "Barely Used Applications",
            value = if (healthScore.unusedAppsCount > 0) {
              "${healthScore.unusedAppsCount} apps (${StorageScannerHelper.formatBytes(healthScore.unusedAppsBytes)})"
            } else {
              "All apps active"
            },
            isHealthy = healthScore.unusedAppsCount <= 1,
            icon = Icons.Default.Apps
          )

          FactorRow(
            label = "Temporary Caches & Junk",
            value = if (healthScore.junkBytes > 0) {
              "${StorageScannerHelper.formatBytes(healthScore.junkBytes)} removable"
            } else {
              "Clean"
            },
            isHealthy = healthScore.junkBytes < 200L * 1024 * 1024,
            icon = Icons.Default.DeleteSweep
          )
        }
      }
    }

    // 3. Smart Advisor Section Header
    item {
      Column {
        Text(
          text = "Smart Advisor: Decide What to Delete",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextWhitePrimary
          )
        )
        Text(
          text = "Tailored recommendations to intelligently recover storage rather than blindly deleting files.",
          color = TextMuted,
          fontSize = 12.sp
        )
      }
    }

    // 4. Advisor Recommendation Cards
    if (healthScore.adviceCards.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldHealth)
            Text(
              text = "Everything looks clean! No pending storage warnings.",
              color = TextWhitePrimary,
              fontSize = 13.sp
            )
          }
        }
      }
    } else {
      items(healthScore.adviceCards, key = { it.id }) { card ->
        AdvisorDecisionCard(
          card = card,
          onAction = { onActionSelected(card.actionType) }
        )
      }
    }
  }
}

@Composable
private fun FactorRow(
  label: String,
  value: String,
  isHealthy: Boolean,
  icon: ImageVector
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isHealthy) EmeraldHealth else CoralAlert,
        modifier = Modifier.size(16.dp)
      )
      Text(text = label, color = TextMuted, fontSize = 12.sp)
    }
    Text(
      text = value,
      color = if (isHealthy) EmeraldHealth else CoralAlert,
      fontWeight = FontWeight.SemiBold,
      fontSize = 12.sp
    )
  }
}

@Composable
private fun AdvisorDecisionCard(
  card: AdvisorCard,
  onAction: () -> Unit
) {
  val severityColor = when (card.severity) {
    AdvisorSeverity.HIGH -> CoralAlert
    AdvisorSeverity.MEDIUM -> AmberWarning
    AdvisorSeverity.LOW -> CyanAccent
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SpaceDarkSurfaceVariant),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("advisor_card_${card.id}")
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(severityColor)
          )
          Text(
            text = card.title,
            color = TextWhitePrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
        Surface(
          color = severityColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = StorageScannerHelper.formatBytes(card.potentialReclaimBytes),
            color = severityColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
      }

      Text(
        text = card.subtitle,
        color = TextMuted,
        fontSize = 12.sp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        Button(
          onClick = onAction,
          colors = ButtonDefaults.buttonColors(containerColor = severityColor),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier.height(36.dp)
        ) {
          Text(
            text = when (card.actionType) {
              AdvisorActionType.CLEAN_JUNK -> "Purge Junk"
              AdvisorActionType.REVIEW_DUPLICATES -> "Review Duplicates"
              AdvisorActionType.REVIEW_LARGE_FILES -> "Review Files"
              AdvisorActionType.OFFLOAD_APPS -> "Manage Apps"
              AdvisorActionType.ENABLE_USAGE_ACCESS -> "Enable Access"
            },
            color = if (card.severity == AdvisorSeverity.HIGH) Color.White else Color(0xFF001F28),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}
