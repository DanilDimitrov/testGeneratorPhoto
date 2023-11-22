package com.example.testgeneratorphoto

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.testgeneratorphoto.databinding.ActivityGalleryPhotoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.FileAsyncHttpResponseHandler
import com.squareup.picasso.Picasso
import cz.msebera.android.httpclient.Header
import java.io.File

class GalleryPhoto : AppCompatActivity() {
    lateinit var bind: ActivityGalleryPhotoBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityGalleryPhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = Firebase.auth

        val imageUrl = intent.getStringExtra("imageUrl")
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("Users")
        val user = auth.currentUser

        runOnUiThread {
            Picasso.get().load(imageUrl).into(bind.Photo)
        }

        // Обработчик нажатия на кнопку "Поделиться"
        bind.SharePhoto.setOnClickListener {

            // Создание Intent для отправки текста
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, imageUrl)
                type = "text/plain"
            }

            // Запуск окна "Поделиться"
            startActivity(Intent.createChooser(shareIntent, "Поделиться с помощью"))
        }

        bind.Save.setOnClickListener{

            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val client = AsyncHttpClient()
            client.get(imageUrl, object : FileAsyncHttpResponseHandler(directory) {
                override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable?, file: File?) {
                    // Уведомляем пользователя о неудачной загрузке
                    Toast.makeText(this@GalleryPhoto, "Failed to download image", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(statusCode: Int, headers: Array<Header>?, file: File) {
                    // Уведомляем пользователя о успешной загрузке
                    Toast.makeText(this@GalleryPhoto, "Image downloaded and saved!", Toast.LENGTH_SHORT).show()
                }
            })
        }
        bind.more.setOnClickListener {bind.moreImage.visibility = View.VISIBLE
        }
        bind.cancel.setOnClickListener { bind.moreImage.visibility = View.INVISIBLE }
        bind.goToHome.setOnClickListener {
            val goHomeIntent = Intent(this, changePhoto::class.java)
            startActivity(goHomeIntent)
            finish()
        }

        fun deleteImageUrlFromUserDocument(userId: String, imageUrlToDelete: String) {
            val userDocRef = usersCollection.document(userId)

            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val imagesUrls = documentSnapshot.get("imagesUrls") as? ArrayList<String>
                        imagesUrls?.let {
                            // Удаляем нужную ссылку из массива
                            it.remove(imageUrlToDelete)

                            // Обновляем документ с обновленным массивом
                            userDocRef.update("imagesUrls", it)
                                .addOnSuccessListener {
                                    // Успешно обновлено
                                }
                                .addOnFailureListener { e ->
                                    // Обработка ошибки при обновлении
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Обработка ошибки при чтении документа
                }
        }

        bind.Delete.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.alert_delete_art)

            val no = dialog.findViewById<TextView>(R.id.no)
            val Yes = dialog.findViewById<TextView>(R.id.Yes)

            no.setOnClickListener {
                dialog.dismiss()
            }

            Yes.setOnClickListener {
                deleteImageUrlFromUserDocument(user!!.uid, imageUrl!!)
                Toast.makeText(this, "Art deleted successful", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        }


    }

}