package com.app.imagevideoactivity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.imagevideoactivity.R
import com.app.imagevideoactivity.model.MediaItem
import com.bumptech.glide.Glide

class MediaAdapter(private val mediaList: List<MediaItem>, private val onClick: (MediaItem) -> Unit) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.media_thumbnail)
        val imgIcon: ImageView = view.findViewById(R.id.imgIcon)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]
        holder.imgIcon.isVisible = media.isVideo
        Glide.with(holder.thumbnail.context).load(media.path).into(holder.thumbnail)
        holder.itemView.setOnClickListener { onClick(media) }
    }

    override fun getItemCount() = mediaList.size
}
