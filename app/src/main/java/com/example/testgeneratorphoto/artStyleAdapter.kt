package com.example.testgeneratorphoto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.ArrayList

class artStyleAdapter(var styles: ArrayList<artModel>):
    RecyclerView.Adapter<artStyleAdapter.ViewHolder>(){
    private var onItemClick: ((artModel) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val styleImageView: ImageView = itemView.findViewById(R.id.createArtImage)
        val styleNameTextView: TextView = itemView.findViewById(R.id.createArtText)

        init {
            itemView.setOnClickListener {
                val prompt = styles[adapterPosition]
                onItemClick?.invoke(prompt)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.art_style_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val style = styles[position]
        holder.styleNameTextView.text = style.styleName
        Glide.with(holder.styleImageView)
            .asGif()
            .load(style.preview.toString())
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(holder.styleImageView)

    }

    fun setOnItemClickListener(listener: (artModel) -> Unit) {
        onItemClick = listener
    }

    override fun getItemCount(): Int {
        return styles.size
    }
}