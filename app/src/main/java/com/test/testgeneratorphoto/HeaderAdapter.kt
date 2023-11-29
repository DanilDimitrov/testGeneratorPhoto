package com.test.testgeneratorphoto
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class HeaderAdapter(private val models: ArrayList<Model>): RecyclerView.Adapter<HeaderAdapter.StyleHolder>() {
    private var onItemClick: ((Model) -> Unit)? = null

    class StyleHolder(item: View) : RecyclerView.ViewHolder(item){
        private val itemImageView: ImageView = item.findViewById(R.id.imageView6)
        private val itemTextView: TextView = item.findViewById(R.id.textView5)
        val tryNow: AppCompatButton = item.findViewById(R.id.tryStyleHeader)
        val playerView = itemView.findViewById<PlayerView>(R.id.video)



        fun bind(model: Model){
            itemView.tag = model
            itemTextView.text = ""
            if (".mp4" in model.preview.toString()) {
                itemImageView.visibility = View.INVISIBLE
                val mediaItem = MediaItem.fromUri(model.preview.toString())
                val exoPlayer = SimpleExoPlayer.Builder(itemView.context).build()
                playerView.player = exoPlayer
                // Подготовка ExoPlayer
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()


            }else {

                itemImageView.scaleType = ImageView.ScaleType.FIT_XY
                Glide.with(itemImageView)
                    .asDrawable()
                    .load(model.previewHeader.toString())
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(itemImageView)
            }
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