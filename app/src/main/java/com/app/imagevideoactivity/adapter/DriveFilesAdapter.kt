package com.app.imagevideoactivity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.imagevideoactivity.R
import com.app.imagevideoactivity.model.DriveFile
import com.app.imagevideoactivity.model.MediaItem
import com.bumptech.glide.Glide

class DriveFilesAdapter(private val context: Context, private val files: List<DriveFile>, private val onClick: (DriveFile) -> Unit) :
    RecyclerView.Adapter<DriveFilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drive_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]

        if (file.isImage() && file.thumbnailLink != null) {

            // Show image using Glide
            holder.imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(file.thumbnailLink)
                .into(holder.imageView)
        }
        else{
            holder.imageView.isVisible =false
        }
        holder.itemView.setOnClickListener {
            onClick(file)
        }
    }



    override fun getItemCount(): Int = files.size


}
