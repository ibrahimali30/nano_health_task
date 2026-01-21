package com.ibrahim.nano_health_task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.ibrahim.nano_health_task.feed.ui.FeedScreen
import com.ibrahim.nano_health_task.feed.ui.FeedViewModel
import com.ibrahim.nano_health_task.ui.theme.Nano_health_taskTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nano_health_taskTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val vm: FeedViewModel = hiltViewModel()
                    FeedScreen(viewModel = vm, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Nano_health_taskTheme {
        // preview: no viewmodel available here; show a stub
    }
}