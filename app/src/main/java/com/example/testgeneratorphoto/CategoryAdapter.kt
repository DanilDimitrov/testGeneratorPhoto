import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testgeneratorphoto.Model
import com.example.testgeneratorphoto.R

class CategoryAdapter(private val categories: List<String>,  private val allStyles: List<Model>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val styleRecyclerView: RecyclerView = itemView.findViewById(R.id.categoryRecyclerView)
        val seeAll: ImageButton = itemView.findViewById(R.id.imageButton)


        val styleAdapter = StyleAdapter(emptyList()) // Пустой список стилей

        init {
            styleRecyclerView.adapter = styleAdapter
            styleRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryTextView.text = category
        holder.seeAll.isVisible = true

        // Здесь вы должны установить список стилей для этой категории
        val stylesForCategory = getStylesForCategory(category)
        holder.styleAdapter.styles = stylesForCategory
        holder.styleAdapter.notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return categories.size
    }

    // Метод для получения списка стилей для категории
    private fun getStylesForCategory(category: String): List<Model> {
        return allStyles.filter { it.category == category }

    }
}
