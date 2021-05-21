package com.karpicki.locationscanner

import android.annotation.SuppressLint
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

    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context
    private val IGNORED_LIST = "ignored_list"

    fun setContext(context: Context) {
        this.context = context
    }

    override fun doInBackground(vararg params: Void?): String {

        var responseStr = "{\"wifi\": [], \"bt\": []}"

        try {

            val client = OkHttpClient();

            val request: Request = Request.Builder()
                .url(BuildConfig.IGNORED_LIST_URL)
                .get()
                .header("x-api-key", BuildConfig.IGNORED_LIST_X_API_KEY )
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

    fun parse(strJson: String?): Array<String> {

        var jsonArray = JSONArray()

        if (strJson != null) {
            try {
                jsonArray = JSONArray(strJson)
            } catch (e: JSONException) {
            }
        }

        return Array(jsonArray.length()) {
            jsonArray.getString(it)
        }
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