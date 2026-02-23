package com.patrick.flowtask

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.patrick.flowtask.app.AppContainer
import com.patrick.flowtask.app.FlowTaskApp
import com.patrick.flowtask.ui.theme.FlowTaskTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
        enableEdgeToEdge()
        setContent {
            FlowTaskTheme {
                FlowTaskApp(container = appContainer)
            }
        }
    }
}