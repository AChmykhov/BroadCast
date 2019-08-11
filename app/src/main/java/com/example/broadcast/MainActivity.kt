package com.example.broadcast

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*





class MainActivity : AppCompatActivity() {

    inner class App @Throws(IOException::class) constructor() : NanoHTTPD(63342) {

        init {
            val textView = findViewById<TextView>(R.id.text)
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            textView.text = "Hooray!"
        }

        override fun serve(session: IHTTPSession): NanoHTTPD.Response {

            return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "audio/mpeg", msg = converter())
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun converter(path: String): ByteArray? {
        try {
            val encoded = Files.readAllBytes(Paths.get(path))
            return encoded
        } catch (e: IOException) {
            return null
        }
    }

    private fun getLocalIpAddress(): String? {
        try {

            val wifiManager: WifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            return ipToString(wifiManager.connectionInfo.ipAddress)
        }
        catch (ex: Exception) {
            Log.e("IP Address", ex.toString())
        }

        return null
    }

    private fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)

    }

    fun runServ(view: View) {
        try {
            App()
        } catch (ioe: IOException) {
            val textView = findViewById<TextView>(R.id.text)
            textView.text = "Not hooray(($ioe"
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.text)
        val textView1 = findViewById<TextView>(R.id.textView)
        textView1.text = getLocalIpAddress()
// ...

// Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "http://www.google.com"
// Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> {},
            Response.ErrorListener { textView.text = "That didn't work!" })

// Add the request to the RequestQueue.
        queue.run {

            // Add the request to the RequestQueue.
            add(stringRequest)
        }

    }
}
