package com.example.broadcast

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun StartParty(@Suppress("UNUSED_PARAMETER")view: View) {
        startActivity(Intent(this, TransmitterActivity::class.java))
    }

    fun joinParty(@Suppress("UNUSED_PARAMETER")view: View) {
        val data = findViewById<TextInputEditText>(R.id.IPPoirtInput)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (data.text.toString() == "") {
            Toast.makeText(this, "No IP address entered", Toast.LENGTH_LONG).show()
        } else {
            if (!(wifiManager.isWifiEnabled)) {
                Toast.makeText(this, "No connection to Wi-Fi network", Toast.LENGTH_LONG).show()
            } else {
                val receiverIntent = Intent(this, ReceiverActivity::class.java)
                receiverIntent.putExtra(ReceiverActivity.ipPort, data.text.toString())
                startActivity(receiverIntent)
            }
        }
    }
}