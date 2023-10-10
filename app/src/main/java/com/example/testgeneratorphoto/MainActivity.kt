package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.testgeneratorphoto.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result

class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.generate.setOnClickListener {
            createImageWithRetry("""
    {
        "model_id": "bubeilijiedemoxing",
        "prompt": "${bind.prompt.text.toString()}",
        "negative_prompt": "bad quality,low details, anime style",
        "width": "512",
        "height": "512",
        "seed": null,
        "lora_model": "detailtweakerlora",
        "lora_strength": null
    }
    """.trimIndent(), "https://api.midjourneyapi.xyz/sd/txt2img") { imageUrl ->
                runOnUiThread {
                    Log.i("URL", imageUrl)
                    Picasso.get().load(imageUrl).into(bind.imageView)
                }
            }
        }
        bind.selfi?.setOnClickListener {
            val intent = Intent(this, changePhoto::class.java)
            startActivity(intent)

        }
    }

     fun createImageWithRetry(json: String, apiUrl: String, callback: (String) -> Unit) {

         val apiKey = "a3dec967-0738-4529-a904-3693bf1c208f"
         val handler = Handler(Looper.getMainLooper())


        fun checkStatus(id: Int) {
            Fuel.post("https://api.midjourneyapi.xyz/sd/fetch")
                .header("Content-Type" to "application/json")
                .timeout(20000)
                .jsonBody("{\"id\": $id}")
                .response { _, _, result ->
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
                    }
                }
        }
        fun sendRequest() {
            Fuel.post(apiUrl)
                .header("Content-Type" to "application/json")
                .header("X-API-KEY" to apiKey)
                .timeout(30000)
                .jsonBody(json)
                .response { _, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val e = result.error.exception
                            Log.e("error", "API FAILED", e)

                            // Повторяем запрос с задержкой 3 секунд
                            handler.postDelayed({ sendRequest() }, 3000)
                        }
                        is Result.Success -> {
                            val body = String(response.data)
                            runOnUiThread { bind.textView.text = body}

                            val jsonObject = JSONObject(body)
                            val status = jsonObject.optString("status")

                            when (status) {
                                "success" -> {
                                    val output = jsonObject.optJSONArray("output")
                                    if (output?.isNull(0) == false) {
                                        val imageUrl = output.getString(0)
                                        callback(imageUrl)
                                    } else {
                                        runOnUiThread { bind.textView.text = "Произошла ошибка" }
                                    }
                                }
                                "processing" -> {
                                    // Если статус "processing", получаем id и продолжаем проверку
                                    val id = jsonObject.optInt("id")
                                    checkStatus(id)
                                    Log.i("Process", status.toString())
                                }
                                else -> {
                                    runOnUiThread { bind.textView.text = "Произошла ошибка" }
                                }
                            }
                        }
                    }
                }
        }



        sendRequest()
    }

}
