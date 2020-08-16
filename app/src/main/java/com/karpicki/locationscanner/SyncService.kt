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

    private val btMaxBufforSize: Int = 2
    private val wifiMaxBufforSize: Int = 2

    private var lastLocation: Location? = null

    private var wifiDevicesToSync: ArrayList<com.karpicki.locationscanner.WIFIScanResult> = ArrayList()
    private var bTDevicesToSync: ArrayList<com.karpicki.locationscanner.BTScanResult> = ArrayList()

    private fun broadcastSaveStatus(action: String, value: Int) {
        val scanIntent = Intent(action)
        scanIntent.putExtra("status", value)
        sendBroadcast(scanIntent)
    }

    private fun broadcastWIFIList() {
        val scanIntent = Intent("wifi_list_to_sync")
        scanIntent.putExtra("list", wifiDevicesToSync)
        sendBroadcast(scanIntent)
    }

    private fun broadcastBTList() {
        val scanIntent = Intent("bt_list_to_sync")
        scanIntent.putExtra("list", bTDevicesToSync)
        sendBroadcast(scanIntent)
    }

    private fun syncBTDevices(location : Location?) {

        if (location == null) {
            return
        }

        val btStoreTask = BTStoreTask()
        val json = btStoreTask.stringify(location, bTDevicesToSync)

        val json2 = btStoreTask.stringifyGeoJson(location, bTDevicesToSync)

        val responseCode = btStoreTask.execute(json).get()

        Log.d("syncBTDevices:payload", json)
        Log.d("syncBTDevices:response_code", responseCode.toString())

        if (responseCode == 201) {
            bTDevicesToSync.clear()
        }
        broadcastSaveStatus("bt_save_status", responseCode)
    }

    private fun syncWIFINetworks(location : Location?) {

        if (location == null) {
            return
        }

        val wifiStoreTask = WIFIStoreTask()
        val json = wifiStoreTask.stringify(location, wifiDevicesToSync)
        val responseCode = wifiStoreTask.execute(json).get()

        Log.d("syncWIFINetworks:payload", json)
        Log.d("syncWIFINetworks:response_code", responseCode.toString())

        if (responseCode == 201) {
            wifiDevicesToSync.clear()
        }
        broadcastSaveStatus("wifi_save_status", responseCode)
    }

    private fun collectWIFINetworks(list: ArrayList<com.karpicki.locationscanner.WIFIScanResult>) {
        list.forEach { item ->
            val foundWIFINetwork : com.karpicki.locationscanner.WIFIScanResult? = wifiDevicesToSync.find {
                it.scanResult.BSSID .equals(item.scanResult.BSSID, true)
            }
            if (foundWIFINetwork == null) {
                wifiDevicesToSync.add(item)
            }
        }
    }

    private fun collectBTDevices (list : ArrayList<com.karpicki.locationscanner.BTScanResult>) {
        list.forEach { item ->
            val foundBtDevice: com.karpicki.locationscanner.BTScanResult? = bTDevicesToSync.find {
                it.scanResult.device.address.equals(item.scanResult.device.address, true)
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

    private fun syncAll(location: Location?) {
        syncBTDevices(location)
        syncWIFINetworks(location)

        broadcastBTList()
        broadcastWIFIList()
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
                            //syncAll(location)
                        }
                        "wifi_scan_update" -> {
                            val wifiList = intent.extras?.get("wifi_results") as ArrayList<*>
                            val foundWifNetworks: ArrayList<com.karpicki.locationscanner.WIFIScanResult> = ArrayList()

                            wifiList.forEach { wifiNetwork ->
                                foundWifNetworks.add(
                                    WIFIScanResult(
                                        wifiNetwork as WIFIScanResult,
                                        System.currentTimeMillis(),
                                        wifiNetwork.level,
                                        lastLocation
                                    )
                                )
                            }
                            collectWIFINetworks(foundWifNetworks)

                            if (collectedEnoughWIFINetworks()) {
                                syncWIFINetworks(lastLocation)
                            }
                            broadcastWIFIList()
                        }
                        "bt_scan_update" -> {
                            val btList = intent.extras?.get("bt_results") as ArrayList<*>

                            val bluetoothScanResults = ArrayList<com.karpicki.locationscanner.BTScanResult>()

                            btList.forEach { btDevice ->
                                //foundBTDevices.add(btDevice as BTScanResult)
                                bluetoothScanResults.add(
                                    BTScanResult(
                                        btDevice as BTScanResult,
                                        System.currentTimeMillis(),
                                        btDevice.rssi,
                                        lastLocation
                                    )
                                )

                            }

                            collectBTDevices(bluetoothScanResults)

                            if (collectedEnoughBTDevices()) {
                                syncBTDevices(lastLocation)
                            }
                            broadcastBTList()
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
        syncAll(lastLocation)
    }

}