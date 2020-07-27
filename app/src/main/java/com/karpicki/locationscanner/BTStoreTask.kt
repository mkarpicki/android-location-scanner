package com.karpicki.locationscanner

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import okhttp3.*
import java.util.*

class BTStoreTask : AsyncTask<String, Int, Int>() {

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

    fun stringify(location: Location, list: ArrayList<BTScanResult>): String {

        val strLocation = "\"location\": {\"latitude\": ${location.latitude}, \"longitude\": ${location.longitude}}"
        val devicesArray = ArrayList<String>()

        list.forEachIndexed{ index, item ->
            val address = item.scanResult.device.address
            val name = item.scanResult.device.name
            val rssi = item.rssi
            val deviceLocation: Location? = item.location
            val timestamp = item.timestamp

            val deviceStr = if (name != null) {
                "\"device\": {\"address\": \"$address\", \"name\": \"$name\" }"
            } else {
                "\"device\": {\"address\": \"$address\" }"
            }

            val itemArray = ArrayList<String>()

            itemArray.add("\"rssi\": $rssi")

            if (deviceLocation != null) {
                itemArray.add("\"location\": {\"latitude\": ${deviceLocation.latitude}, \"longitude\": ${deviceLocation.longitude}}")
            }
            if (timestamp != 0L) {
                itemArray.add("\"timestamp\": $timestamp")
            }

            var itemStr = itemArray.joinToString(",")

            itemStr = "{ $itemStr, $deviceStr }"

            devicesArray.add(itemStr)
        }

        val devicesStr = devicesArray.joinToString(",")

        return "{$strLocation, \"devices\": [$devicesStr] }"
    }
}