package com.example.testgeneratorphoto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.example.testgeneratorphoto.databinding.ActivityChangePhotoBinding
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso
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
    val apiKey = "Bearer api-7aa8f78965f911ee9e1fbef530bcf532"
    val handler = Handler(Looper.getMainLooper())

    val uploadcare = UploadcareClient("8e5546827ea347b7479c", "9a60e9e2427b72234e5d")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityChangePhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.photo.setOnClickListener {
            val goToPhoto = Intent(this, MainActivity::class.java)
            startActivity(goToPhoto)
        }

        bind.select.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        bind.generate.setOnClickListener {
            createImageWithRetry("""
                {
                    "prompt": "[elf | people] in the forest, hdr, realism",
                    "imagePath": "https://ucarecdn.com/${fileValue.toString()}/-/preview/500x500/-/quality/smart/-/format/auto/",
                    "modelId": "65153a1d6173e0279c96d006",
                    "negativePrompt": "[deformed | disfigured], poorly drawn, [bad : wrong] anatomy, [extra | missing | floating | disconnected] limb, (mutated hands and fingers), blurry",
                    "guidanceScale": 18, 
                    "strength" : 0.5, 
                    "count" : 1, 
                    "styleId": "64e036bafd1c84b45905c9cf",
                    "height": 512,
                    "steps": 22
                }
            """.trimIndent(), "https://creator.aitubo.ai/api/job/create") { imageUrl ->
                runOnUiThread {
                    Log.i("URL", imageUrl)
                    Picasso.get().load(imageUrl).into(bind.imageView2)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data

            if (selectedImageUri != null) {
                runOnUiThread { bind.imageView2.setImageURI(selectedImageUri) }
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(selectedImageUri, projection, null, null, null)

                cursor?.use {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    it.moveToFirst()
                    val filePath = it.getString(columnIndex)
                    val file = File(filePath)

                    val apiKey = "8e5546827ea347b7479c"
                    val uploadUrl = "https://upload.uploadcare.com/base/"
                    val client = OkHttpClient()
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("UPLOADCARE_PUB_KEY", apiKey)
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
                            Log.e("NetworkError", e.message ?: "Unknown error")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()
                                Log.i("LOG", responseBody.toString())
                                val jsonObject = JSONObject(responseBody)

                                fileValue = jsonObject.getString("file")
                                Log.i("LOG", fileValue.toString())

                            } else {
                                // Handle the response error here
                            }
                        }
                    })
                }
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
}
