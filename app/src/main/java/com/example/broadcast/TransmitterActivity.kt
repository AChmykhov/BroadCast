package com.example.broadcast

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TransmitterActivity : AppCompatActivity() {

    fun getData(): String {
        return "123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmitter)
        findViewById<TextView>(R.id.ShowIPPortTextView).setText(getData())
    }

    fun exitFromParty(view: View) {
        // PUT YOUR CODE HERE
    }
}
