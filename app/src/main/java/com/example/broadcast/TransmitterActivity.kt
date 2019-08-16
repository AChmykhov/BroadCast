package com.example.broadcast

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_transmitter.*
import java.io.File
import java.io.IOException


class TransmitterActivity : AppCompatActivity() {

    var path: String = ""
    var adapter = MusicListAdapter(this)

    inner class App @Throws(IOException::class) constructor() : NanoHTTPD(63342) {

        init {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        }

        override fun serve(session: IHTTPSession): NanoHTTPD.Response {

            return NanoHTTPD.newChunkedResponse(Response.Status.OK, ".mp3", readFileAsTextUsingInputStream(path))
        }

    }


    fun readFileAsTextUsingInputStream(fileName: String) = File(fileName).inputStream()

    private fun getLocalIpAddress(): String? {
        try {

            val wifiManager: WifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            return ipToString(wifiManager.connectionInfo.ipAddress)
        } catch (ex: Exception) {
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

    public fun runServ(view: View, _path: String) {
        path = _path
        try {
            App()
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
        findViewById<TextView>(R.id.ShowIPTextView).setText(getData())

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

        createMusicList()

        val showIP = findViewById<TextView>(R.id.ShowIPTextView)
        showIP.text = getLocalIpAddress()
        ExitT.setOnClickListener {
            finish()
        }
    }

    fun createMusicList() {
        var musicList = findViewById<ListView>(R.id.MusicList)
        getMusic()
        musicList.adapter = adapter

        musicList.setOnItemClickListener { parent, view, position, id ->
            path = adapter.objects[position].location
            Toast.makeText(this, "YES!", Toast.LENGTH_SHORT).show()
            try {
                Toast.makeText(this, "Good!", Toast.LENGTH_LONG).show()
                runServ(view, path)
            } catch (ioe: Exception) {
                System.err.println("Couldn't start server:\n$ioe")
                Toast.makeText(this, "$ioe", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun getMusic() {
        var contecntResolver = getContentResolver()
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver.query(songUri, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst()) {
            var songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            var songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            do {
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentLocation = songCursor.getString(songLocation)
                adapter.addItem(currentTitle, currentArtist, currentLocation, ::runServ)
            } while (songCursor.moveToNext())
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
