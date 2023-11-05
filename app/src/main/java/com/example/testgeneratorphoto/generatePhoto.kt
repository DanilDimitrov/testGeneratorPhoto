package com.example.testgeneratorphoto

import Manage
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.example.testgeneratorphoto.databinding.ActivityGeneratePhotoBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uploadcare.android.library.api.UploadcareClient
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
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class generatePhoto : AppCompatActivity() {
    lateinit var bind: ActivityGeneratePhotoBinding
    val uploadcare = UploadcareClient("8e5546827ea347b7479c", "67008faeb1a524b9d9c0")
    private var shouldStopProcessing = false


    val handler = Handler(Looper.getMainLooper())
    val apiKey = "Bearer api-f1c9b6f96dce11eea95ce67244d2bd83"
    var fileValue: String = ""
    override fun onBackPressed() {
        shouldStopProcessing = true // Установите флаг, чтобы прервать выполнение запросов и операций
        super.onBackPressed() // Вызовите родительский метод для обработки нажатия кнопки "Назад"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bind = ActivityGeneratePhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val manage = Manage()
        val gson = Gson()

        val styleinjson = intent.getStringExtra("promptModel")
        val color = intent.getStringExtra("color")

        val selectedImageUri = intent.getParcelableExtra<Uri>("selectedImageUri")
        val type = object : TypeToken<List<Model>>() {}.type
        Log.i("styleinjson", styleinjson.toString())
        Log.i("selectedImageUri", selectedImageUri.toString())
        Log.i("color", color.toString())

        val promptModel = gson.fromJson<List<Model>>(styleinjson, type)
        Log.i("promptModel", promptModel.toString())

        val imageSize = manage.getImageSize(this, selectedImageUri!!)
        val width = imageSize?.first
        val height = imageSize?.second

        fun loadToUpload(callback: (String) -> Unit){
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImageUri, projection, null, null, null)

            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                it.moveToFirst()
                val filePath = it.getString(columnIndex)
                val file = File(filePath)

                val uploadUrl = "https://upload.uploadcare.com/base/"
                val client = OkHttpClient()
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("UPLOADCARE_PUB_KEY", "8e5546827ea347b7479c")
                    .addFormDataPart("UPLOADCARE_STORE", "auto")
                    .addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // Handle the failure here
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            val jsonObject = JSONObject(responseBody!!)
                            fileValue = jsonObject.getString("file") // Set fileValue here
                            callback(fileValue)

                        } else {
                            Log.i("ERROR", response.body.toString())

                        }

                    }
                })

            }
        }

        fun makeReq(prompt: Model) {

            loadToUpload()
            {fileValue ->

                val reque = """
                    {
                        "model_id": "${prompt.modelId}",
                        "controlModel": "${prompt.controlModel}",
                        "imagePath": "https://ucarecdn.com/${fileValue}/-/preview/768x${manage.resizeImage(width!!, height!!, 768)}/-/quality/smart/-/format/auto/",
                        "prompt": "$color ${prompt.prompt}",
                        "negative_prompt": "${prompt.negativePrompt}",
                        "width": 768,
                        "strength" : ${prompt.strength},
                        "height": ${manage.resizeImage(width!!, height!!, 768)},
                        "seed": null,
                        "lora_model": ${prompt.lora},
                        "lora_strength": 1,
                        "steps": ${prompt.steps}
                    }
                    """.trimIndent()

                Log.i("reque", reque)
                createImageWithRetry(
                    reque, "https://creator.aitubo.ai/api/job/create") { imageUrl ->
                    val toHome = Intent(this@generatePhoto, changePhoto::class.java)

                    if (shouldStopProcessing){
                        startActivity(toHome)
                        finish()
                    }

                    Log.i("URL", imageUrl)
                    val intentPhoto_Activity = Intent(this@generatePhoto, Photo_Activity::class.java)
                    intentPhoto_Activity.putExtra("imageUrl", imageUrl)
                    if(imageUrl == "break"){
                        startActivity(toHome)
                        finish()
                    }
                    else {
                        startActivity(intentPhoto_Activity)
                        finish()
                    }
                }

            }}

        makeReq(promptModel[0])

    }
    fun createImageWithRetry(json: String, apiUrl: String, callback: (String) -> Unit) {
        fun checkStatus(id: String) {
            if (shouldStopProcessing){
                callback("break")
            }
            Fuel.get("https://creator.aitubo.ai/api/job/get?id=$id")
                .timeout(20000)
                .response { _, _, result ->
                    if (shouldStopProcessing){
                        startActivity(Intent(this@generatePhoto, changePhoto::class.java))
                        finish()
                    }
                    when (result) {
                        is Result.Failure -> {
                            val e = result.error.exception
                            Log.e("error", "API FAILED", e)
                            handler.postDelayed({ checkStatus(id) }, 3000)
                            if (shouldStopProcessing){
                                startActivity(Intent(this@generatePhoto, changePhoto::class.java))
                                finish()
                            }
                        }
                        is Result.Success -> {
                            if (shouldStopProcessing){
                                startActivity(Intent(this@generatePhoto, changePhoto::class.java))
                                finish()
                            }

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
                                    if (shouldStopProcessing){
                                        callback("break")
                                    }

                                    if (imagesArray != null && imagesArray.length() > 0) {
                                        val firstImageUrl = imagesArray.getString(0)
                                        Log.i("First IMAGE URL", firstImageUrl)
                                        GlobalScope.launch(Dispatchers.IO) {
                                            uploadcare.deleteFile(fileValue.toString())

                                        }
                                        callback("https://file.aitubo.ai/${firstImageUrl.toString()}")



                                    } else {
                                        handler.postDelayed({ checkStatus(id) }, 2000)
                                        if (shouldStopProcessing){
                                            callback("break")
                                        }
                                    }
                                }

                                1 -> {
                                    handler.postDelayed({ checkStatus(id) }, 3000)
                                    Log.i("Process", status.toString())
                                    if (shouldStopProcessing){
                                        callback("break")
                                    }
                                }

                                else -> {
                                    handler.postDelayed({ checkStatus(id) }, 3000)
                                    if (shouldStopProcessing){
                                        callback("break")
                                    }
                                }
                            }
                        }
                    }
                }
        }

        fun sendRequest() {
            if (shouldStopProcessing){
                callback("break")
            }
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
                            if (shouldStopProcessing){
                                callback("break")
                            }
                        }

                        is Result.Success -> {
                            val body = String(response.data)

                            val jsonObject = JSONObject(body)
                            val code = jsonObject.optString("code")
                            Log.i("id", jsonObject.toString())
                            when (code) {
                                "0" -> {
                                    if (shouldStopProcessing){
                                        callback("break")
                                    }
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
                                    if (shouldStopProcessing){
                                        callback("break")
                                    }
                                }
                            }
                        }
                    }
                }
        }
        Log.i("LOG", "Sending request...")

        sendRequest()
    }


}