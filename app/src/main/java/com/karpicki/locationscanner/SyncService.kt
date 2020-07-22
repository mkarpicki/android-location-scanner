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

    private val btMaxBufforSize: Int = 25
    private val wifiMaxBufforSize: Int = 25

    private var lastLocation: Location? = null

    private var wifiDevicesToSync: ArrayList<WIFIScanResult> = ArrayList()
    private var bTDevicesToSync: ArrayList<BLuetoothScanResult> = ArrayList()

//    private fun getNames (): String {
//        var  str = ""
//
//        bTDevicesToSync.forEach { item ->
//            str += item.device.address + ", "
//        }
//
//        return str
//    }

    private fun syncBTDevices(location : Location) {

        val btStoreTask = BTStoreTask()
        val json = btStoreTask.stringify(location, bTDevicesToSync)
        //BTStoreTask().execute(json)

        Log.d("syncBTDevices", json)

        bTDevicesToSync.clear()
    }

    private fun syncWIFINetworks(location : Location) {
        val wifiStoreTask = WIFIStoreTask()
        val json = wifiStoreTask.stringify(location, wifiDevicesToSync)
        //BTStoreTask().execute(json)

        Log.d("syncWIFINetworks", json)

        wifiDevicesToSync.clear()
    }

    private fun collectWIFINetworks(list: ArrayList<WIFIScanResult>) {
        list.forEach { item ->
            val foundWIFINetwork : WIFIScanResult? = wifiDevicesToSync.find {
                it.BSSID == item.BSSID
            }
            if (foundWIFINetwork == null) {
                wifiDevicesToSync.add(item)
            }
        }
    }

    private fun collectBTDevices (list : ArrayList<BLuetoothScanResult>) {
        list.forEach { item ->
            val foundBtDevice: BLuetoothScanResult? = bTDevicesToSync.find {
                it.scanResult.device.address == item.scanResult.device.address
            }
            if (foundBtDevice == null) {
                bTDevicesToSync.add(item)
            }
        }
    }

    private fun collectedEnoughWIFINetworks () :Boolean {
        return (wifiDevicesToSync.size >= wifiMaxBufforSize && lastLocation != null)
    }


    private fun collectedEnoughBTDevices () :Boolean {
        return (bTDevicesToSync.size >= btMaxBufforSize && lastLocation != null)
    }

    private fun syncAll(location: Location) {
        syncBTDevices(location)
        syncWIFINetworks(location)
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

                            collectWIFINetworks(foundWifNetworks)

                            if (collectedEnoughWIFINetworks()) {
                                syncWIFINetworks(lastLocation!!)
                            }
                        }
                        "bt_scan_update" -> {
                            val btList = intent.extras?.get("bt_results") as ArrayList<*>

                            val bluetoothScanResults = ArrayList<BLuetoothScanResult>()

                            btList.forEach { btDevice ->
                                //foundBTDevices.add(btDevice as BTScanResult)
                                bluetoothScanResults.add(BLuetoothScanResult(
                                    btDevice as BTScanResult,
                                    System.currentTimeMillis(),
                                    btDevice.rssi,
                                    lastLocation
                                ))

                            }

                            collectBTDevices(bluetoothScanResults)

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