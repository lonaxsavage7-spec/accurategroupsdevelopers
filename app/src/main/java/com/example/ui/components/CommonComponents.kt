package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StorageScannerHelper
import com.example.model.*
import com.example.ui.theme.*

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StorageBreakdownBar(
  breakdown: StorageBreakdown,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    // Segmented bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(18.dp)
        .clip(RoundedCornerShape(9.dp))
        .background(SpaceDarkSurfaceVariant)
    ) {
      Row(modifier = Modifier.fillMaxSize()) {
        val total = breakdown.totalBytes.toFloat().coerceAtLeast(1f)
        val categories = StorageCategory.values()
        categories.forEach { cat ->
          val size = breakdown.categorySizes[cat] ?: 0L
          if (size > 0) {
            val weight = (size.toFloat() / total).coerceAtLeast(0.005f)
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .weight(weight)
                .background(cat.color)
            )
          }
        }
        // Free space
        if (breakdown.freeBytes > 0) {
          val freeWeight = (breakdown.freeBytes.toFloat() / total).coerceAtLeast(0.005f)
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .weight(freeWeight)
              .background(Color(0xFF2E3D5C))
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Legend
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      StorageCategory.values().forEach { cat ->
        val size = breakdown.categorySizes[cat] ?: 0L
        if (size > 0) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(cat.color)
            )
            Text(
              text = "${cat.label}: ${StorageScannerHelper.formatBytes(size)}",
              color = TextMuted,
              fontSize = 11.sp
            )
          }
        }
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFF2E3D5C))
        )
        Text(
          text = "Free: ${StorageScannerHelper.formatBytes(breakdown.freeBytes)}",
          color = TextMuted,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun HealthGauge(
  score: Int,
  status: HealthStatus,
  sizeDp: Dp = 160.dp,
  strokeWidthDp: Dp = 14.dp,
  modifier: Modifier = Modifier
) {
  val animatedProgress by animateFloatAsState(
    targetValue = (score / 100f).coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 1000),
    label = "health_gauge_anim"
  )

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier.size(sizeDp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokePx = strokeWidthDp.toPx()
      val arcSize = size.minDimension - strokePx
      val topLeft = Offset((size.width - arcSize) / 2f, (size.height - arcSize) / 2f)

      // Background track (240 degree sweep from 150 deg to 390 deg)
      drawArc(
        color = Color(0xFF1E2842),
        startAngle = 150f,
        sweepAngle = 240f,
        useCenter = false,
        topLeft = topLeft,
        size = Size(arcSize, arcSize),
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )

      // Colored progress arc
      val sweep = 240f * animatedProgress
      val progressBrush = Brush.sweepGradient(
        0.0f to status.color.copy(alpha = 0.7f),
        1.0f to status.color
      )

      drawArc(
        brush = progressBrush,
        startAngle = 150f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = topLeft,
        size = Size(arcSize, arcSize),
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "$score",
        style = MaterialTheme.typography.displaySmall.copy(
          fontWeight = FontWeight.Bold,
          color = status.color
        )
      )
      Text(
        text = "Score / 100",
        color = TextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(2.dp))
      Surface(
        color = status.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = status.label,
          color = status.color,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }
    }
  }
}

@Composable
fun CelebrationDialog(
  result: CleanResult,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
        modifier = Modifier.testTag("dialog_done_btn")
      ) {
        Text("Done", color = Color(0xFF001F28), fontWeight = FontWeight.Bold)
      }
    },
    icon = {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(EmeraldHealth.copy(alpha = 0.15f))
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Success",
          tint = EmeraldHealth,
          modifier = Modifier.size(36.dp)
        )
      }
    },
    title = {
      Text(
        text = "Space Reclaimed!",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = TextWhitePrimary
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = result.message,
          color = TextMuted,
          fontSize = 14.sp
        )
        if (result.bytesReclaimed > 0) {
          Surface(
            color = SpaceDarkSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Total Freed:", color = TextMuted, fontSize = 13.sp)
              Text(
                StorageScannerHelper.formatBytes(result.bytesReclaimed),
                color = EmeraldHealth,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            }
          }
        }
      }
    },
    containerColor = SpaceDarkSurface,
    shape = RoundedCornerShape(16.dp)
  )
}
