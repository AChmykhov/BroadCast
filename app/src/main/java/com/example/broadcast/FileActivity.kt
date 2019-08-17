package com.example.broadcast

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class FileActivity : AppCompatActivity() {

    lateinit var adapter: MusicListAdapter
    var handler = fun(path: String) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
    }

    fun createMusicList() {
        var musicList = findViewById<ListView>(R.id.MusicList)
        adapter = MusicListAdapter(this)
        getMusic()
        musicList.adapter = adapter


        musicList.setOnItemClickListener { parent, view, position, id ->
            var intent = Intent()
            intent.setData(Uri.parse(adapter.objects[position].location))
            setResult(RESULT_OK, intent)
            onBackPressed()
        }
    }

    fun onExit(view: View) {
        var intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        onBackPressed()
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    fun getMusic() {
        var contecntResolver = getContentResolver()
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver.query(songUri, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst()) {
            var songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            var songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            do {
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentLocation = songCursor.getString(songLocation)
                adapter.addItem(currentTitle, currentArtist, currentLocation)
            } while (songCursor.moveToNext())
        }
    }

}
