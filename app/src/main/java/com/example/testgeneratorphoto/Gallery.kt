package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testgeneratorphoto.databinding.ActivityGalleryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Gallery : AppCompatActivity() {
    lateinit var bind: ActivityGalleryBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = Firebase.auth
        val user = auth.currentUser
        updateUI(user)

        bind.button6.setOnClickListener {
            onBackPressed()
        }
        bind.imageButton11.setOnClickListener {
            onBackPressed()
        }
        bind.button61.setOnClickListener {
            val toHome = Intent(this, changePhoto::class.java)
            startActivity(toHome)
            finish()
        }
        bind.imageButton10.setOnClickListener {
            val toSetting = Intent(this, properties::class.java)
            startActivity(toSetting)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.i("USER INFORMATION", user?.isAnonymous.toString())
        if (user?.isAnonymous == true) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("Users").document(uid)

            // Проверяем, существует ли документ
            userDoc.get()
                .addOnSuccessListener { documentSnapshot ->
                        Log.i("USER INFORMATION", "Document already exists")

                        val isUserAccessGallery = documentSnapshot.getBoolean("isUserAccessGallery") ?: false
                        Log.i("USER INFORMATION", "isUserPaid: $isUserAccessGallery")

                        // В зависимости от значения isUserPaid, выводим соответствующее сообщение
                        if (isUserAccessGallery) {
                            Log.i("USER INFORMATION", "The user is paid.")
                            bind.notProUserProfile.visibility = View.INVISIBLE
                            bind.ProUserProfile.visibility = View.VISIBLE
                            bind.galleryRecycler.visibility = View.VISIBLE

                            //получение urls пользователя
                            val imageList = documentSnapshot.get("imagesUrls") as? ArrayList<String>

                            if (imageList != null) {
                                val adapter = GalleryProUserAdapter(imageList)
                                adapter.setOnItemClickListener { url->
                                    val toPhoto = Intent(this, GalleryPhoto::class.java)
                                    toPhoto.putExtra("imageUrl", url)
                                    startActivity(toPhoto)

                                }
                                bind.galleryRecycler.adapter = adapter
                                bind.galleryRecycler.layoutManager = GridLayoutManager(this, 2)
                                if (imageList.isEmpty()){
                                    Log.i("imageList.isEmpty()", imageList.toString())

                                    bind.ProUserProfile.visibility = View.VISIBLE
                                    bind.galleryRecycler.visibility = View.INVISIBLE

                                }
                                else {
                                    bind.ProUserProfile.visibility = View.INVISIBLE
                                    bind.galleryRecycler.visibility = View.VISIBLE
                                }

                            }

                        } else {
                            Log.i("USER INFORMATION", "The user is not paid.")
                            bind.notProUserProfile.visibility = View.VISIBLE
                            bind.galleryRecycler.visibility = View.INVISIBLE
                            bind.ProUserProfile.visibility = View.INVISIBLE
                        }

                }
                .addOnFailureListener {
                    // Ошибка получения документа
                    Log.e("USER INFORMATION", "Failed to get document")
                }
        }
        else{
            val uid = user!!.uid
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("Users").document(uid)

            // Проверяем, существует ли документ
            userDoc.get()
                .addOnSuccessListener { documentSnapshot ->
                    Log.i("USER INFORMATION", "Document already exists")

                    val isUserAccessGallery = documentSnapshot.getBoolean("isUserAccessGallery") ?: false
                    Log.i("USER INFORMATION", "isUserPaid: $isUserAccessGallery")

                    // В зависимости от значения isUserPaid, выводим соответствующее сообщение
                    if (isUserAccessGallery) {
                        Log.i("USER INFORMATION", "The user is paid.")
                        bind.notProUserProfile.visibility = View.INVISIBLE
                        bind.ProUserProfile.visibility = View.VISIBLE
                        bind.galleryRecycler.visibility = View.VISIBLE

                        //получение urls пользователя
                        val imageList = documentSnapshot.get("imagesUrls") as? ArrayList<String>

                        if (imageList != null) {
                            val adapter = GalleryProUserAdapter(imageList)
                            adapter.setOnItemClickListener { url->
                                val toPhoto = Intent(this, GalleryPhoto::class.java)
                                toPhoto.putExtra("imageUrl", url)
                                startActivity(toPhoto)

                            }
                            bind.galleryRecycler.adapter = adapter
                            bind.galleryRecycler.layoutManager = GridLayoutManager(this, 2)
                            if (imageList.isEmpty()){
                                Log.i("imageList.isEmpty()", imageList.toString())

                                bind.ProUserProfile.visibility = View.VISIBLE
                                bind.galleryRecycler.visibility = View.INVISIBLE

                            }
                            else {
                                bind.ProUserProfile.visibility = View.INVISIBLE
                                bind.galleryRecycler.visibility = View.VISIBLE
                            }

                        }

                    } else {
                        Log.i("USER INFORMATION", "The user is not paid.")
                        bind.notProUserProfile.visibility = View.VISIBLE
                        bind.galleryRecycler.visibility = View.INVISIBLE
                        bind.ProUserProfile.visibility = View.INVISIBLE
                    }

                }
                .addOnFailureListener {
                    // Ошибка получения документа
                    Log.e("USER INFORMATION", "Failed to get document")
                }
        }
    }
}
