package com.example.broadcast

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class MusicListAdapter : BaseAdapter {

    var objects = emptyArray<MusicListItem>()
    var handler = fun(view: View, path: String) {}
    var LInflater: LayoutInflater


    constructor(mContext: Context, func: (view: View, path: String) -> Unit) {

        LInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    // кол-во элементов
    override fun getCount(): Int {
        return objects.size
    }

    // элемент по позиции
    override fun getItem(position: Int): Any {
        return objects[position]
    }

    // id по позиции
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // пункт списка
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView ?: LInflater.inflate(R.layout.activity_music_list_item, parent, false)

        val item = getMusicListItem(position)

        (view.findViewById(R.id.titleTextView) as TextView).setText(item.title)
        (view.findViewById(R.id.artistTextView) as TextView).setText(item.artist)

        return view
    }

    fun addItem(title: String, artist: String, location: String) {
        val item = MusicListItem(title, artist, location)
        objects += item
    }

    fun getMusicListItem(position: Int): MusicListItem {
        return getItem(position) as MusicListItem
    }
}
