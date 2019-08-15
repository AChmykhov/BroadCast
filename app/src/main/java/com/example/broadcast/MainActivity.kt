package com.example.broadcast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    val PICK_CONTACT_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun ScanQR(view: View) {
        val intent = Intent(this@MainActivity, ScanActivity::class.java)
        startActivityForResult(intent, PICK_CONTACT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            PICK_CONTACT_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    var data = findViewById(R.id.IPPortInput) as TextInputEditText
                    data.setText(intent?.data.toString())
                }
            }
        }
    }

    fun StartParty(view: View) {
        startActivity(Intent(this, TransmitterActivity::class.java))
    }

    fun JoinParty(view: View) {
        var ReceiverIntent = Intent(this, ReceiverActivity::class.java)
        val data = findViewById(R.id.IPPortInput) as TextInputEditText
        ReceiverIntent.putExtra(ReceiverActivity.IPPort, data.text.toString())
        startActivity(ReceiverIntent)
    }

}