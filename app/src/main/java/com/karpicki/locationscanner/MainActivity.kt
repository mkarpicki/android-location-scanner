package com.karpicki.locationscanner

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.karpicki.locationscanner.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val requestCode: Int = 100

    private lateinit var binding: ActivityMainBinding
    private var broadcastReceiver: BroadcastReceiver? = null
    private var lastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (!runtimePermissions()) {
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {

                override fun onReceive(p0: Context?, p1: Intent?) {
                    val location = p1?.extras?.get("location") as Location
                    lastLocation = location
                    displayLocation(location)
                }
            }
            registerReceiver(broadcastReceiver, IntentFilter("location_update"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var granted = true

        if (requestCode == this.requestCode) {
            grantResults.forEach { item: Int ->
                if (item != PackageManager.PERMISSION_GRANTED)  {
                    granted = false
                }
            }
        }

        if (granted) {
            start()
        } else {
            runtimePermissions()
        }
    }

    private fun start() {
        binding.mainButtonStart.setOnClickListener {
            val intent = Intent(applicationContext, GPSService::class.java)
            startService(intent)
        }

        binding.mainButtonStop.setOnClickListener {
            val intent = Intent(applicationContext, GPSService::class.java)
            stopService(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayLocation(location: Location) {
        binding.lastLocationText.text = "${location.latitude} ${location.longitude}"+
                "\n" +
                binding.lastLocationText.text
    }

    private fun runtimePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), requestCode)

            return true
        }
        return false
    }
}