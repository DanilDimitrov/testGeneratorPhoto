package com.example.testgeneratorphoto

import Manage
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testgeneratorphoto.databinding.ActivityMainBinding
import org.json.JSONObject
import com.google.firebase.auth.FirebaseAuth

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding
    val manage = Manage()
    private lateinit var auth: FirebaseAuth



    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        // AUTH
        auth = Firebase.auth



        lifecycleScope.launch {
            val allModels = manage.getAllCollections()
            Log.i("allModels", allModels.toString())

            val adapter = HeaderAdapter(allModels[0])
            bind.recyclerView?.layoutManager = GridLayoutManager(this@MainActivity, 2)
            bind.recyclerView?.adapter = adapter

            adapter.setOnItemClickListener { prompt ->
                // Здесь можно выполнить действия с промптом
                Log.i("PROMPT", prompt.toString())


        bind.generate.setOnClickListener {
            val inputText = bind.prompt.text.toString()
            lifecycleScope.launch {
                val translatedText = manage.translator(inputText, "uk")
                    Log.i("TEXT", translatedText)
                val reque =    """
    {
        "model_id": "${prompt.modelId}",
        "prompt": "$translatedText  ${prompt.prompt}",
        "negative_prompt": "${prompt.negativePrompt}",
        "width": "512",
        "height": "768", 
        "seed": null,
	  "lora_model": ${prompt.lora},
	  "lora_strength": 1
    }
    """.trimIndent()
                Log.i("reque", reque)

                    createImageWithRetry(
                        reque, "https://api.midjourneyapi.xyz/sd/txt2img"
                    ) { imageUrl ->
                        runOnUiThread {
                            Log.i("URL", imageUrl)
                            intent = Intent(this@MainActivity, Photo_Activity::class.java)
                            intent.putExtra("imageUrl", imageUrl)
                            startActivity(intent)
                            //Picasso.get().load(imageUrl).into(bind.imageView)
                        }
                    }
                }
            }


            }
        }
        bind.selfi?.setOnClickListener {
            val intent = Intent(this@MainActivity, changePhoto::class.java)
            startActivity(intent)
            finish()

        }
    }
    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        signInAnonymously()
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
    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
        // [END signin_anonymously]
    }
    private fun updateUI(user: FirebaseUser?) {
        Log.i("USER INFORMATION", user?.isAnonymous.toString())
    }
}
