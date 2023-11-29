package com.test.testgeneratorphoto

import Manage
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.test.testgeneratorphoto.databinding.ActivitySeeAllStylesBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector

class seeAllStyles : AppCompatActivity() {
    lateinit var bind: ActivitySeeAllStylesBinding
    val manage = Manage()
    var width: Int = 0
    var height: Int = 0
    lateinit var promptModel: List<Model>
    lateinit var modelsInCategory: List<Model>
    lateinit var categoryName: String
    val chosePhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySeeAllStylesBinding.inflate(layoutInflater)
        setContentView(bind.root)
        // Получите JSON-строку из Intent
        val allStylesJson = intent.getStringExtra("allStylesInCategory")

// Преобразуйте JSON-строку обратно в список стилей
        val gson = Gson()
        val type = object : TypeToken<List<Model>>() {}.type
        val allStylesInCategory = gson.fromJson<List<Model>>(allStylesJson, type)
        Log.i("allStylesInCategory", allStylesInCategory.toString())

        bind.textView2.text = allStylesInCategory[0].category

        val seeAllAdapter = AllStyleAdapter(allStylesInCategory)
        val seeAllAdapterLayoutManager = GridLayoutManager(this,2)
        bind.seeAllStyles.adapter = seeAllAdapter
        bind.seeAllStyles.layoutManager = seeAllAdapterLayoutManager

        seeAllAdapter.setOnItemClickListener {
                prompt ->
            Log.i("PROMPT", prompt.toString())
            categoryName  = prompt.category
            modelsInCategory = allStylesInCategory
            startActivityForResult(chosePhoto, 1)
            promptModel = listOf(prompt)
            Log.i("promptModel", promptModel.toString())
        }

        bind.backFromAllStyle.setOnClickListener{
            val FromAllStyle = Intent(this, changePhoto::class.java)
            startActivity(FromAllStyle)
            finish()
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data

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
                            val choseStyleIntent = Intent(this@seeAllStyles, choseStyle::class.java)

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
    }

}