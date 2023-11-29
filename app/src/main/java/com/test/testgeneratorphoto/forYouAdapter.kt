package com.test.testgeneratorphoto
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ForYouAdapter(private val models: List<Model>): RecyclerView.Adapter<ForYouAdapter.StyleHolder>() {
    private var onItemClick: ((Model) -> Unit)? = null

    class StyleHolder(item: View) : RecyclerView.ViewHolder(item){
        private val itemImageView: ImageView = item.findViewById(R.id.forYouImage)
        private val itemTextView: TextView = item.findViewById(R.id.forYouText)

        fun bind(model: Model){
            itemView.tag = model
            itemTextView.text =""

            itemImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(itemImageView)
                .asDrawable()
                .load(model.preview.toString())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(itemImageView)

        }
    }
    fun setOnItemClickListener(listener: (Model) -> Unit) {
        onItemClick = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.for_you_adapter, parent, false)
        return StyleHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: StyleHolder, position: Int) {
        val model = models[position]
        holder.bind(model)


        holder.itemView.setOnClickListener {
            val prompt = holder.itemView.tag as Model
            onItemClick?.invoke(prompt)
        }
    }
}