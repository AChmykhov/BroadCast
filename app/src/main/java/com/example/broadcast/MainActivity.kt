package com.example.broadcast

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun StartParty(view: View) {
        startActivity(Intent(this, TransmitterActivity::class.java))
    }

    fun JoinParty(view: View) {
        var ReceiverIntent = Intent(this, ReceiverActivity::class.java)
        val data = findViewById(R.id.IPPoirtInput) as TextInputEditText
        ReceiverIntent.putExtra(ReceiverActivity.IPPort, data.text.toString())
        startActivity(ReceiverIntent)
    }
}