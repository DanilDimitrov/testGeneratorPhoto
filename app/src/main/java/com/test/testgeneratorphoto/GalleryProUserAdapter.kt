package com.test.testgeneratorphoto
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GalleryProUserAdapter(private val urlsImages: ArrayList<String>) : RecyclerView.Adapter<GalleryProUserAdapter.StyleHolder>() {
    private var onItemClick: ((String) -> Unit)? = null

    class StyleHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val itemImageView: ImageView = item.findViewById(R.id.imageGallery)

        fun bind(urlImage: String?) {
            if (urlImage != null) {
                itemView.tag = urlImage

                itemImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(itemImageView)
                    .load(urlImage)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(itemImageView)
            }
        }
    }

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item_adapter, parent, false)
        return StyleHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: StyleHolder, position: Int) {
        if (position < urlsImages.size) {
            val model = urlsImages[position]
            holder.bind(model)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(holder.itemView.tag.toString())
            }
        } else {
            // Если данных меньше, оставляем элементы пустыми
            holder.bind(null)
            holder.itemView.setOnClickListener(null)
        }
    }

}
