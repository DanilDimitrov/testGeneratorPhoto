package com.example.testgeneratorphoto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
                val context = itemView.context

                if (adapterPosition != selectedItem) {
                    // Сбросить эффект предыдущего выбранного элемента
                    if (selectedItem != -1) {
                        val previousSelectedItem = selectedItem
                        notifyItemChanged(previousSelectedItem)
                    }

                    // Установить эффект для текущего элемента
                    val cornerRadiusDrawable = ContextCompat.getDrawable(context, R.drawable.select_item)
                    card.background = cornerRadiusDrawable

                    selectedItem = adapterPosition
                    onItemClick?.invoke(prompt)
                } else {
                    // Если элемент уже выбран, сбросить эффект и сбросить selectedItem
                    val cornerRadiusDrawable = ContextCompat.getDrawable(context, R.drawable.radio_unselect)
                    card.background = cornerRadiusDrawable
                    selectedItem = -1
                }
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
            .asDrawable()
            .load(style.preview)
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
