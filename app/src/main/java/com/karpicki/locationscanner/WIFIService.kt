package com.karpicki.locationscanner

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.IBinder

class WIFIService : Service() {

    private var resultList = ArrayList<ScanResult>()
    private lateinit var wifiManager: WifiManager

    private var addressesToIgnore: ArrayList<String> = ArrayList()

    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val intentAddressesToIgnore = intent.extras?.get("addressesToIgnore") as Array<*>
            intentAddressesToIgnore.forEach {
                addressesToIgnore.add(it.toString())
            }
        }
        return START_STICKY;
    }

    override fun onCreate() {
        super.onCreate()

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)

                if (success!!) {
                    resultList = wifiManager.scanResults as ArrayList<ScanResult>
                    resultList = filterOutMyNetwork(resultList)
                } else {
                    resultList.clear()
                }
                val scanIntent = Intent("wifi_scan_update")
                scanIntent.putExtra("wifi_results", resultList)
                sendBroadcast(scanIntent)
            }
        }

        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        if (!wifiManager.startScan()) {
            TODO("consider handling failure")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    private fun filterOutMyNetwork(list: ArrayList<ScanResult>):ArrayList<ScanResult> {

        val wifiInfo = wifiManager.connectionInfo as WifiInfo

        return if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            val filteredList = ArrayList<ScanResult>()
            list.forEach { item ->
                if (!wifiInfo.bssid.equals(item.BSSID, true)) {
                    filteredList.add(item)
                }
            }
            filteredList
        } else {
            list
        }

    }
}