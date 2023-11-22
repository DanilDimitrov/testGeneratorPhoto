package com.example.testgeneratorphoto

import Manage
import android.app.Activity
import android.content.Intent
import com.uploadcare.android.library.api.UploadcareClient
import com.uploadcare.android.library.api.UploadcareFile
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testgeneratorphoto.databinding.ActivityGeneratePhotoBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.uploadcare.android.library.callbacks.UploadFileCallback
import com.uploadcare.android.library.exceptions.UploadFailureException
import com.uploadcare.android.library.exceptions.UploadcareApiException
import com.uploadcare.android.library.upload.FileUploader
import com.uploadcare.android.library.upload.UrlUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException

class generatePhoto : AppCompatActivity() {
    lateinit var bind: ActivityGeneratePhotoBinding
    val uploadcare = UploadcareClient("8e5546827ea347b7479c", "67008faeb1a524b9d9c0")
    private var isDestroyed = false
    private lateinit var auth: FirebaseAuth
    var isUserGallery = false
    var urlFileUploader: String = ""



    val handler = Handler(Looper.getMainLooper())
    val apiKey = "Bearer api-f1c9b6f96dce11eea95ce67244d2bd83"
    var fileValue: String = ""
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
    }

    fun uploadFromUrl(url: String, callback: (String) -> Unit) {
        val uploader = UrlUploader(uploadcare, url)
            .store(true)

        try {
            uploader.uploadAsync(object : UploadFileCallback {
                override fun onFailure(e: UploadcareApiException) {
                    // Handle errors.
                }

                override fun onProgressUpdate(
                    bytesWritten: Long,
                    contentLength: Long,
                    progress: Double) {
                    // Upload progress info.
                }

                override fun onSuccess(result: UploadcareFile) {
                    // Successfully uploaded file to Uploadcare.
                    urlFileUploader = result.originalFileUrl.toString()
                    Log.i("file", result.toString())
                    callback(urlFileUploader)
                }
            })

        } catch (e: UploadFailureException) {
            // Handle errors.
        }

    }




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bind = ActivityGeneratePhotoBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val manage = Manage()
        val gson = Gson()
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        updateUI(user)


        val styleinjson = intent.getStringExtra("promptModel")
        val color = intent.getStringExtra("color")
        val savedToGallery = intent.getBooleanExtra("savedToGallery", false)


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
                    val toHome = Intent(this@generatePhoto, changePhoto::class.java)
                    Log.i("URL", imageUrl)

                    if (isUserGallery) {
                        if(savedToGallery == true) {
                            uploadFromUrl(imageUrl) { url ->


                                val uid = user?.uid
                                val db = FirebaseFirestore.getInstance()
                                val documentReference = db.collection("Users").document(uid!!)

                                documentReference.update(
                                    "imagesUrls",
                                    FieldValue.arrayUnion(url)
                                )
                                    .addOnSuccessListener {
                                        Log.i("imagesUrls", url)
                                    }
                                    .addOnFailureListener {

                                    }

                            }
                        }
                    }
                    val intentPhoto_Activity = Intent(this@generatePhoto, Photo_Activity::class.java)
                    intentPhoto_Activity.putExtra("imageUrl", imageUrl)
                    intentPhoto_Activity.putExtra("styleName", prompt.styleName)
                    intentPhoto_Activity.putExtra("isUserGallery", isUserGallery)

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
            Fuel.get("https://creator.aitubo.ai/api/job/get?id=$id")
                .timeout(20000)
                .response { _, _, result ->
                    if (!isDestroyed) {

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
        }

        fun sendRequest() {
            Fuel.post(apiUrl)


                .header("Content-Type" to "application/json")
                .header("Authorization" to apiKey)
                .timeout(30000)
                .jsonBody(json)
                .response { _, response, result ->
                    if (!isDestroyed) {
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
                }}
        }
        Log.i("LOG", "Sending request...")

        sendRequest()
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.i("USER INFORMATION", user?.isAnonymous.toString())
        if (user?.isAnonymous == true) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("Users").document(uid)

            userDoc.get()
                .addOnSuccessListener { documentSnapshot ->
                    Log.i("USER INFORMATION", "Document already exists")

                    val isUserAccessGallery = documentSnapshot.getBoolean("isUserAccessGallery") ?: false
                    Log.i("USER INFORMATION", "isUserPaid: $isUserAccessGallery")

                    if (isUserAccessGallery) {
                        Log.i("USER INFORMATION", "The user is paid.")
                        isUserGallery = true
                    } else {
                        Log.i("USER INFORMATION", "The user is not paid.")
                        isUserGallery = false
                    }

                }
                .addOnFailureListener {
                    Log.e("USER INFORMATION", "Failed to get document")
                }
        }
    }

}