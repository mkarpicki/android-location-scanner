package com.karpicki.locationscanner

import android.bluetooth.le.ScanResult
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

    fun stringify(list: ArrayList<ScanResult>): String {
        var str = ""
        return str
    }
}