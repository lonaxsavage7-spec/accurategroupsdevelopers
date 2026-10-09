package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SpaceLens", appName)
  }

  @Test
  fun `formatBytes formats correctly`() {
    assertEquals("0 B", com.example.data.StorageScannerHelper.formatBytes(0))
    assertEquals("1.5 KB", com.example.data.StorageScannerHelper.formatBytes(1536))
    assertEquals("100 MB", com.example.data.StorageScannerHelper.formatBytes(100L * 1024 * 1024))
    assertEquals("2.5 GB", com.example.data.StorageScannerHelper.formatBytes((2.5 * 1024 * 1024 * 1024).toLong()))
  }

  @Test
  fun `duplicate keep rule marks correct original file`() {
    val item1 = com.example.model.StorageFileItem(
      id = "1", name = "photo.jpg", path = "/a/photo.jpg", sizeBytes = 1000,
      category = com.example.model.StorageCategory.IMAGES, lastModified = 100L
    )
    val item2 = com.example.model.StorageFileItem(
      id = "2", name = "photo.jpg", path = "/b/photo.jpg", sizeBytes = 1000,
      category = com.example.model.StorageCategory.IMAGES, lastModified = 500L
    )
    val group = com.example.model.DuplicateGroup(
      id = "dup_photo", fileName = "photo.jpg", fileSizeBytes = 1000,
      items = listOf(item2, item1)
    )

    val oldestRule = group.withKeepRule(com.example.model.DuplicateKeepRule.KEEP_OLDEST)
    assertEquals("1", oldestRule.items.first { it.isDuplicateOriginal }.id)

    val newestRule = group.withKeepRule(com.example.model.DuplicateKeepRule.KEEP_NEWEST)
    assertEquals("2", newestRule.items.first { it.isDuplicateOriginal }.id)
  }
}
