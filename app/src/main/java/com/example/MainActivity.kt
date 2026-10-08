package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.SpaceLensApp
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.SpaceLensTheme
import com.example.viewmodel.SpaceLensViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: SpaceLensViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      SpaceLensTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = SpaceDarkBg
        ) {
          SpaceLensApp(viewModel = viewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.checkUsageAccess()
  }
}
