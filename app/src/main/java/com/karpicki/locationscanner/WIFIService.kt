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
                var resultList = ArrayList<ScanResult>()

                if (success!!) {
                    resultList = wifiManager.scanResults as ArrayList<ScanResult>
                    resultList = filterOutIgnored(resultList)
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

    private fun ignoreResult(result: ScanResult): Boolean {
        if (result.BSSID.equals(getMyWiFiAddress(), true)) {
            return true;
        }
        return !(this.addressesToIgnore.find { ignored -> ignored.equals(result.BSSID, true) }).isNullOrEmpty()
    }

    private fun filterOutIgnored(list: ArrayList<ScanResult>): ArrayList<ScanResult> {
        return list.filter { item -> !(ignoreResult(item)) } as ArrayList<ScanResult>
    }

    private fun getMyWiFiAddress(): String?{
        val wifiInfo = wifiManager.connectionInfo as WifiInfo

        return if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            wifiInfo.bssid
        } else {
            null
        }
    }
}