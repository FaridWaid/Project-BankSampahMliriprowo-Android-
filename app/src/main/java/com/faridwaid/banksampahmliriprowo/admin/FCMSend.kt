package com.faridwaid.banksampahmliriprowo.admin

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.AuthFailureError

import com.android.volley.VolleyError

class FCMSend {

    // Membuat variabel base_url yang merupakan api dari fcm
    // dan variabel server_key merupakan ket dari firebase aplikasi bank sampah mliriprowo
    private val BASE_URL = "https://fcm.googleapis.com/fcm/send"
    private val SERVER_KEY = "key=AAAA8VYs_uY:APA91bG1nJdfDMwyAIB1XHuKFeKBTe1iGJq9ydRP0CAOGSqtF-iWhjmJqZAfx4op4gt9FLi1fb5MifJ5O3mz1NxBsPCkLWKzaU9VTy2uVn4-_LBh338xv4ke5YcxFHd_MQnX-Rikqbig"

    // Membuat fungsi pushNotification yang nantinya dapat diakses agar aplikasi bisa mengirimkan notifikasi
    fun pushNotification(context: Context?, token: String?, title: String?, message: String?) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val queue = Volley.newRequestQueue(context)

        try {
            val json = JSONObject()
            json.put("to", token)
            val notification = JSONObject()
            notification.put("title", title)
            notification.put("body", message)
            json.put("notification", notification)

            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, BASE_URL, json,
                Response.Listener<JSONObject>() {
                    Log.d("FCM", it.toString())
                }, object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError?) {
                        TODO("Not yet implemented")
                    }
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-type"] = "application/json"
                    params["Authorization"] = SERVER_KEY
                    return params
                }
            }
            queue.add(jsonObjectRequest)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

}