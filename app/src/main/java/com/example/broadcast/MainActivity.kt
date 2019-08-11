package com.example.broadcast

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import fi.iki.elonen.NanoHTTPD
import java.io.IOException




class MainActivity : AppCompatActivity() {

    inner class App @Throws(IOException::class) constructor() : NanoHTTPD(8080) {

        init {
            val textView = findViewById<TextView>(R.id.text)
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            textView.text = "Hooray!"
        }

        override fun serve(session: IHTTPSession): NanoHTTPD.Response {
            var msg = "<html><body><h1>Hello server</h1>\n"
            val parms = session.parms
            if (parms["username"] == null) {
                msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n"
            } else {
                msg += "<p>Hello, " + parms["username"] + "!</p>"
            }
            return NanoHTTPD.newFixedLengthResponse("$msg</body></html>\n")
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

    override fun onCreate(savedInstanceState: Bundle?) {

        try {
            App()
        } catch (ioe: IOException) {
            System.err.println("Couldn't start server:\n$ioe")
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.text)
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
