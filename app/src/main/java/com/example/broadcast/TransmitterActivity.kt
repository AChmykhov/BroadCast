package com.example.broadcast

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_transmitter.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.util.*


class TransmitterActivity : AppCompatActivity() {

    var path: String = ""
    var ipList = mutableMapOf<String, Member>()
    var songRun = false
    var timeToStart: Long = 0
    var timeToStop: Long = 0
    private val latency = 200
    lateinit var server: SongServer
    internal var bitmap: Bitmap? = null
    private var showIPQR: ImageView? = null
    lateinit var adapter: MusicListAdapter
    val FILE_SYSTEM_REQUEST = 100
    private var mediaplayer = MediaPlayer()
    private var pause = true
    private var updateRate: Long = 1000


    inner class SongServer @Throws(IOException::class) constructor() : NanoHTTPD(63342) {

        init {
            this.start(SOCKET_READ_TIMEOUT, false)
        }

        override fun serve(session: IHTTPSession): Response {
            val params = session.parameters
            val ip = session.remoteIpAddress

            if (!ipList.contains(ip) and params.containsKey("Downloaded")) {
                ipList[ip] = Member(ip)
                if (songRun) {
                    songRun = false
                    resumeSong()
                }
            }
            if (params.containsKey("Song")) {
                return newChunkedResponse(Response.Status.OK, ".mp3", File(path).inputStream())
            }
            return newFixedLengthResponse("Hello world!")
        }
        fun stpServer(){
            this.stop()
        }


    }

    fun close() {
        finish()
    }

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

    public fun runSongServer(_path: String) {
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
        var wasButtonPressed = false

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

        val etqr = getLocalIpAddress()
        showIPQR = findViewById<ImageView>(R.id.ShowIPQR)
        if ((etqr?.trim { it <= ' ' }?.isEmpty())!!) {
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

        //createMusicList()

        val showIP = findViewById<TextView>(R.id.ShowIPTextView)
        showIP.text = getLocalIpAddress()
        changeSong()
        ExitT.setOnClickListener {
            SongServer().stpServer()
            finish()
            runOnUiThread { Toast.makeText(this, "closed", Toast.LENGTH_SHORT).show() }
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
        val fileIntent = Intent(this, FileActivity::class.java)
        startActivityForResult(fileIntent, FILE_SYSTEM_REQUEST)
    }


    fun changeSongHandler(view: View) {
        changeSong()
    }

    fun resumeSongHandler(view: View) {
        resumeSong()
    }

    fun syncHandler(view: View) {
        sync()
    }

    fun resumeSong() {
        sync()
        if (!songRun) {
            mediaplayer.start()
            var i = 0
            for ((ip, value) in ipList) {
                val queue = Volley.newRequestQueue(this)
                //val time = currentTimeMillis() + value.latency + value.difference
                val time = mediaplayer.currentPosition + value.latency
                timeToStart = time
                val stringRequest = StringRequest(Request.Method.POST, "http://$ip:63343/?timeToStart=$time",
                    Response.Listener {response -> runOnUiThread {Toast.makeText(this, "$response ot $ip", Toast.LENGTH_LONG).show()}},
                    Response.ErrorListener {error -> runOnUiThread {Toast.makeText(this, "resume error " + error.toString(), Toast.LENGTH_SHORT).show()}})
                queue.add(stringRequest)
                i++
            }
            songRun = true
            PlayPauseButton.setImageResource(R.drawable.pausebutton)
        }
        else {
            mediaplayer.pause()
            var i = 0
            for ((ip, value) in ipList) {
                val queue = Volley.newRequestQueue(this)
                val time = currentTimeMillis() + value.latency + value.difference
                timeToStop = time
                val stringRequest = StringRequest(Request.Method.POST, "http://$ip:63343/?timeToStop=$time",
                    Response.Listener {response -> runOnUiThread {Toast.makeText(this, "$response ot $ip", Toast.LENGTH_LONG).show()}},
                    Response.ErrorListener {error -> runOnUiThread {Toast.makeText(this, "stop error" + error.toString(), Toast.LENGTH_SHORT).show()}})
                queue.add(stringRequest)
                i++
            }
            songRun = false
            PlayPauseButton.setImageResource(R.drawable.playbutton)
        }
    }

    fun sync() {
        var i = 0
        for ((ip, value) in ipList) {
            val queue = Volley.newRequestQueue(this)
            val time = currentTimeMillis()
            var receiverTime: Long = 0
            val start = currentTimeMillis()
            val stringRequest = StringRequest(Request.Method.GET, "http://$ip:63343/?currentTime=$time",
                Response.Listener {response ->
                    receiverTime = response.toLong()
                    val finish = currentTimeMillis()
                    val ping = (finish - start) / 2
                    receiverTime = receiverTime - ping

                    runOnUiThread {
                        Toast.makeText(this, "ping " + ping.toString(), Toast.LENGTH_LONG).show()
                        Toast.makeText(this, "difference " + (receiverTime - time), Toast.LENGTH_LONG).show()
                    }

                    ipList["a"] = Member(ip,  (finish - start) / 2, receiverTime - time)
                },
                Response.ErrorListener {error -> runOnUiThread {Toast.makeText(this, "sync error" + error.toString(), Toast.LENGTH_SHORT).show()} }
            )

            queue.add(stringRequest)
            i++
        }
    }


    fun changeSong() {
        /*
        MaterialFilePicker()
            .withActivity(this)
            .withRequestCode(1000)
            .withFilter(Pattern.compile(".*\\.mp3$")) // Filtering files and directories by file name using regexp
            .withFilterDirectories(false) // Set directories filterable (false by default)
            .withHiddenFiles(true) // Show hidden files and folders
            .start()
            */
        createMusicList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            1000 -> {
                val resultPath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
                if (resultPath == null) {
                    Toast.makeText(this, "Problem with file path parsing", Toast.LENGTH_LONG).show()
                } else {
                    if (resultCode == Activity.RESULT_OK)
                        path = resultPath
                }
            }

            FILE_SYSTEM_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    path = data!!.getStringExtra("path")!!
                    runOnUiThread { Toast.makeText(this, "The $path", Toast.LENGTH_SHORT).show() }
                    runSongServer(path)
                    runOnUiThread { Toast.makeText(this, "Server run", Toast.LENGTH_SHORT).show() }
                    mediaplayer = MediaPlayer.create(this, Uri.parse(path))
                }
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