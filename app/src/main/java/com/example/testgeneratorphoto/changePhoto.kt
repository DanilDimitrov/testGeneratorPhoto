package com.example.testgeneratorphoto

import Manage
import StyleAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
    val apiKey = "Bearer api-f1c9b6f96dce11eea95ce67244d2bd83"
    val handler = Handler(Looper.getMainLooper())
    val uploadcare = UploadcareClient("8e5546827ea347b7479c", "9a60e9e2427b72234e5d")
    val manage = Manage()
    val uiIntarface = UIIntreface()
    var width: Int = 0
    var height: Int = 0
    val chosePhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    lateinit var promptModel : List<Model>
    lateinit var modelsInCategory: List<Model>
    lateinit var categoryName: String



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        bind = ActivityChangePhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        findViewById<View>(R.id.changePhoto).startAnimation(animation)
        // UI
        bind.textApp.paint.shader = uiIntarface.textApp(bind.textApp)

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
                    val allStylesInCategory = allModels[2].filter { it.category == category }

                    val gson = Gson()
                    val allStylesJson = gson.toJson(allStylesInCategory)

                    val seeAllIntent = Intent(this@changePhoto, seeAllStyles::class.java)

                    seeAllIntent.putExtra("allStylesInCategory", allStylesJson)
                    Log.i("allStylesJson", allStylesJson.toString())
                    startActivity(seeAllIntent)
                }

                styleAdapter.setOnItemClickListener { prompt ->
                    Log.i("PROMPT", prompt.toString())
                    categoryName  = prompt.category
                    modelsInCategory = allModels[2].filter { it.category == categoryName }

                    startActivityForResult(chosePhoto, REQUEST_IMAGE_PICK)
                    promptModel = listOf(prompt)
                    Log.i("promptModel", promptModel.toString())

                }
            }
            //all styles END
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data

            if (selectedImageUri != null) {
                // FACE DETECTOR
                Log.i("selectedImageUri", selectedImageUri.toString())

                val imageSize = manage.getImageSize(this, selectedImageUri.toString())

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
                            choseStyleIntent.putExtra("selectedImageUri", selectedImageUri.toString())
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

    fun createImageWithRetry(json: String, apiUrl: String, callback: (String) -> Unit) {
        fun checkStatus(id: String) {
            Fuel.get("https://creator.aitubo.ai/api/job/get?id=$id")
                .timeout(20000)
                .response { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            val e = result.error.exception
                            Log.e("error", "API FAILED", e)
                            handler.postDelayed({ checkStatus(id) }, 3000)
                        }

                        is Result.Success -> {
                            val body = String(result.value)
                            val jsonObject = JSONObject(body)
                            val status = jsonObject.optInt("status")
                            Log.i("STATUS", status.toString())

                            Log.i("RESULT", jsonObject.toString())
                            when (status) {
                                0 -> {
                                    val data = jsonObject.optJSONObject("data")
                                    val resultObject = data?.optJSONObject("result")
                                    val dataObject = resultObject?.optJSONObject("data")
                                    val imagesArray = dataObject?.optJSONArray("images")
                                    Log.i("imgs", imagesArray.toString())
                                    Log.i("dataObject", dataObject.toString())

                                    if (imagesArray != null && imagesArray.length() > 0) {
                                        val firstImageUrl = imagesArray.getString(0)
                                        Log.i("First IMAGE URL", firstImageUrl)
                                        GlobalScope.launch(Dispatchers.IO) {
                                            uploadcare.deleteFile(fileValue.toString())

                                        }
                                        callback("https://file.aitubo.ai/${firstImageUrl.toString()}")



                                    } else {
                                        handler.postDelayed({ checkStatus(id) }, 2000)
                                    }
                                }

                                1 -> {
                                    handler.postDelayed({ checkStatus(id) }, 3000)
                                    Log.i("Process", status.toString())
                                }

                                else -> {
                                    handler.postDelayed({ checkStatus(id) }, 3000)
                                }
                            }
                        }
                    }
                }
        }

        fun sendRequest() {
            Fuel.post(apiUrl)
                .header("Content-Type" to "application/json")
                .header("Authorization" to apiKey)
                .timeout(30000)
                .jsonBody(json)
                .response { _, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val e = result.error.exception
                            Log.e("error", "API FAILED", e)
                            handler.postDelayed({ sendRequest() }, 3000)
                        }

                        is Result.Success -> {
                            val body = String(response.data)

                            val jsonObject = JSONObject(body)
                            val code = jsonObject.optString("code")
                            Log.i("id", jsonObject.toString())
                            when (code) {
                                "0" -> {
                                    val dataObject = jsonObject.optJSONObject("data")
                                    val id = dataObject?.optString("id")
                                    Log.i("data", dataObject!!.toString())
                                    Log.i("id", id.toString())
                                    if (id != null) {
                                        Log.i("LOG", "Checking status for ID: $id")
                                        checkStatus(id)
                                    }

                                }
                                else -> {
                                    Log.e("ERROR", "Error $code")
                                }
                            }
                        }
                    }
                }
        }
        Log.i("LOG", "Sending request...")

        sendRequest()
    }



fun makeReq(prompt: Model){
    val reque =    """
    {
        "model_id": "${prompt.modelId}",
        "controlModel": "${prompt.controlModel}",
        "imagePath": "https://ucarecdn.com/${fileValue}/-/preview/768x${manage.resizeImage(width, height, 768)}/-/quality/smart/-/format/auto/",
        "prompt": "${prompt.prompt}",
        "negative_prompt": "${prompt.negativePrompt}",
        "width": 768,
        "strength" : ${prompt.strength},
        "height": ${manage.resizeImage(width, height, 768)}, 
        "seed": null,
	  "lora_model": ${prompt.lora},
	  "lora_strength": 1, 
      "steps": ${prompt.steps}
    }
    """.trimIndent()
    Log.i("reque", reque)
    createImageWithRetry(
        reque, "https://creator.aitubo.ai/api/job/create") { imageUrl ->
        runOnUiThread {
            Log.i("URL", imageUrl)
            intent = Intent(this@changePhoto, Photo_Activity::class.java)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
        }
    }
}



}
