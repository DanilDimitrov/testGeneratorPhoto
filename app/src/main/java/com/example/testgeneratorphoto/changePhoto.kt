package com.example.testgeneratorphoto

import Manage
import StyleAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testgeneratorphoto.databinding.ActivityChangePhotoBinding
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.uploadcare.android.library.api.UploadcareClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

class changePhoto : AppCompatActivity() { // Поменял название класса

    lateinit var bind: ActivityChangePhotoBinding
    private val REQUEST_IMAGE_PICK = 1
     var fileValue: String = ""

    val manage = Manage()
    val uiIntarface = UIIntreface()
    var width: Int = 0
    var height: Int = 0
    val chosePhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    lateinit var promptModel : List<Model>
    lateinit var modelsInCategory: List<Model>
    lateinit var categoryName: String

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                12
            )

        } else {
            // Permission already granted
            // Your existing logic here
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 12) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

                checkCameraPermission()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        bind = ActivityChangePhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        findViewById<View>(R.id.changePhoto).startAnimation(animation)

        val noCoins = intent.getBooleanExtra("noCoins", false)
        if(noCoins){
            Toast.makeText(this, "noCoins", Toast.LENGTH_SHORT).show()
        }
        // UI
        bind.textApp.paint.shader = uiIntarface.textApp(bind.textApp, "ReImage")

        // UI CLOSE

bind.aiArt.setOnClickListener {
    val i = Intent(this, MainActivity::class.java)
    startActivity(i)
    finish()
}

        lifecycleScope.launch {
            val allModels = manage.getAllCollections()
             val categories = HashSet<String>()

            for(categoria in allModels[2]){
                categories.add(categoria.category)
            }

            Log.i("allModels", allModels.toString())
            Log.i("categories", categories.toString())


            // Header adapter
            val headerAdapter = HeaderAdapter(allModels[0])
            val headerLayoutManager = LinearLayoutManager(this@changePhoto, LinearLayoutManager.HORIZONTAL, false)
            headerLayoutManager.stackFromEnd = true
            bind.recyclerView.adapter = headerAdapter
            bind.recyclerView.layoutManager = headerLayoutManager

            val centerPosition = headerAdapter.itemCount / 2
            bind.recyclerView.scrollToPosition(centerPosition)

            headerAdapter.setOnItemClickListener { prompt ->
                checkCameraPermission()
                Log.i("PROMPT", prompt.toString())
                categoryName  = prompt.category
                modelsInCategory = allModels[0].filter { it.category == categoryName }
                startActivityForResult(chosePhoto, REQUEST_IMAGE_PICK)
                promptModel = listOf(prompt)
                Log.i("promptModel", promptModel.toString())


            }
            // Header adapter END

            // Popular adapter
            val popularAdapter = PopularAdapter(allModels[1])
            val popularLayoutManager = LinearLayoutManager(this@changePhoto, LinearLayoutManager.HORIZONTAL, false)
            bind.popular.adapter = popularAdapter
            bind.popular.layoutManager = popularLayoutManager

            popularAdapter.setOnItemClickListener { prompt ->
                checkCameraPermission()
                Log.i("PROMPT", prompt.toString())
                categoryName  = prompt.category
                modelsInCategory = allModels[1].filter { it.category == categoryName }
                startActivityForResult(chosePhoto, REQUEST_IMAGE_PICK)
                promptModel = listOf(prompt)
                Log.i("promptModel", promptModel.toString())
            }
            // Popular adapter END

            //all styles
            for (category in categories) {
                // Создаем заголовок категории
                val itemView = LayoutInflater.from(this@changePhoto).inflate(R.layout.text_adapter, null)
                val categoryHeader = itemView.findViewById<TextView>(R.id.categoryTextView)
                val seeAllButton = itemView.findViewById<ImageButton>(R.id.imageButton)
                val categoryRecyclerView = itemView.findViewById<RecyclerView>(R.id.categoryRecyclerView)

                // Настройка текста и изображения
                categoryHeader.text = category
                categoryRecyclerView.layoutManager = LinearLayoutManager(this@changePhoto, LinearLayoutManager.HORIZONTAL, false)

                bind.linear.addView(itemView)

                // Фильтруем стили по категории
                val stylesForCategory = allModels[2].filter { it.category == category }

                // Создаем адаптер и связываем его с RecyclerView
                val styleAdapter = StyleAdapter(stylesForCategory, category)
                categoryRecyclerView.adapter = styleAdapter

                seeAllButton.setOnClickListener {
                    checkCameraPermission()
                    val allStylesInCategory = allModels[2].filter { it.category == category }

                    val gson = Gson()
                    val allStylesJson = gson.toJson(allStylesInCategory)

                    val seeAllIntent = Intent(this@changePhoto, seeAllStyles::class.java)

                    seeAllIntent.putExtra("allStylesInCategory", allStylesJson)
                    Log.i("allStylesJson", allStylesJson.toString())
                    startActivity(seeAllIntent)
                }

                styleAdapter.setOnItemClickListener { prompt ->
                    checkCameraPermission()
                    Log.i("PROMPT", prompt.toString())
                    categoryName  = prompt.category
                    modelsInCategory = allModels[2].filter { it.category == categoryName }

                    startActivityForResult(chosePhoto, REQUEST_IMAGE_PICK)
                    promptModel = listOf(prompt)
                    Log.i("promptModel", promptModel.toString())

                }
            }

            val text = TextView(this@changePhoto)
            val allStylesRecyclerView = RecyclerView(this@changePhoto)

            val marginInDp = 20 // Задайте нужное значение отступа в DP
            val marginInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginInDp.toFloat(),
                resources.displayMetrics
            )

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(marginInPx.toInt(), 10, marginInPx.toInt(), 0)
            allStylesRecyclerView.layoutParams = layoutParams

            text.textSize = 17.0F
            text.text = "All Styles"
            text.setTextColor(resources.getColor(R.color.white))
            text.layoutParams = layoutParams

            bind.linear.addView(text)

            val AllStylesAdapter = AllStyleBottomAdapter(allModels[2])
            val AllStylesLayoutManager = GridLayoutManager(this@changePhoto, 2)
            allStylesRecyclerView.adapter = AllStylesAdapter
            allStylesRecyclerView.layoutManager = AllStylesLayoutManager
            bind.linear.addView(allStylesRecyclerView)

            AllStylesAdapter.setOnItemClickListener { prompt ->
                checkCameraPermission()
                Log.i("PROMPT", prompt.toString())
                startActivityForResult(chosePhoto, REQUEST_IMAGE_PICK)
                categoryName = prompt.category

                promptModel = listOf(prompt)
                modelsInCategory = allModels[2]

                Log.i("promptModel", promptModel.toString())
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
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
                            val choseStyleIntent = Intent(this@changePhoto, choseStyle::class.java)

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
                            Toast.makeText(this, "Change photo", Toast.LENGTH_SHORT).show()
                            startActivityForResult(chosePhoto, REQUEST_IMAGE_PICK)

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
