package com.example.broadcast

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern


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
        return try {
            val encoded = Files.readAllBytes(Paths.get(path))
            encoded
        } catch (e: IOException) {
            null
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
            System.err.println("Couldn't start server:\n$ioe")
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 10001)

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


    fun changeSong(view: View) {
        MaterialFilePicker()
            .withActivity(this)
            .withRequestCode(1000)
            .withFilter(Pattern.compile(".*\\.mp3$")) // Filtering files and directories by file name using regexp
            .withFilterDirectories(false) // Set directories filterable (false by default)
            .withHiddenFiles(true) // Show hidden files and folders
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            val filePath = data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            val path = filePath
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1001 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
