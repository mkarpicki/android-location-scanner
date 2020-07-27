package com.karpicki.locationscanner

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException

import org.json.JSONObject





class IgnoredListLoaderTask: AsyncTask<Void, Void, String>() {

    private lateinit var context: Context
    private val IGNORED_LIST = "ignored_list"

    fun setContext(context: Context) {
        this.context = context
    }

    override fun doInBackground(vararg params: Void?): String? {

        var responseStr = "{\"wifi\": [], \"bt\": []}"

        try {

            val client = OkHttpClient();

            val request: Request = Request.Builder()
                .url(BuildConfig.IGNORED_LIST_HOST)
                .get()
                .header("x-api-key", BuildConfig.IGNORED_LIST_API_KEY )
                .build()

            val response: Response = client.newCall(request).execute()

            Log.d("TAG", "response.code():" + response.code())

            if (response.code() == 200) {
                responseStr = response.body()!!.string()
                saveList(responseStr)
            }

        } catch (e: Exception) {
            responseStr = getList()
        } finally { }

        return responseStr;
    }

    fun parseWIFIList(strJson: String?): ArrayList<String> {
        val listJson: JSONArray = parse(strJson, "wifi")
        val list = ArrayList<String>()
        for (i in 0 until listJson.length()) {
            val address: String = listJson.getJSONObject(i).get("bssid").toString()
            list.add(address)
        }
        return list;
    }

    fun parseBTList(strJson: String?): ArrayList<String> {
        val listJson: JSONArray = parse(strJson, "bt")
        val list = ArrayList<String>()
        for (i in 0 until listJson.length()) {
            val address: String = listJson.getJSONObject(i).get("address").toString()
            list.add(address)
        }
        return list;
    }

    private fun parse(strJson: String?, propName: String): JSONArray {

        if (strJson != null) {
            try {
                return JSONObject(strJson).get(propName) as JSONArray
            } catch (e: JSONException) {
            }
        }
        return JSONArray()
    }

    private fun saveList(response: String) {
        val editor: SharedPreferences.Editor =
            context.getSharedPreferences(IGNORED_LIST, MODE_PRIVATE).edit()

        editor.putString(IGNORED_LIST, response)
        editor.apply()
    }

    fun getList(): String {

        var listStr = "{\"wifi\": [], \"bt\": []}"

        try {
            val sharedPref = context.getSharedPreferences(IGNORED_LIST, MODE_PRIVATE)
            val listFromFile: String? = sharedPref.getString(IGNORED_LIST, "0")

            if (listFromFile != null) {
                listStr = listFromFile
            }
        } catch (e: Exception) { }

        return listStr;
    }

}