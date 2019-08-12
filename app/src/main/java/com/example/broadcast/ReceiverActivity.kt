package com.example.broadcast

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_receiver.*

class ReceiverActivity : AppCompatActivity() {

    companion object {
        const val IPPort = "IP:Port_of_connection"
    }

    fun getDelay(): Int {
        val delay = findViewById<TextView>(R.id.DelayTextView)
        return Integer.parseInt(delay.text.toString())
    }

    fun getData(): String? {
        return intent.getStringExtra(IPPort)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)

        val textview = findViewById<TextView>(R.id.IPPortReceiverTextView)
        textview.setText(getData())

        val middle: Int = findViewById<SeekBar>(R.id.DelayBar).max / 2
        findViewById<SeekBar>(R.id.DelayBar).progress = middle
        val min = findViewById<TextView>(R.id.MinimumTextView)
        min.setText((-middle).toString())
        val max = findViewById<TextView>(R.id.MaximumTextView)
        max.setText(middle.toString())

        var mediaplayer = MediaPlayer.create(this, Uri.parse("http://d.zaix.ru/dQYH.mp3"))
        //var mediaplayer = MediaPlayer.create(this, Uri.parse("192.168.212.102:63342"))

        var pause = true

        PLAY.setOnClickListener {
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
        EXITR.setOnClickListener {
            mediaplayer.stop()
            finish()
        }

        var delaybar = findViewById<SeekBar>(R.id.DelayBar)

        delaybar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                    var delaytext = findViewById<TextView>(R.id.DelayTextView)
                    val delay: Int = progress - delaybar.max / 2
                    delaytext.setText(delay.toString())
                    mediaplayer.seekTo(mediaplayer.currentPosition+getDelay())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )

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
