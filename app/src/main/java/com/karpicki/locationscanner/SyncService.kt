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
import android.util.Log

import kotlin.collections.ArrayList

class SyncService: Service() {

    private var broadcastReceiver: BroadcastReceiver? = null

    private val btCollectBuffor: Int = 3 //10

    private var lastLocation: Location? = null
    //private var lastSyncLocation: Location? = null
    //private var lastWifiList: ArrayList<WIFIScanResult> = ArrayList()

    private var bTDevicesToSync: ArrayList<BTScanResult> = ArrayList()

    private fun syncBTDevices(location : Location) {

        //var json = "{\"location\":{\"latitude\":52.1, \"longitude\":13.1}, \"devices\":[{\"address\": \"as:zx:as:12:22:zz\"}]}"
        //BTStoreTask().execute(json)
        Log.d("TAG", "save GPD:" + location)
        Log.d("TAG", "save BT:" + bTDevicesToSync)

        bTDevicesToSync.clear()
    }

    private fun syncWIFINetworks(list: ArrayList<WIFIScanResult>) {
        //if lastLocation == lastSyncLocation
        //check if any new WIFI found for lastSyncLocation
        //sync
        //update lastSyncLocation

    }

    private fun collectBTDevices (list : ArrayList<BTScanResult>) {
        list.forEach { item ->
            val foundBtDevice: BTScanResult? = bTDevicesToSync.find {
                it.device.address == item.device.address
            }
            if (foundBtDevice == null) {
                bTDevicesToSync.add(item)
            }
        }
    }

    private fun collectedEnoughBTDevices () :Boolean {
        return (bTDevicesToSync.size >= btCollectBuffor && lastLocation != null)
    }

    private fun syncAll(location: Location) {
        syncBTDevices(location)
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
                            syncAll(location)
                        }
                        "wifi_scan_update" -> {
                            val wifiList = intent.extras?.get("wifi_results") as ArrayList<*>
                            val foundWifNetworks: ArrayList<WIFIScanResult> = ArrayList()

                            wifiList.forEach { wifiNetwork -> foundWifNetworks.add(wifiNetwork as WIFIScanResult) }
                            //syncWIFINetworks(foundWifNetworks)
                        }
                        "bt_scan_update" -> {
                            val btList = intent.extras?.get("bt_results") as ArrayList<*>
                            val foundBTDevices: ArrayList<BTScanResult> = ArrayList()

                            btList.forEach { btDevice ->  foundBTDevices.add(btDevice as BTScanResult) }

                            collectBTDevices(foundBTDevices)

                            if (collectedEnoughBTDevices()) {
                                syncBTDevices(lastLocation!!)
                            }
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