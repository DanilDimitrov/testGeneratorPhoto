package com.example.testgeneratorphoto

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class AllStyleBottomAdapter(var styles: List<Model>) :
    RecyclerView.Adapter<AllStyleBottomAdapter.ViewHolder>() {
    private var onItemClick: ((Model) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val allStyleMainImage: ImageView = itemView.findViewById(R.id.allStyleMainImage)
        val allStyleMainText: TextView = itemView.findViewById(R.id.allStyleMainText)
        val playerView = itemView.findViewById<PlayerView>(R.id.video)


        init {
            itemView.setOnClickListener {
                val prompt = styles[adapterPosition]
                onItemClick?.invoke(prompt)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.style_for_all_styles, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val style = styles[position]
        holder.allStyleMainText.text =""
        if (".mp4" in style.preview.toString()) {
            holder.allStyleMainImage.visibility = View.INVISIBLE
            val mediaItem = MediaItem.fromUri(style.preview.toString())
            val exoPlayer = SimpleExoPlayer.Builder(holder.itemView.context).build()
            holder.playerView.player = exoPlayer
            // Подготовка ExoPlayer
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()


        }else{
            Glide.with(holder.allStyleMainImage)

                .load(style.preview.toString())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(holder.allStyleMainImage)
        }

    }

    fun setOnItemClickListener(listener: (Model) -> Unit) {
        onItemClick = listener
    }

    override fun getItemCount(): Int {
        return styles.size
    }
}

