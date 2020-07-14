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

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

class SyncService: Service() {

    private var broadcastReceiver: BroadcastReceiver? = null

    private var lastLocation: Location? = null
    private var lastSyncLocation: Location? = null
    //private var lastWifiList: ArrayList<WIFIScanResult> = ArrayList()
    private var syncedBTDevices: ArrayList<BTScanResult> = ArrayList()

    private val mediaType: MediaType? = MediaType.parse("application/json; charset=utf-8");
    //private val BT_API = "http://192.168.0.14:9001/2015-03-31/functions/myfunction/invocations"

    private fun sendBTDevices(list: ArrayList<BTScanResult>) {

        Thread {
            // TODO - move to own class with vals used
            var json = "{\"location\":{\"latitude\":52.1, \"longitude\":13.1}, \"devices\":[{\"address\": \"as:zx:as:12:22:zz\"}]}"
            val client: OkHttpClient = OkHttpClient();
            val body: RequestBody = RequestBody.create(mediaType, json)

            val request: Request = Request.Builder()
                .url(BuildConfig.BT_STORE_HOST)
                .post(body)
                .header("x-api-key", BuildConfig.BT_STORE_API_KEY )
                .build()

            val response: Response = client.newCall(request).execute()

            response.body()?.string()

        }.start()

//        String post(String url, String json) throws IOException {
//            RequestBody body = RequestBody.create(json, JSON);
//            Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//            try (Response response = client.newCall(request).execute()) {
//                return response.body().string();
//            }
//            }

//        val request = Request.Builder()
//            .url(url)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {}
//            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
//        })
    }

    private fun syncBTDevices(list: ArrayList<BTScanResult>) {
        //check if device(s) already sync ofr this location / so keep in some Hash
        //which not yet -> sync
        //update lastSyncLocation
        var btDevicesToSync: ArrayList<BTScanResult> = ArrayList()

        if (lastSyncLocation != lastLocation) {
            syncedBTDevices.clear()
            btDevicesToSync = list
        } else {

            list.forEach { item ->
                val foundBtDevice: BTScanResult? = syncedBTDevices.find {
                    it.device.address == item.device.address
                }
                if (foundBtDevice == null) {
                    btDevicesToSync.add(item)
                }
            }
        }
        if (btDevicesToSync.size > 0) {
            syncedBTDevices.addAll(btDevicesToSync)
            sendBTDevices(btDevicesToSync)
        }
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
                            val foundWifNetworks: ArrayList<WIFIScanResult> = ArrayList()

                            wifiList.forEach { wifiNetwork -> foundWifNetworks.add(wifiNetwork as WIFIScanResult) }
                            syncWIFINetworks(foundWifNetworks)
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