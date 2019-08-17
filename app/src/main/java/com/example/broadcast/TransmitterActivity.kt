package com.example.broadcast

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_transmitter.*
import java.io.File
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.util.regex.Pattern


class TransmitterActivity : AppCompatActivity() {

    var path: String = ""
    var ipList = mutableListOf<String>()
    var songRun = false
    var timeToStart: Long = 0
    var timeToStop: Long = 0
    private val latency = 200
    lateinit var server: SongServer

    inner class SongServer @Throws(IOException::class) constructor() : NanoHTTPD(63342) {

        init {
            start(SOCKET_READ_TIMEOUT, false)
        }

        override fun serve(session: IHTTPSession): Response {
            val params = session.parameters
            val ip = session.remoteIpAddress
            if (!ipList.contains(ip) and params.containsKey("Downloaded")) {
                ipList.add(ip)
                runOnUiThread{
                    findViewById<TextView>(R.id.debug_text).text = ip
                }
            }
            return newChunkedResponse(Response.Status.OK, ".mp3", File(path).inputStream())
        }

    }

    fun close() {
        finish()
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

    fun runSongServer() {
        try {
            server = SongServer()
        } catch (ioe: IOException) {
            System.err.println("Couldn't start server:\n$ioe")
        }

    }

    fun getData(): String {
        return getLocalIpAddress().toString()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmitter)
        findViewById<TextView>(R.id.ShowIPTextView).text = getData()

        val thisActivity = this@TransmitterActivity

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            if (!(ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                ActivityCompat.requestPermissions(thisActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        val showIP = findViewById<TextView>(R.id.ShowIPTextView)
        showIP.text = getLocalIpAddress()
        changeSong()
        ExitT.setOnClickListener {
            finish()
        }
    }



    fun exitFromParty(view: View) {
        // PUT YOUR CODE HERE
    }

    fun stopSong(view: View) {
    }

    fun changeSongHandler(view: View) {
        changeSong()
    }

    fun resumeSong(view: View) {
        if (!songRun){
            for (ip in ipList) {
                val queue = Volley.newRequestQueue(this)
                val time = currentTimeMillis() + latency
                timeToStart = time
                val stringRequest = StringRequest(Request.Method.GET, "http://$ip:63343/?timeToStart=$time",
                    Response.Listener<String> {response -> runOnUiThread {Toast.makeText(this, "$response ot $ip", Toast.LENGTH_LONG).show()}},
                    Response.ErrorListener {error -> runOnUiThread {Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()}})
                queue.add(stringRequest)
                    }
            songRun = true
            }
        else {
            for (ip in ipList) {
                val queue = Volley.newRequestQueue(this)
                val time = currentTimeMillis() + latency
                timeToStop = time
                val stringRequest = StringRequest(Request.Method.GET, "http://$ip:63343/?timeToStop=$time",
                    Response.Listener<String> {response -> runOnUiThread {Toast.makeText(this, response, Toast.LENGTH_LONG).show()}},
                    Response.ErrorListener {error -> runOnUiThread {Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()}})
                queue.add(stringRequest)
                runOnUiThread {Toast.makeText(this, ip, Toast.LENGTH_SHORT).show()}
                    }
            songRun = false
            }
        }

    fun changeSong() {
        MaterialFilePicker()
            .withActivity(this)
            .withRequestCode(1000)
            .withFilter(Pattern.compile(".*\\.mp3$")) // Filtering files and directories by file name using regexp
            .withFilterDirectories(false) // Set directories filterable (false by default)
            .withHiddenFiles(true) // Show hidden files and folders
            .start()
        runSongServer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            val filePath = data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            path = filePath
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
