package com.example.broadcast

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import kotlinx.android.synthetic.main.activity_transmitter.*
import java.io.File
import java.io.IOException
import java.util.regex.Pattern
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_main.*


class TransmitterActivity : AppCompatActivity() {

    var path: String = ""

    inner class App @Throws(IOException::class) constructor() : NanoHTTPD(63342) {

        init {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun serve(session: IHTTPSession): NanoHTTPD.Response {

            return NanoHTTPD.newChunkedResponse(Response.Status.OK, ".mp3", readFileAsTextUsingInputStream(path))
        }

    }


    fun readFileAsTextUsingInputStream(fileName: String) = File(fileName).inputStream()

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.connectionInfo.ipAddress == 0) {
            Toast.makeText(this, "No connection to Wi-Fi network", Toast.LENGTH_LONG).show()
            finish()
        }
        return ipToString(wifiManager.connectionInfo.ipAddress)
    }

    private fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)

    }

    fun runServ(@Suppress("UNUSED_PARAMETER") view: View) {
        try {
            App()
        } catch (ioe: IOException) {
            System.err.println("Couldn't start server:\n$ioe")
            Toast.makeText(this, "Couldn't start server:\n$ioe", Toast.LENGTH_LONG).show()
            finish()
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmitter)
        findViewById<TextView>(R.id.ShowIPTextView).text = getLocalIpAddress()

        val thisActivity = this@TransmitterActivity

        if (ContextCompat.checkSelfPermission(
                thisActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (!(ActivityCompat.shouldShowRequestPermissionRationale(
                    thisActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
            ) {
                ActivityCompat.requestPermissions(
                    thisActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )
            }
        }

        val showIP = findViewById<TextView>(R.id.ShowIPTextView)
        showIP.text = getLocalIpAddress()
    }


    fun exitFromParty(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }

    fun stopSong(@Suppress("UNUSED_PARAMETER") view: View) {
        // PUT YOUR CODE HERE
    }

    fun resumeSong(view: View) {
        Toast.makeText(this, "YES!", Toast.LENGTH_SHORT).show()
        try {
            Toast.makeText(this, "Good!", Toast.LENGTH_LONG).show()
            runServ(view)
        } catch (ioe: Exception) {
            System.err.println("Couldn't start server:\n$ioe")
            Toast.makeText(this, "$ioe", Toast.LENGTH_LONG).show()
        }
    }

    fun changeSong(@Suppress("UNUSED_PARAMETER") view: View) {
        MaterialFilePicker()
            .withActivity(this)
            .withRequestCode(1000)
            .withFilter(Pattern.compile(".*\\.mp3$")) // Filtering files and directories by file name using regexp
            .withFilterDirectories(false) // Set directories filterable (false by default)
            .withHiddenFiles(true) // Show hidden files and folders
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val resultPath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
        if (resultPath == null) {
            Toast.makeText(this, "Problem with file path parsing", Toast.LENGTH_LONG).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)


            if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
                path = resultPath
            }
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
