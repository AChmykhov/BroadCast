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
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class ReceiverActivity : AppCompatActivity() {

    private var mediaplayer = MediaPlayer()
    private var pause = true

    inner class receiverServer @Throws(IOException::class) constructor() : NanoHTTPD(63343) {

        init {
            start(SOCKET_READ_TIMEOUT, false)
        }

        override fun serve(session: IHTTPSession): Response {
            val params = session.parameters
            runOnUiThread {Toast.makeText(this@ReceiverActivity, "Resume signal received", Toast.LENGTH_SHORT).show()}
            runOnUiThread {Toast.makeText(this@ReceiverActivity, params.toString(), Toast.LENGTH_SHORT).show()}
            if (params.containsKey("timeToStart")) {
                params["timeToStart"]?.get(0)?.let { startPlaying(it) }
            }
            return newFixedLengthResponse("Hello World!")
        }

    }

    companion object {
        const val ipPort = "IP:Port_of_connection"
    }

    fun startPlaying(Time: String) {
        runOnUiThread {Toast.makeText(this, "Resume signal understood", Toast.LENGTH_SHORT).show()}
        val time = System.currentTimeMillis()
        Thread.sleep(Time.toLong() - time)
        mediaplayer.start()
        pause = false
    }

    fun getDelay(): Int {
        val delay = findViewById<TextView>(R.id.DelayTextView)
        return Integer.parseInt(delay.text.toString())
    }

    fun getData(): String? {
        return intent.getStringExtra(ipPort)
    }

    fun runServer() {
        receiverServer()
    }

    fun bar() {

        var delaybar = findViewById<SeekBar>(R.id.DelayBar)

        delaybar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                    val delaytext = findViewById<TextView>(R.id.DelayTextView)
                    val delay: Int = progress - delaybar.max / 2
                    delaytext.setText(delay.toString())
                    mediaplayer.seekTo(mediaplayer.currentPosition + getDelay())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)

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

        val queue = Volley.newRequestQueue(this)
        var Response_to_request = ""
        val stringRequest = StringRequest(
            Request.Method.GET, "http://$ip:63342?Downloaded=true",
            Response.Listener<String> { response ->
                Response_to_request = response
            },
            Response.ErrorListener {Response_to_request = "Error" })
        queue.add(stringRequest)

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

    fun onPlay(@Suppress("UNUSED_PARAMETER") view: View) {
        if (pause) {
            mediaplayer.start()
            pause = false
            PLAY.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            mediaplayer.pause()
            pause = true
            PLAY.setImageResource(android.R.drawable.ic_media_play)
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
            var play = findViewById<ImageButton>(R.id.PLAY)
            play.visibility = View.VISIBLE
        }

    }
}