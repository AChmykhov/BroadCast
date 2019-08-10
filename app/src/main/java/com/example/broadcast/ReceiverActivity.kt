package com.example.broadcast

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReceiverActivity : AppCompatActivity() {

    companion object {
        const val IPPort = "IP:Port_of_connection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)
        bar()

        val data = intent.getStringExtra(IPPort)
        val textview = findViewById<TextView>(R.id.IPPortReceiverTextView)
        textview.setText(data)

        val middle: Int = findViewById<SeekBar>(R.id.DelayBar).max / 2
        findViewById<SeekBar>(R.id.DelayBar).progress = middle
        val min = findViewById<TextView>(R.id.MinimumTextView)
        min.setText((-middle).toString())
        val max = findViewById<TextView>(R.id.MaximumTextView)
        max.setText(middle.toString())
    }

    private fun bar() {
        var delaybar = findViewById<SeekBar>(R.id.DelayBar)

        delaybar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                    var delaytext = findViewById<TextView>(R.id.DelayTextView)
                    val delay: Int = progress - delaybar.max / 2
                    delaytext.setText(delay.toString())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )
    }

    fun getDelay(): Int {
        val delay = findViewById<TextView>(R.id.DelayTextView)
        return Integer.parseInt(delay.text.toString())
    }
}
