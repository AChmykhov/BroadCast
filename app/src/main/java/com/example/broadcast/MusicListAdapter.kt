package com.example.broadcast

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.daimajia.swipe.adapters.BaseSwipeAdapter



class MusicListAdapter(private val mContext: Context) : BaseSwipeAdapter() {

    var objects = emptyArray<MusicListItem>()
    var handler = fun(view: View, path: String) {}

    fun addItem(title: String, artist: String, location: String, method: (view: View, path: String) -> Unit) {
        val item = MusicListItem(title, artist, location)
        handler = method
        objects += item
    }

    fun addItem(item: MusicListItem) {
        objects += item
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipeMusicItem
    }

    //ATTENTION: Never bind listener or fill values in generateView.
    //           You have to do that in fillValues method.
    override fun generateView(position: Int, parent: ViewGroup): View {
        return LayoutInflater.from(mContext).inflate(R.layout.activity_music_list_item, null)
    }

    override fun fillValues(position: Int, convertView: View) {
        var title = convertView.findViewById<TextView>(R.id.titleTextView)
        var artist = convertView.findViewById<TextView>(R.id.artistTextView)
        var button = convertView.findViewById<Button>(R.id.chooseButton)
        title.text = objects[position].title
        artist.text = objects[position].artist
        button.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(view: View) {
                    handler(view, objects[position].location)
                }
            }
        )
    }

    override fun getCount(): Int {
        return objects.size
    }

    override fun getItem(position: Int): Any? {
        if (position >= objects.size)
            return null
        return objects[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
