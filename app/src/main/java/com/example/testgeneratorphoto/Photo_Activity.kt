package com.example.testgeneratorphoto
import com.example.testgeneratorphoto.databinding.ActivityPhotoBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.FileAsyncHttpResponseHandler
import com.squareup.picasso.Picasso
import cz.msebera.android.httpclient.Header
import java.io.File

class Photo_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var bind: ActivityPhotoBinding

        super.onCreate(savedInstanceState)
        bind = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val imageUrl = intent.getStringExtra("imageUrl")
        runOnUiThread {Picasso.get().load(imageUrl).into(bind.Photo) }

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
    }
}