package com.karpicki.locationscanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings

class GPSService : Service() {

    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager

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
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 0f, locationListener)
        //TODO("pass both minValues from UI")
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }
}