package com.test.testgeneratorphoto

import Manage
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.test.testgeneratorphoto.databinding.ActivityChoseStyleBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector

class choseStyle : AppCompatActivity() {
    lateinit var bind: ActivityChoseStyleBinding
    val manage = Manage()
    var width: Int = 0
    var height: Int = 0
    val chosePhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    lateinit var promptModel: List<Model>
    lateinit var modelsInCategory: List<Model>
    lateinit var categoryName: String
    lateinit var selectedImageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        bind = ActivityChoseStyleBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        promptModel = listOf()
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        findViewById<View>(R.id.choseStyle).startAnimation(animation)

        val allStylesJson = intent.getStringExtra("styleForChose")
        selectedImageUri = intent.getParcelableExtra<Uri>("selectedImageUri")!!
        val modelsInCategoryJson = intent.getStringExtra("modelsInCategory")



// Преобразуйте JSON-строку обратно в список стилей
        val gson = Gson()
        val type = object : TypeToken<List<Model>>() {}.type
        val allStylesInCategory = gson.fromJson<List<Model>>(allStylesJson, type)
        modelsInCategory = gson.fromJson<List<Model>>(modelsInCategoryJson, type)

        Log.i("styleForChose", allStylesInCategory.toString())
        Log.i("selectedImageUri", selectedImageUri.toString())
        Log.i("modelsInCategory", modelsInCategory.toString())


        runOnUiThread {
            Glide.with(bind.aboutStyle)
                .asDrawable()
                .load(allStylesInCategory[0].preview.toString())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(bind.aboutStyle)

            bind.aboutStyleText.text = ""
        }
        val forYouLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val forYouAdapter = ForYouAdapter(modelsInCategory)
        bind.SuggestedForYou.adapter = forYouAdapter
        bind.SuggestedForYou.layoutManager = forYouLayoutManager

        forYouAdapter.setOnItemClickListener { prompt ->
            Log.i("PROMPT", prompt.toString())
            categoryName  = prompt.category
            startActivityForResult(chosePhoto, 1)
            promptModel = listOf(prompt)
            Log.i("promptModel", promptModel.toString())
        }

        bind.cross.setOnClickListener {
            val toMain = Intent(this, changePhoto::class.java)
            startActivity(toMain)
            finish()


        }

        bind.tryStyleNow.setOnClickListener{
            val tryStyle = Intent(this, choseColor::class.java)
            tryStyle.putExtra("promptModel", allStylesJson)
            tryStyle.putExtra("selectedImageUri", selectedImageUri)

            Log.i("inputImage", selectedImageUri.toString())
            startActivity(tryStyle)
            finish()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {

             selectedImageUri = data.data!!

            if (selectedImageUri != null) {
                // FACE DETECTOR
                Log.i("selectedImageUri", selectedImageUri.toString())

                val imageSize = manage.getImageSize(this, selectedImageUri)

                if(imageSize != null){
                    width = imageSize.first
                    height = imageSize.second
                }else{
                    Log.i("ERROR SIZE IMAGE", "ERROR SIZE IMAGE")

                }

                val image: InputImage = InputImage.fromFilePath(this, selectedImageUri)

                val faceDetector: FaceDetector = FaceDetection.getClient()

                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val choseStyleIntent = Intent(this@choseStyle, choseStyle::class.java)

                            val gson = Gson()
                            val styleForChose = gson.toJson(promptModel)
                            val modelsJson = gson.toJson(modelsInCategory)
                            choseStyleIntent.putExtra("styleForChose", styleForChose)
                            Log.i("styleForChose", styleForChose.toString())
                            choseStyleIntent.putExtra("selectedImageUri", selectedImageUri)

                            Log.i("selectedImageUri", selectedImageUri.toString())
                            choseStyleIntent.putExtra("modelsInCategory", modelsJson)

                            startActivity(choseStyleIntent)

                        } else {
                            val dialog = Dialog(this)
                            dialog.setContentView(R.layout.alert_change_photo)

                            val Ok = dialog.findViewById<TextView>(R.id.Ok)
                            Ok.setOnClickListener {
                                startActivityForResult(chosePhoto, 1)
                                dialog.dismiss()
                            }

                            dialog.show()

                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }

                // FACE Detector END
            }
        }
        else if (requestCode == 2){


                if (data != null) {
            val newImageUri = data.data

            // FACE DETECTOR
            Log.i("selectedImageUri", newImageUri.toString())

            val imageSize = manage.getImageSize(this, newImageUri!!)

            if(imageSize != null){
                width = imageSize.first
                height = imageSize.second
            }else{
                Log.i("ERROR SIZE IMAGE", "ERROR SIZE IMAGE")

            }

            val image: InputImage = InputImage.fromFilePath(this, newImageUri)

            val faceDetector: FaceDetector = FaceDetection.getClient()

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        selectedImageUri = newImageUri
                        Log.i("selectedImageUri", selectedImageUri.toString())
                        Toast.makeText(this, "Photo Changed successful", Toast.LENGTH_SHORT).show()

                    }
                    else {
                        val dialog = Dialog(this)
                        dialog.setContentView(R.layout.alert_change_photo)

                        val Ok = dialog.findViewById<TextView>(R.id.Ok)
                        Ok.setOnClickListener {
                            startActivityForResult(chosePhoto, 1)
                            dialog.dismiss()
                        }

                        dialog.show()

                 }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
        }}
        }
    }
}
