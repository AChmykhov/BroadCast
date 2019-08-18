package com.example.broadcast

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_receiver.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class ReceiverActivity : AppCompatActivity() {

    private var mediaplayer = MediaPlayer()
    private var muted = false
    lateinit var server: receiverServer
    private var step: Int = 20
    private var step_latency: Int = 50
    var delaybar: SeekBar? = null

    inner class receiverServer @Throws(IOException::class) constructor() : NanoHTTPD(63343) {

        init {
            start(SOCKET_READ_TIMEOUT, false)
        }

        fun stpServer() {
            val queue = Volley.newRequestQueue(this@ReceiverActivity)
            val ip = getData()
            val stringRequest = StringRequest(Request.Method.POST, "http://$ip:63342/?Exit=true",
                com.android.volley.Response.Listener { response ->
                    this.stop()
                },
                com.android.volley.Response.ErrorListener { error ->
                    runOnUiThread {
                        Toast.makeText(
                            this@ReceiverActivity,
                            "exit error " + error.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            queue.add(stringRequest)
        }

        override fun serve(session: IHTTPSession): Response {
            val params = session.parameters
            runOnUiThread { Toast.makeText(this@ReceiverActivity, "Resume signal received", Toast.LENGTH_SHORT).show() }
            runOnUiThread { Toast.makeText(this@ReceiverActivity, params.toString(), Toast.LENGTH_SHORT).show() }
            if (params.containsKey("timeToStart")) {
                params["timeToStart"]?.get(0)?.let {
                    startPlaying(it)
                }
            }
            if (params.containsKey("timeToStop")) {
                stopPlaying()
            }

            if (params.containsKey("currentTime")) {
                val time = System.currentTimeMillis()
                return newFixedLengthResponse(time.toString())
            }

            return newFixedLengthResponse("Hello World!")
        }

    }

    companion object {
        const val ipPort = "IP:Port_of_connection"
    }

    fun stopPlaying() {
        runOnUiThread { Toast.makeText(this, "Stop signal understood", Toast.LENGTH_SHORT).show() }
        mediaplayer.pause()
    }

    fun startPlaying(Time: String) {
        runOnUiThread { Toast.makeText(this, "Resume signal understood", Toast.LENGTH_SHORT).show() }
        val time = System.currentTimeMillis()
        //Thread.sleep(Time.toLong() - time)
        mediaplayer.start()
        mediaplayer.seekTo(Time.toInt())
    }

    fun getDelay(): Int {
        val delay = findViewById<TextView>(R.id.DelayTextView)
        return Integer.parseInt(delay.text.toString())
    }

    fun getData(): String? {
        return intent.getStringExtra(ipPort)
    }

    fun runServer() {
        server = receiverServer()
    }

    fun plusProgress(view: View) {
        if (delaybar!!.max > delaybar!!.progress + step)
            delaybar!!.setProgress(delaybar!!.progress + step)
        else
            delaybar!!.setProgress(delaybar!!.max)
    }

    fun minusProgress(view: View) {
        if (0 < delaybar!!.progress - step)
            delaybar!!.setProgress(delaybar!!.progress - step)
        else
            delaybar!!.setProgress(0)
    }

    fun bar() {
        var prevdelay = delaybar!!.progress
        delaybar!!.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                    val delaytext = findViewById<TextView>(R.id.DelayTextView)
                    val delay: Int = progress - delaybar!!.max / 2
                    delaytext.setText(delay.toString())
                    mediaplayer.seekTo(mediaplayer.currentPosition + delaybar!!.progress - prevdelay + step_latency)
                    prevdelay = delaybar!!.progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)
        delaybar = findViewById<SeekBar>(R.id.DelayBar)
        val textview = findViewById<TextView>(R.id.IPPortReceiverTextView)
        textview.setText(getData())
        val thisActivity = this@ReceiverActivity
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
        val ip = getData()
        val urlStr = "http://$ip:63342/song.mp3/?Song=true"
        try {
            DownloadFileFromURL().execute(urlStr)
        } catch (ioe: Exception) {
            Toast.makeText(this, "Problem with downloading song \n Exception: $ioe", Toast.LENGTH_LONG).show()
            finish()
        }

        val middle: Int = findViewById<SeekBar>(R.id.DelayBar).max / 2
        findViewById<SeekBar>(R.id.DelayBar).progress = middle
        val min = findViewById<TextView>(R.id.MinimumTextView)
        min.setText((-middle).toString())
        val max = findViewById<TextView>(R.id.MaximumTextView)
        max.setText(middle.toString())
        runServer()
        Toast.makeText(this, "Server run", Toast.LENGTH_SHORT).show()
        bar()
    }

    fun onMute(@Suppress("UNUSED_PARAMETER") view: View) {
        if (muted) {
            mediaplayer.setVolume(1.0.toFloat(), 1.0.toFloat())
            muted = false
            MUTE.setImageResource(R.drawable.unmutebutton)
        } else {
            mediaplayer.setVolume(0.0.toFloat(), 0.0.toFloat())
            muted = true
            MUTE.setImageResource(R.drawable.mutebutton)
        }
    }

    fun onExit(@Suppress("UNUSED_PARAMETER") view: View) {
        close()
    }

    override fun onBackPressed() {
        close()
        super.onBackPressed()
    }

    fun close() {
        server.stpServer()
        mediaplayer.stop()
        finish()
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


    internal inner class DownloadFileFromURL : AsyncTask<String, String, String?>() {

        override fun onPreExecute() {}

        override fun doInBackground(vararg f_url: String): String? {
            try {
                val url = URL(f_url[0])
                val conection = url.openConnection()
                conection.connect()

                var dir = File(Environment.getExternalStorageDirectory().toString() + "/Music")
                if (!dir.exists())
                    dir.mkdirs()

                val input = BufferedInputStream(url.openStream(), 8192)
                val output = FileOutputStream(
                    Environment.getExternalStorageDirectory().toString() + "/Music/song.mp3"
                )

                val data = ByteArray(1024)
                var total: Long = 0
                var count = input.read(data)
                while (count != -1) {
                    total += count.toLong()
                    output.write(data, 0, count)
                    count = input.read(data)
                }

                output.flush()
                output.close()
                input.close()

            } catch (e: Exception) {
                Log.e("Error: ", "$e")
            }

            return null
        }

        override fun onProgressUpdate(vararg progress: String) {}

        override fun onPostExecute(fileUri: String?) {
            val root = Environment.getExternalStorageDirectory().toString()
            val thisActivity = this@ReceiverActivity
            mediaplayer = MediaPlayer.create(thisActivity, Uri.parse(root + "/Music/song.mp3"))
            var play = findViewById<ImageButton>(R.id.MUTE)
            play.visibility = View.VISIBLE
            val queue = Volley.newRequestQueue(this@ReceiverActivity)
            var Response_to_request = ""
            val ip = getData()
            val stringRequest = StringRequest(
                Request.Method.GET, "http://$ip:63342?Downloaded=true",
                Response.Listener<String> { response ->
                    Response_to_request = response
                },
                Response.ErrorListener { Response_to_request = "Error" })
            queue.add(stringRequest)
        }

    }
}