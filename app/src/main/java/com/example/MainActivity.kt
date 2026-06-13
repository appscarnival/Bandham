package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.BandhamApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BandhamViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BandhamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BandhamApp(viewModel)
            }
        }
    }
}
