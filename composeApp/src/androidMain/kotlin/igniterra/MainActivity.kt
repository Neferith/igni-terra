package igniterra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import igniterra.ui.ManualApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        CrackleSound.start()
        setContent { ManualApp() }
    }

    override fun onDestroy() {
        super.onDestroy()
        CrackleSound.stop()
    }
}