package com.duy.skydodger

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.duy.skydodger.ui.GameScreen
import com.duy.skydodger.ui.SkyDodgerTheme
import com.duy.skydodger.ui.rememberGameHolder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The game is always dark, so both system bars need light icons.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        // A run is a few seconds long; letting the screen sleep in the middle of it is annoying.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            SkyDodgerTheme {
                GameScreen(holder = rememberGameHolder())
            }
        }
    }
}
