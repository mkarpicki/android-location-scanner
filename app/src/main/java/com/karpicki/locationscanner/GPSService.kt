package com.karpicki.locationscanner

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings

//https://stackoverflow.com/questions/49182661/get-wifi-scan-results-list-with-kotlin
//https://www.youtube.com/watch?v=lvcGh2ZgHeA

class GPSService : Service() {

    private lateinit var locationListener: LocationListener
    private var locationManager: LocationManager? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val intent = Intent("location_update")
                intent.putExtra("location", location)
                sendBroadcast(intent)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
        // permissions enforced in Activity, so check can be skipped
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 0f, locationListener)
        //TODO("change minDistance to 5 (meters)")
        //TODO("pass both minValues from UI")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null) {
            locationManager?.removeUpdates(locationListener)
        }
    }
}