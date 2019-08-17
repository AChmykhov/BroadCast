package com.example.broadcast

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_transmitter.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.lang.System.currentTimeMillis
import java.util.regex.Pattern
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_main.*


class TransmitterActivity : AppCompatActivity() {

    var path: String = ""
    var ipList = mutableListOf<String>()
    private val latency = 200
    lateinit var server: SongServer
    internal var bitmap: Bitmap? = null
    private var showIPQR: ImageView? = null
    lateinit var adapter: MusicListAdapter

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

    public fun runServ(_path: String) {
        path = _path
        try {
            server = SongServer()
        } catch (ioe: IOException) {
            System.err.println("Couldn't start server:\n$ioe")
            Toast.makeText(this, "Couldn't start server:\n$ioe", Toast.LENGTH_LONG).show()
            finish()
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
        showIPQR = findViewById<ImageView>(R.id.ShowIPQR)
        if (etqr.trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(this@TransmitterActivity, "Enter String!", Toast.LENGTH_SHORT).show()
        } else {
            try {
                bitmap = TextToImageEncode(etqr)
                showIPQR!!.setImageBitmap(bitmap)
                val path = saveImage(bitmap)  //give read write permission
                Toast.makeText(this@TransmitterActivity, "QRCode saved to -> $path", Toast.LENGTH_SHORT).show()
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            if (!(ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                ActivityCompat.requestPermissions(thisActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        createMusicList()

        val showIP = findViewById<TextView>(R.id.ShowIPTextView)
        showIP.text = getLocalIpAddress()
        changeSong()
        ExitT.setOnClickListener {
            finish()
        }
    }

    companion object {

        val QRcodeWidth = 500
        private val IMAGE_DIRECTORY = "/QRcodeDemonuts"
    }

    @Throws(WriterException::class)
    private fun TextToImageEncode(Value: String): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(
                Value,
                BarcodeFormat.QR_CODE,
                QRcodeWidth, QRcodeWidth, null
            )

        } catch (Illegalargumentexception: IllegalArgumentException) {

            return null
        }

        val bitMatrixWidth = bitMatrix.getWidth()

        val bitMatrixHeight = bitMatrix.getHeight()

        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth

            for (x in 0 until bitMatrixWidth) {

                pixels[offset + x] = if (bitMatrix.get(x, y))
                    resources.getColor(R.color.black)
                else
                    resources.getColor(R.color.white)
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)
        return bitmap
    }


fun saveImage(myBitmap: Bitmap?): String {
        val bytes = ByteArrayOutputStream()
        myBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
        )
        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs())
            wallpaperDirectory.mkdirs()
        }

        try {
            val f = File(
                wallpaperDirectory, Calendar.getInstance()
                    .timeInMillis.toString() + ".jpg"
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)

            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }



    fun createMusicList() {
        var musicList = findViewById<ListView>(R.id.MusicList)
        adapter = MusicListAdapter(this, this::runServ)
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

    fun stopSong(view: View) {
    }

    fun changeSongHandler(view: View) {
        changeSong()
    }

    fun resumeSong(view: View) {
        for (ip in ipList) {
            val queue = Volley.newRequestQueue(this)
            val time = currentTimeMillis() + latency
            val responses = mutableListOf<String>()
            val stringRequest = StringRequest(Request.Method.GET, "http://$ip:63343/?timeToStart=$time",
                Response.Listener<String> {response -> runOnUiThread {Toast.makeText(this, response, Toast.LENGTH_LONG).show()}},
                Response.ErrorListener {error -> runOnUiThread {Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()}})
            queue.add(stringRequest)
            runOnUiThread {Toast.makeText(this, ip, Toast.LENGTH_SHORT).show()}
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
        runSongServer()
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
                adapter.addItem(currentTitle, currentArtist, currentLocation)
            } while (songCursor.moveToNext())
        }
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