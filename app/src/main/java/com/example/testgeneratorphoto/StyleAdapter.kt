import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.testgeneratorphoto.Model
import com.example.testgeneratorphoto.R

class StyleAdapter(var styles: List<Model>, private var category: String) :
    RecyclerView.Adapter<StyleAdapter.ViewHolder>() {
    private var onItemClick: ((Model) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val styleImageView: ImageView = itemView.findViewById(R.id.styleMainImage)
        val styleNameTextView: TextView = itemView.findViewById(R.id.styleMainText)

        init {
            itemView.setOnClickListener {
                val prompt = styles[adapterPosition]
                onItemClick?.invoke(prompt)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_style, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val style = styles[position]
        holder.styleNameTextView.text = ""
        Glide.with(holder.styleImageView)
            .asDrawable()
            .load(style.preview.toString())
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(holder.styleImageView)

        // Установите категорию для элемента
       style.category = category
    }

    fun setOnItemClickListener(listener: (Model) -> Unit) {
        onItemClick = listener
    }

    override fun getItemCount(): Int {
        return styles.size
    }
}

