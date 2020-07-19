package com.karpicki.locationscanner

import android.location.Location
import android.net.wifi.ScanResult
import android.os.AsyncTask
import android.util.Log
import okhttp3.*

class WIFIStoreTask: AsyncTask<String, Int, Int>() {

    private val mediaType: MediaType? = MediaType.parse("application/json; charset=utf-8");

    override fun doInBackground(vararg params: String?): Int? {

        if (params.isEmpty()) {
            return null
        }
        val json = params[0].toString()
        val client = OkHttpClient();
        val body: RequestBody = RequestBody.create(mediaType, json)

        val request: Request = Request.Builder()
            .url(BuildConfig.BT_STORE_HOST)
            .post(body)
            .header("x-api-key", BuildConfig.BT_STORE_API_KEY )
            .build()

        val response: Response = client.newCall(request).execute()

        Log.d("TAG", "response.code():" + response.code())
        //response.body()?.string()
        return response.code()
    }

    public fun stringify(location : Location, list: ArrayList<ScanResult>): String {
        var strNetworks = ""
        val strLocation = "\"location\": {\"latitude\": ${location.latitude}, \"longitude\": ${location.longitude}}"

        list.forEachIndexed{ index, item ->
            val ssid = item.SSID
            val bssid = item.BSSID
            val rssi = item.level
            var strItem = ""

            strItem = " {\"network\": {\"ssid\": \"$ssid\", \"bssid\": \"$bssid\" }, \"rssi\": $rssi}"

            if (index > 0) {
                strItem = ",$strItem"
            }
            strNetworks += strItem
        }
        strNetworks = "\"networks\": [$strNetworks]"

        return "{$strLocation, $strNetworks}"
    }
}