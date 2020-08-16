package com.karpicki.locationscanner

import android.location.Location
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
            .url(BuildConfig.WIFI_STORE_HOST)
            .post(body)
            .header("x-api-key", BuildConfig.WIFI_STORE_API_KEY )
            .build()

        val response: Response = client.newCall(request).execute()

        Log.d("TAG", "response.code():" + response.code())
        //response.body()?.string()
        return response.code()
    }
    fun stringifyGeoJson(location : Location, list: ArrayList<WIFIScanResult>): String {

        val features = ArrayList<String>()
        val currentTimestamp = System.currentTimeMillis()

        list.forEach { item ->
            val ssid = item.scanResult.SSID
            val bssid = item.scanResult.BSSID
            val rssi = item.scanResult.level
            val deviceLocation: Location? = item.location
            val timestamp = item.timestamp
            val coordinates = ArrayList<Double>()

            val propertiesArray = ArrayList<String>()

            val networkStr: String
            val syncData: String

            networkStr = "{ \"ssid\": \"$ssid\", \"bssid\": \"$bssid\" }"

            if (deviceLocation != null) {
                coordinates.add(deviceLocation.longitude)
                coordinates.add(deviceLocation.latitude)
            } else {
                coordinates.add(location.longitude)
                coordinates.add(location.latitude)
            }

            val locationStr = "{ \"latitude\": ${location.latitude}, \"longitude\": ${location.longitude} }"
            syncData = "{ \"timestamp\": $currentTimestamp, \"location\": $locationStr }"

            propertiesArray.add("\"name\": \"$ssid\"")
            propertiesArray.add("\"rssi\": $rssi")
            propertiesArray.add("\"network\": $networkStr")
            propertiesArray.add("\"syncData\": $syncData")

            if (timestamp != 0L) {
                propertiesArray.add("\"timestamp\": $timestamp")
            }

            val geometryStr = "{ \"type\": \"Point\", \"coordinates\": [${coordinates[0]}, ${coordinates[1]}]}"
            val propertiesStr = "{ ${propertiesArray.joinToString(",")} }"

            val featureStr = "{ \"geometry\": $geometryStr, \"properties\": $propertiesStr, \"type\": \"Feature\" }"

            features.add(featureStr)
        }

        val featuresStr = features.joinToString(",")
        return "{ \"type\": \"FeatureCollection\", \"features\": [$featuresStr] }"
    }


    fun stringify(location : Location, list: ArrayList<WIFIScanResult>): String {

        val strLocation = "\"location\": {\"latitude\": ${location.latitude}, \"longitude\": ${location.longitude}}"
        val devicesArray = java.util.ArrayList<String>()

        list.forEach { item ->
            val ssid = item.scanResult.SSID
            val bssid = item.scanResult.BSSID
            val rssi = item.scanResult.level
            val deviceLocation: Location? = item.location
            val timestamp = item.timestamp

            val itemArray = java.util.ArrayList<String>()

            itemArray.add("\"rssi\": $rssi")

            if (deviceLocation != null) {
                itemArray.add("\"location\": {\"latitude\": ${deviceLocation.latitude}, \"longitude\": ${deviceLocation.longitude}}")
            }
            if (timestamp != 0L) {
                itemArray.add("\"timestamp\": $timestamp")
            }

            var itemStr = itemArray.joinToString(",")

            val deviceStr = "\"network\": {\"ssid\": \"$ssid\", \"bssid\": \"$bssid\" }"

            itemStr = "{ $itemStr, $deviceStr }"

            devicesArray.add(itemStr)

        }

        val devicesStr = devicesArray.joinToString(",")

        return "{$strLocation, \"networks\": [$devicesStr] }"
    }
}