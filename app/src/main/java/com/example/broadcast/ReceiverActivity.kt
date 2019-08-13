package com.example.broadcast

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.View.Z
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_receiver.*
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL


class ReceiverActivity : AppCompatActivity() {

    public var mediaplayer = MediaPlayer()
    private var pause = true

    companion object {
        const val IPPort = "IP:Port_of_connection"
    }

    fun getDelay(): Int {
        val delay = findViewById<TextView>(R.id.DelayTextView)
        return Integer.parseInt(delay.text.toString())
    }

    fun getData(): String {
        return intent.getStringExtra(IPPort).toString()
    }

    fun bar() {
        Z
        var delaybar = findViewById<SeekBar>(R.id.DelayBar)

        delaybar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                    var delaytext = findViewById<TextView>(R.id.DelayTextView)
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

        try {
            DownloadFileFromURL().execute("http://" + getData() + "/song.mp3")
        } catch (ioe: Exception) {
            mediaplayer = MediaPlayer.create(this, Uri.parse("http://d.zaix.ru/dQYH.mp3"))

            Toast.makeText(this, "$ioe", Toast.LENGTH_LONG).show()
        }
        val middle: Int = findViewById<SeekBar>(R.id.DelayBar).max / 2
        findViewById<SeekBar>(R.id.DelayBar).progress = middle
        val min = findViewById<TextView>(R.id.MinimumTextView)
        min.setText((-middle).toString())
        val max = findViewById<TextView>(R.id.MaximumTextView)
        max.setText(middle.toString())

        bar()
    }

    fun onPlay(view: View) {
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

    fun onExit(view: View) {
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
            val count: Int
            try {
                val url = URL(f_url[0])
                val conection = url.openConnection()
                conection.connect()

                val lenghtOfFile = conection.contentLength
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
            var thisActivity = this@ReceiverActivity
            mediaplayer = MediaPlayer.create(thisActivity, Uri.parse(root + "/Music/song.mp3"))
        }

    }

}