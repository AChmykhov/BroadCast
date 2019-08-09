package com.example.broadcast

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.MediaPlayer
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
    fun Play(view: View){
        var mediaPlayer: MediaPlayer? = MediaPlayer.create(this, R.raw.audio_test)
        mediaPlayer?.start()
    }
    fun Stop(view:View){
        var mediaPlayer: MediaPlayer? = MediaPlayer.create(this, R.raw.audio_test)
        mediaPlayer?.release()

    }
    fun Resume(view: View){
        var mediaPlayer: MediaPlayer? = MediaPlayer.create(this, R.raw.audio_test)
        mediaPlayer?.start()
    }
}
