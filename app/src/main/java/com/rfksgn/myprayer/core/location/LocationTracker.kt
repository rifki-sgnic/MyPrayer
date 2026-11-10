package com.rfksgn.myprayer.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationTracker(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Triple<Double, Double, String>? {
        return try {
            val location = fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await() ?: fusedClient.lastLocation.await()

            if (location != null) {
                val cityName = getCityName(location.latitude, location.longitude)
                Triple(location.latitude, location.longitude, cityName)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getCityName(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val subAdmin = addresses[0].subAdminArea // Kota/Kabupaten
                val locality = addresses[0].locality     // Kecamatan
                listOfNotNull(locality, subAdmin).joinToString(", ").ifEmpty { "Lokasi Terdeteksi" }
            } else "Lokasi GPS"
        } catch (e: Exception) {
            "Koordinat: ${"%.2f".format(lat)}, ${"%.2f".format(lng)}"
        }
    }
}