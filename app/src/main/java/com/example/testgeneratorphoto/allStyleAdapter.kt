package com.example.testgeneratorphoto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class AllStyleAdapter(var styles: List<Model>) :
    RecyclerView.Adapter<AllStyleAdapter.ViewHolder>() {
    private var onItemClick: ((Model) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val allStyleMainImage: ImageView = itemView.findViewById(R.id.allStyleMainImage)
        val allStyleMainText: TextView = itemView.findViewById(R.id.allStyleMainText)

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
        holder.allStyleMainText.text = style.styleName
        Glide.with(holder.allStyleMainImage)
            .asGif()
            .load(style.preview.toString())
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(holder.allStyleMainImage)

    }

    fun setOnItemClickListener(listener: (Model) -> Unit) {
        onItemClick = listener
    }

    override fun getItemCount(): Int {
        return styles.size
    }
}

