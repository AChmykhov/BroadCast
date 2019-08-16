package com.example.broadcast

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.MediaRecorder
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_sync.*



class SyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
        ActivityCompat.requestPermissions(this, permissions, 200)
        val recorder = MediaRecorder()
        startRecording.setOnClickListener {
            val root = Environment.getRootDirectory().absolutePath
            val dir = root + "/Music/song.3gp"
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            recorder.setOutputFile(dir)
            recorder.prepare()
            recorder.start()
        }
        stopRecording.setOnClickListener{
            recorder.stop()
            recorder.release()
        }


    }


    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == 200) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }
}
