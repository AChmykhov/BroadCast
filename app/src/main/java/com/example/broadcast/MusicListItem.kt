package com.example.broadcast

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.swipe.adapters.BaseSwipeAdapter
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup



class MusicListItem(private val mContext: Context) : BaseSwipeAdapter() {

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipeMusicItem
    }

    //ATTENTION: Never bind listener or fill values in generateView.
    //           You have to do that in fillValues method.
    override fun generateView(position: Int, parent: ViewGroup): View {
        return LayoutInflater.from(mContext).inflate(R.layout.activity_music_list_item, null)
    }

    override fun fillValues(position: Int, convertView: View) {
        val t = convertView.findViewById(R.id.position) as TextView
        t.text = (position + 1).toString() + "."
    }

    override fun getCount(): Int {
        return 50
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
