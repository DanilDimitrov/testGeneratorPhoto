package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.testgeneratorphoto.databinding.ActivityGenerateArtProcessBinding
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import org.json.JSONObject

class generateArtProcess : AppCompatActivity() {
    lateinit var bind: ActivityGenerateArtProcessBinding
    private var isDestroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityGenerateArtProcessBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val jsonForRequest = intent.getStringExtra("jsonForRequest").toString().trimIndent()
        val styleName = intent.getStringExtra("styleName")
        Log.i("styleName", styleName.toString())

        val videoPath = "android.resource://" + packageName + "/" + R.raw.loading

        bind.videoView.setVideoPath(videoPath)

        bind.videoView.setOnCompletionListener {
            bind.videoView.start()
        }
        bind.videoView.start()
        fun createImageWithRetry(json: String, apiUrl: String, callback: (String) -> Unit) {

            val apiKey = "a3dec967-0738-4529-a904-3693bf1c208f"
            val handler = Handler(Looper.getMainLooper())


            fun checkStatus(id: Int) {
                Fuel.post("https://api.midjourneyapi.xyz/sd/fetch")
                    .header("Content-Type" to "application/json")
                    .timeout(20000)
                    .jsonBody("{\"id\": $id}")
                    .response { _, _, result ->
                        if (!isDestroyed) {

                            when (result) {
                            is Result.Failure -> {
                                val e = result.error.exception
                                Log.e("error", "API FAILED", e)

                                // Повторяем запрос с задержкой 3 секунд
                                handler.postDelayed({ checkStatus(id) }, 3000)
                            }
                            is Result.Success -> {
                                val body = String(result.value)
                                val jsonObject = JSONObject(body)
                                val status = jsonObject.optString("status")

                                when (status) {
                                    "success" -> {
                                        val output = jsonObject.optJSONArray("output")
                                        if (output?.isNull(0) == false) {
                                            val imageUrl = output.getString(0)
                                            callback(imageUrl)
                                        }
                                    }
                                    "processing" -> {
                                        // Если статус "processing", продолжаем проверку
                                        handler.postDelayed({ checkStatus(id) }, 3000)
                                        Log.i("Process", status.toString())
                                    }
                                    else -> {
                                        // Обработка других статусов, если необходимо
                                    }
                                }
                            }
                        }}
                    }
            }
            fun sendRequest() {
                Fuel.post(apiUrl)
                    .header("Content-Type" to "application/json")
                    .header("X-API-KEY" to apiKey)
                    .timeout(30000)
                    .jsonBody(json)
                    .response { _, response, result ->
                        if (!isDestroyed) {

                        when (result) {
                            is Result.Failure -> {
                                val e = result.error.exception
                                Log.e("error", "API FAILED", e)

                                // Повторяем запрос с задержкой 3 секунд
                                handler.postDelayed({ sendRequest() }, 3000)
                            }
                            is Result.Success -> {
                                val body = String(response.data)

                                val jsonObject = JSONObject(body)
                                val status = jsonObject.optString("status")

                                when (status) {
                                    "success" -> {
                                        val output = jsonObject.optJSONArray("output")
                                        if (output?.isNull(0) == false) {
                                            val imageUrl = output.getString(0)
                                            callback(imageUrl)
                                        } else {
                                        }
                                    }
                                    "processing" -> {
                                        // Если статус "processing", получаем id и продолжаем проверку
                                        val id = jsonObject.optInt("id")
                                        checkStatus(id)
                                        Log.i("Process", status.toString())
                                    }
                                    else -> {
                                    }
                                }
                            }
                        }
                    }
            }}



            sendRequest()
        }


        createImageWithRetry(
            jsonForRequest, "https://api.midjourneyapi.xyz/sd/txt2img") { imageUrl ->
            val toImageIntent = Intent(this, Photo_Activity::class.java)
            toImageIntent.putExtra("imageUrl", imageUrl)
            toImageIntent.putExtra("styleName", styleName)
            Log.i("styleNameIntent", styleName.toString())
            startActivity(toImageIntent)
            finish()

    }

}
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
    }
}