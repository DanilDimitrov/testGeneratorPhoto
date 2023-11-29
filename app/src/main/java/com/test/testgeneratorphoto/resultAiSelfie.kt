package com.test.testgeneratorphoto

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.test.testgeneratorphoto.databinding.ActivityResultAiSelfieBinding
import java.io.IOException

class resultAiSelfie : AppCompatActivity() {
    lateinit var bind: ActivityResultAiSelfieBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityResultAiSelfieBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val receivedBitmap = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(receivedBitmap)
        Log.i("imageUri", imageUri.toString())

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(bind.imageButton9) // Ваш ImageView
        }

        bind.button3.setOnClickListener {
            val replay = Intent(this, aiSelfiies::class.java)
            startActivity(replay)
            finish()
        }

        bind.button5.setOnClickListener {
            val resolver = contentResolver
            val contentValue = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            val imageUriSaved = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)
            if (imageUriSaved != null) {
                try {
                    resolver.openOutputStream(imageUriSaved)?.use { outputStream ->
                        val inputStream = resolver.openInputStream(imageUri)
                        inputStream?.use { input ->
                            val buffer = ByteArray(1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } > 0) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    // Уведомление пользователя об успешном сохранении
                    Toast.makeText(this, "Image saved to gallery!", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    // Обработка ошибки в случае неудачного сохранения
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }
}