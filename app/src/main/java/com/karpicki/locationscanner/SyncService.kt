package com.karpicki.locationscanner

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.wifi.ScanResult as WIFIScanResult
import android.bluetooth.le.ScanResult as BTScanResult
import android.os.IBinder

class SyncService: Service() {

    private var broadcastReceiver: BroadcastReceiver? = null

    private var lastLocation: Location? = null
    private var lastSyncLocation: Location? = null
    //private var lastWifiList: ArrayList<WIFIScanResult> = ArrayList()
    //private var lastBTDevices: ArrayList<BTScanResult> = ArrayList()

    private fun syncBTDevices(list: ArrayList<BTScanResult>) {
        //check if device(s) already sync ofr this location / so keep in some Hash
        //which not yet -> sync
        //update lastSyncLocation
    }

    private fun syncWIFINetworks(list: ArrayList<WIFIScanResult>) {
        //if lastLocation == lastSyncLocation
        //check if any new WIFI found for lastSyncLocation
        //sync
        //update lastSyncLocation

    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {

                override fun onReceive(p0: Context?, intent: Intent?) {

                    when (intent?.action) {
                        "location_update" -> {
                            val location = intent.extras?.get("location") as Location
                            lastLocation = location

                        }
                        "wifi_scan_update" -> {
                            val wifiList = intent.extras?.get("wifi_results") as ArrayList<*>
                            val foundWifiList: ArrayList<WIFIScanResult> = ArrayList()

                            wifiList.forEach { wifiNetwork -> foundWifiList.add(wifiNetwork as WIFIScanResult) }
                            syncWIFINetworks(foundWifiList)
                        }
                        "bt_scan_update" -> {
                            val btList = intent.extras?.get("bt_results") as ArrayList<*>
                            val foundBTDevices: ArrayList<BTScanResult> = ArrayList()

                            btList.forEach { btDevice ->  foundBTDevices.add(btDevice as BTScanResult) }
                            syncBTDevices(foundBTDevices)

                        }
                    }
                }
            }

            val intentFilter = IntentFilter()
            intentFilter.addAction("location_update")
            intentFilter.addAction("wifi_scan_update")
            intentFilter.addAction("bt_scan_update")

            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

}