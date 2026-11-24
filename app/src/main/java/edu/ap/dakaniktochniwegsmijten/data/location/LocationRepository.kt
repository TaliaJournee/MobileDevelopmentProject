package edu.ap.dakaniktochniwegsmijten.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationRepository(context: Context) {

    private val appContext = context.applicationContext

    suspend fun getCurrentLocation(): Location? {
        val manager = appContext.getSystemService(LocationManager::class.java)
            ?: return null

        val hasFineAccess = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseAccess = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineAccess && !hasCoarseAccess) return null

        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            if (hasFineAccess) add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
        }

        for (provider in providers) {
            try {
                if (!manager.isProviderEnabled(provider)) continue

                val location = withTimeoutOrNull(10_000L) {
                    requestLocation(manager, provider)
                }

                if (location != null) return location
            } catch (_: SecurityException) {
                // Permission may have changed since the initial check.
                return null
            } catch (_: IllegalArgumentException) {
                // This provider may be unavailable on the device.
                continue
            }
        }

        return null
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    private suspend fun requestLocation(
        manager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()

        LocationManagerCompat.getCurrentLocation(
            manager,
            provider,
            signal,
            ContextCompat.getMainExecutor(appContext)
        ) { location ->
            continuation.resume(location)
        }

        continuation.invokeOnCancellation {
            signal.cancel()
        }
    }
}