package com.karpicki.locationscanner

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class IgnoredListLoaderTask: AsyncTask<String, Int, Int>() {

    private lateinit var context: Context
    private val IGNORED_LIST = "ignored_list"

    fun setContext(context: Context) {
        this.context = context
    }

    override fun doInBackground(vararg params: String?): Int? {

        var responseCode = 500

        try {

            val client = OkHttpClient();

            val request: Request = Request.Builder()
                .url(BuildConfig.IGNORED_LIST_HOST)
                .get()
                .header("x-api-key", BuildConfig.IGNORED_LIST_API_KEY )
                .build()

            val response: Response = client.newCall(request).execute()

            Log.d("TAG", "response.code():" + response.code())
            //response.body()?.string()

            responseCode = response.code()

            if (responseCode == 200) {
                saveList(response.body()!!.string())
            }

        } catch (e: Exception) {

        } finally {
        }

        return responseCode;
    }

    private fun saveList(response: String) {
        val editor: SharedPreferences.Editor =
            context.getSharedPreferences(IGNORED_LIST, MODE_PRIVATE).edit()
        editor.putString(IGNORED_LIST, response)
        editor.apply()
    }

    fun getList() {

    }

}