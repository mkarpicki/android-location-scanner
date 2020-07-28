package com.karpicki.locationscanner

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.wifi.ScanResult as WIFIScanResult
import android.bluetooth.le.ScanResult as BTScanResult
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.karpicki.locationscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val requestCode: Int = 100

    private var wifiSaveSuccessCount = 0;
    private var wifiSaveFailureCount = 0;

    private var btSaveSuccessCount = 0;
    private var btSaveFailureCount = 0;

    private lateinit var binding: ActivityMainBinding
    private var broadcastReceiver: BroadcastReceiver? = null

    private var lastLocation: Location? = null;
    private var lastWifiList: ArrayList<WIFIScanResult> = ArrayList();
    private var lastBTDevices: ArrayList<BTScanResult> = ArrayList();

    private lateinit var listOfIgnoredAddresses: Array<String>;

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
        prepareReceiver()
    }

    private fun prepareReceiver() {
        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {

                override fun onReceive(p0: Context?, intent: Intent?) {
                    handleIntents(intent)
                }
            }

            val intentFilter = IntentFilter()
            intentFilter.addAction("location_update")
            intentFilter.addAction("wifi_scan_update")
            intentFilter.addAction("bt_scan_update")
            intentFilter.addAction("wifi_save_status")
            intentFilter.addAction("bt_save_status")

            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    private fun handleIntents(intent: Intent?) {
        when (intent?.action) {
            "wifi_save_status" -> {
                val status = intent.extras?.get("status") as Int
                if (status == 201) {
                    wifiSaveSuccessCount++;
                } else {
                    wifiSaveFailureCount++;
                }
            }
            "bt_save_status" -> {
                val status = intent.extras?.get("status") as Int
                if (status == 201) {
                    btSaveSuccessCount++;
                } else {
                    btSaveFailureCount++;
                }
            }
            "location_update" -> {
                val location = intent.extras?.get("location") as Location
                lastLocation = location
                displayLocation(lastLocation as Location)
            }
            "wifi_scan_update" -> {
                lastWifiList.clear()
                val wifiList = intent.extras?.get("wifi_results") as ArrayList<*>
                wifiList.forEach { wifiNetwork -> lastWifiList.add(wifiNetwork as WIFIScanResult) }
                displayWIFINetworks(lastWifiList)
            }
            "bt_scan_update" -> {
                val btList = intent.extras?.get("bt_results") as ArrayList<*>

                if (lastBTDevices.size > 10) {
                    lastBTDevices.clear()
                }

                btList.forEach { btDevice ->
                    val item = btDevice as BTScanResult
                    val foundBtDevice: BTScanResult? = lastBTDevices.find {
                        it.device.address == item.device.address
                    }
                    if (foundBtDevice == null) {
                        lastBTDevices.add(btDevice)
                    }
                }
                displayBTDevices(lastBTDevices)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseReceiver()
    }

    private fun releaseReceiver() {
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

    private fun updateListOfIgnoredDevices() {

        val ignoredListLoaderTask = IgnoredListLoaderTask()
        ignoredListLoaderTask.setContext(this)
        val responseStr = ignoredListLoaderTask.execute().get()

        this.listOfIgnoredAddresses = ignoredListLoaderTask.parse(responseStr)

        //Toast.makeText(this,R.string.ignored_list_not_loaded,Toast.LENGTH_LONG).show()
    }

    private fun start() {

        updateListOfIgnoredDevices()

        binding.mainButtonStart.setOnClickListener {
            val gpsIntent = Intent(applicationContext, GPSService::class.java)
            startService(gpsIntent)

            val wifiIntent = Intent(applicationContext, WIFIService::class.java)
            wifiIntent.putExtra("addressesToIgnore", listOfIgnoredAddresses)
            startService(wifiIntent)

            val btIntent = Intent(applicationContext, BTService::class.java)
            btIntent.putExtra("addressesToIgnore", this.listOfIgnoredAddresses)
            startService(btIntent)

            val syncIntent = Intent(applicationContext, SyncService::class.java)
            startService(syncIntent)
        }

        binding.mainButtonStop.setOnClickListener {
            val gpsIntent = Intent(applicationContext, GPSService::class.java)
            stopService(gpsIntent)

            val wifiIntent = Intent(applicationContext, WIFIService::class.java)
            stopService(wifiIntent)

            val btIntent = Intent(applicationContext, BTService::class.java)
            stopService(btIntent)

            val syncIntent = Intent(applicationContext, SyncService::class.java)
            stopService(syncIntent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayLocation(location: Location) {
        binding.lastLocationText.text = "${location.latitude} ${location.longitude}"
    }

    private fun displayWIFINetworks(list: ArrayList<WIFIScanResult>) {
        var listAsString = ""

        list.sortedWith(compareBy { it.level })

        list.forEach { item ->
            listAsString = listAsString.plus("${item.SSID} (${item.BSSID}) \n")
        }
        binding.lastWifiNetworks.text = listAsString
    }

    private fun displayBTDevices(list: ArrayList<BTScanResult>) {
        var listAsString = ""

        list.sortedWith(compareBy { it.rssi })

        list.forEach { item ->
            var line = item.device.address

            if (item.device.name != null) {
                line = line.plus(" (${item.device.name})")
            }
            listAsString = listAsString.plus("$line \n")
        }
        binding.lastBtDevices.text = listAsString
    }

    private fun runtimePermissions(): Boolean {
        //if (Build.VERSION.SDK_INT >= 23
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED

        ) {
            requestPermissions(arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    //android.Manifest.permission.ACCESS_WIFI_STATE,
                    android.Manifest.permission.CHANGE_WIFI_STATE
            ), requestCode)

            return true
        }
        return false
    }
}