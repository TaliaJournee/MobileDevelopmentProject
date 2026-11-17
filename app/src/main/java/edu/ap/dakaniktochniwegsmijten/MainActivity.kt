package edu.ap.dakaniktochniwegsmijten

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import edu.ap.dakaniktochniwegsmijten.ui.navigation.AppNavigation
import org.osmdroid.config.Configuration
import java.io.File
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.ap.dakaniktochniwegsmijten.ui.theme.DaKanIkTochNiWegsmijtenTheme


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
            DaKanIkTochNiWegsmijtenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )

                        AppNavigation(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}