package com.example.testgeneratorphoto
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StyleAdapter(private val models: ArrayList<Model>): RecyclerView.Adapter<StyleAdapter.StyleHolder>() {
    private var onItemClick: ((Model) -> Unit)? = null

    class StyleHolder(item: View) : RecyclerView.ViewHolder(item){
        private val itemImageView: ImageView = item.findViewById(R.id.image)
        private val itemTextView: TextView = item.findViewById(R.id.StyleName)


        fun bind(model: Model){
            itemView.tag = model
            itemImageView.setImageResource(model.preview.toString().toInt())
            itemTextView.text = model.styleName.toString()

        }
    }
    fun setOnItemClickListener(listener: (Model) -> Unit) {
        onItemClick = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleHolder {
         val view = LayoutInflater.from(parent.context).inflate(R.layout.styleitem, parent, false)
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