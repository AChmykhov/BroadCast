package com.example.broadcast

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_transmitter.*
import java.io.File
import java.io.IOException
import java.util.regex.Pattern


class TransmitterActivity : AppCompatActivity() {

    var path: String = ""
    var musicArray = emptyArray<String>()
    var locationArray = emptyArray<String>()

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

    fun runServ(view: View) {
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
        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, musicArray)
        musicList.adapter = adapter

        musicList.setOnItemClickListener { parent, view, position, id ->
            path = locationArray[position]
            Toast.makeText(this, "YES!", Toast.LENGTH_SHORT).show()
            try {
                Toast.makeText(this, "Good!", Toast.LENGTH_LONG).show()
                runServ(view)
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
                musicArray += "Title: " + currentTitle + "\n\n" +
                        "Artist: " + currentArtist
                locationArray += currentLocation
            } while (songCursor.moveToNext())
        }
    }

    fun exitFromParty(view: View) {
        // PUT YOUR CODE HERE
    }

    fun stopSong(view: View) {
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
