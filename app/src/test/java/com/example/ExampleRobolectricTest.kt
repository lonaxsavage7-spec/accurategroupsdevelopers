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
}
