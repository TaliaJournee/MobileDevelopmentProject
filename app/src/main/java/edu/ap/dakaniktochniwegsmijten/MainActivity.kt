package edu.ap.dakaniktochniwegsmijten

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import edu.ap.dakaniktochniwegsmijten.ui.map.MapScreen
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().apply {
            load(
                applicationContext,
                getSharedPreferences("osmdroid", MODE_PRIVATE)
            )

            userAgentValue = packageName

            osmdroidBasePath = File(filesDir, "osmdroid").apply {
                mkdirs()
            }

            osmdroidTileCache = File(cacheDir, "osmdroid/tiles").apply {
                mkdirs()
            }
        }

        enableEdgeToEdge()

        setContent {
            MapScreen(
                modifier = Modifier.safeDrawingPadding()
            )
        }
    }
}