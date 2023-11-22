package com.example.testgeneratorphoto
import android.app.Dialog
import android.content.Intent
import com.example.testgeneratorphoto.databinding.ActivityPhotoBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.FileAsyncHttpResponseHandler
import com.squareup.picasso.Picasso
import cz.msebera.android.httpclient.Header
import java.io.File

class Photo_Activity : AppCompatActivity() {
    lateinit var bind: ActivityPhotoBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bind = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val imageUrl = intent.getStringExtra("imageUrl")
        val styleName = intent.getStringExtra("styleName")

        val isUserGallery = intent.getBooleanExtra("isUserGallery", false)
        Log.i("isUserGallery", isUserGallery.toString())

        if (styleName != null) {
            Log.i("styleName", styleName)
        }

        runOnUiThread {
            Picasso.get().load(imageUrl).into(bind.Photo)
            bind.textView10.text = "Style: $styleName"
        }

        bind.Share.setOnClickListener {

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, imageUrl)
                type = "text/plain"
            }

            startActivity(Intent.createChooser(shareIntent, "Поделиться с помощью"))
        }

        bind.Download.setOnClickListener{

            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val client = AsyncHttpClient()
            client.get(imageUrl, object : FileAsyncHttpResponseHandler(directory) {
                override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable?, file: File?) {
                    // Уведомляем пользователя о неудачной загрузке
                    Toast.makeText(this@Photo_Activity, "Failed to download image", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(statusCode: Int, headers: Array<Header>?, file: File) {
                    // Уведомляем пользователя о успешной загрузке
                    Toast.makeText(this@Photo_Activity, "Image downloaded and saved!", Toast.LENGTH_SHORT).show()
                }
            })
        }

        bind.switch2.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Кнопка включена, выполняйте необходимые действия
            } else {
                // Кнопка выключена, выполняйте необходимые действия
            }
        }

        bind.more.setOnClickListener {bind.moreImage.visibility = View.VISIBLE
        }
        bind.cancel.setOnClickListener { bind.moreImage.visibility = View.INVISIBLE }
        bind.goToHome.setOnClickListener {
            if(!isUserGallery){
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.alert_unsaved_image)

                val Yes = dialog.findViewById<TextView>(R.id.Yes)
                val no = dialog.findViewById<TextView>(R.id.no)

                Yes.setOnClickListener {

                    dialog.dismiss()
                    val goHomeIntent = Intent(this, changePhoto::class.java)
                    startActivity(goHomeIntent)
                    finish()
                }
                no.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }else {
                val goHomeIntent = Intent(this, changePhoto::class.java)
                startActivity(goHomeIntent)
                finish()
            }
        }
    }
}