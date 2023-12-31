package com.example.testgeneratorphoto
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.compose.material.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class HeaderAdapter(private val models: ArrayList<Model>): RecyclerView.Adapter<HeaderAdapter.StyleHolder>() {
    private var onItemClick: ((Model) -> Unit)? = null

    class StyleHolder(item: View) : RecyclerView.ViewHolder(item){
        private val itemImageView: ImageView = item.findViewById(R.id.imageView6)
        private val itemTextView: TextView = item.findViewById(R.id.textView5)
        val tryNow: AppCompatButton = item.findViewById(R.id.tryStyleHeader)



        fun bind(model: Model){
            itemView.tag = model
            itemTextView.text = ""

            itemImageView.scaleType = ImageView.ScaleType.FIT_XY
            Glide.with(itemImageView)
                .asDrawable()
                .load(model.previewHeader.toString())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(itemImageView)

        }
    }
    fun setOnItemClickListener(listener: (Model) -> Unit) {
        onItemClick = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleHolder {
         val view = LayoutInflater.from(parent.context).inflate(R.layout.header_item, parent, false)
         return StyleHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: StyleHolder, position: Int) {
        val model = models[position]
        holder.bind(model)


        holder.tryNow.setOnClickListener {
            val prompt = holder.itemView.tag as Model
            onItemClick?.invoke(prompt)
        }
    }
}