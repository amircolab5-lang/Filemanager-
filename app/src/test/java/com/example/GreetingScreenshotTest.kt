package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.StorageStats
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun homeScreen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = false) {
        HomeScreen(
          storageStats = StorageStats(
            totalBytes = 64L * 1024 * 1024 * 1024,
            usedBytes = 28L * 1024 * 1024 * 1024,
            freeBytes = 36L * 1024 * 1024 * 1024
          ),
          hasPermission = true,
          categoryCounts = emptyMap(),
          whatsAppStatusCount = 0,
          recentDocuments = emptyList(),
          onRequestPermission = {},
          onCategoryClick = {},
          onMoreClick = {},
          onOpenAllFiles = {},
          onOpenCleaner = {},
          onOpenFile = {},
          onSearchClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

