package com.example.testgeneratorphoto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.ArrayList

class artStyleAdapter(var styles: ArrayList<artModel>) :
    RecyclerView.Adapter<artStyleAdapter.ViewHolder>() {
    private var onItemClick: ((artModel) -> Unit)? = null
    private var selectedItem = -1 // Индекс выбранного элемента, изначально -1 (ничего не выбрано)

    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val styleImageView: ImageView = itemView.findViewById(R.id.createArtImage)
        val styleNameTextView: TextView = itemView.findViewById(R.id.createArtText)
        val card: CardView = itemView.findViewById(R.id.cardArt)

        init {
            itemView.setOnClickListener {
                val prompt = styles[adapterPosition]

                // Устанавливаем новое значение selectedItem при нажатии
                val previousSelectedItem = selectedItem
                selectedItem = adapterPosition

                // Обновляем цвет элементов RecyclerView
                notifyDataSetChanged()

                onItemClick?.invoke(prompt)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.art_style_adapter, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val style = styles[position]
        holder.styleNameTextView.text = style.styleName
        Glide.with(holder.styleImageView)
            .asGif()
            .load(style.preview)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(holder.styleImageView)

        // Устанавливаем цвет элемента в зависимости от выбранного элемента
        if (position == selectedItem) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }
    }

    fun setOnItemClickListener(listener: (artModel) -> Unit) {
        onItemClick = listener
    }

    override fun getItemCount(): Int {
        return styles.size
    }
}
