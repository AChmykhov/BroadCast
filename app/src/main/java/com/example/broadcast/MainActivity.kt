package com.example.broadcast

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val thisActivity = this@MainActivity

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