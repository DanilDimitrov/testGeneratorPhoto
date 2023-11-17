package com.example.testgeneratorphoto

import Manage
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testgeneratorphoto.databinding.ActivityMainBinding
import org.json.JSONObject
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.random.Random
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding
    val manage = Manage()
    private lateinit var auth: FirebaseAuth
    val uiIntarface = UIIntreface()
    var sizeForGeneration = ""
    var promptForArt: artModel? = null
    var width :Int? = null
    var height :Int? = null
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 12
    var rewardedAd: RewardedAd? = null

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
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

        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

                checkCameraPermission()
            }
        }
    }
 fun loadAd(){
     val adRequest = AdRequest.Builder().build()
     RewardedAd.load(this, "ca-app-pub-9370272402380511/5176048905", adRequest, object : RewardedAdLoadCallback(){
         override fun onAdFailedToLoad(p0: LoadAdError) {
             super.onAdFailedToLoad(p0)
         }

         override fun onAdLoaded(p0: RewardedAd) {
             super.onAdLoaded(p0)
             rewardedAd = p0
             rewardedAd?.fullScreenContentCallback = adListener()
         }
     })
 }

    fun adListener() = object : FullScreenContentCallback(){
        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            rewardedAd = null
            loadAd()
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
            rewardedAd = null
            loadAd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        // AUTH
        val gson = Gson()
        loadAd()
        auth = Firebase.auth
        MobileAds.initialize(this){}
        val prompt = intent.getStringExtra("promptFromSeeAll")
        if (prompt != null){
            val type = object : TypeToken<artModel>() {}.type
            promptForArt = gson.fromJson(prompt, type)
            Log.i("promptForArt", promptForArt.toString())
        }


        bind.size1.setOnClickListener {
            bind.size1.isChecked = true
            bind.size2.isChecked = false
            bind.size3.isChecked = false
            bind.size4.isChecked = false
            bind.size5.isChecked = false
            selectSize() }
        bind.size2.setOnClickListener {
            bind.size2.isChecked = true
            bind.size1.isChecked = false
            bind.size3.isChecked = false
            bind.size4.isChecked = false
            bind.size5.isChecked = false
            selectSize() }
        bind.size3.setOnClickListener {
            bind.size3.isChecked = true
            bind.size2.isChecked = false
            bind.size1.isChecked = false
            bind.size4.isChecked = false
            bind.size5.isChecked = false
            selectSize() }
        bind.size4.setOnClickListener {
            bind.size4.isChecked = true
            bind.size2.isChecked = false
            bind.size3.isChecked = false
            bind.size1.isChecked = false
            bind.size5.isChecked = false
            selectSize() }
        bind.size5.setOnClickListener {
            bind.size5.isChecked = true
            bind.size2.isChecked = false
            bind.size3.isChecked = false
            bind.size4.isChecked = false
            bind.size1.isChecked = false
            selectSize() }

        // UI
        bind.textApp3.paint?.shader = uiIntarface.textApp(bind.textApp3, "ReImage")

        bind.button4.setOnClickListener {
            val goTopPro = Intent(this, pro_screen::class.java)
            startActivity(goTopPro)
        }

        lifecycleScope.launch {
            val artModelsArray = manage.getArtModels()
            val allPrompts = manage.getArtPrompt()

            val artModels = artModelsArray[0]
            Log.i("artModels", artModels.toString())

            val artAdapter = artStyleAdapter(artModels)
            val artStyleLayoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            bind.artStyleRecycler.adapter = artAdapter
            bind.artStyleRecycler.layoutManager = artStyleLayoutManager

            artAdapter.setOnItemClickListener { prompt ->
                Log.i("PROMPT", prompt.toString())
                promptForArt = prompt

            }


            bind.inspiration.setOnClickListener {
                val randomNumber= (0 until allPrompts.size).random()
                Log.i("randomNumber", randomNumber.toString())
                runOnUiThread { bind.editTextText.setText(allPrompts[randomNumber])}


            }

            bind.seeAllArtStyles.setOnClickListener{
                val seeAllArtIntent = Intent(this@MainActivity, seeAllForArt::class.java)
                val allStylesJson = gson.toJson(artModels)
                seeAllArtIntent.putExtra("allStylesInCategory", allStylesJson)
                Log.i("allStylesJson", allStylesJson.toString())
                startActivity(seeAllArtIntent)
            }
        }



        // UI CLOSE
        bind.selfi.setOnClickListener {
            val intent = Intent(this@MainActivity, changePhoto::class.java)
            startActivity(intent)
            finish()

        }
        bind.generate.setOnClickListener {
            checkCameraPermission()

            if(bind.editTextText.text.isEmpty()){
                Toast.makeText(this, "Your prompt is empty", Toast.LENGTH_SHORT).show()
            }
            else if (sizeForGeneration == ""){ Toast.makeText(this, "Please chose size for art", Toast.LENGTH_SHORT).show() }
            else if(promptForArt == null){ Toast.makeText(this, "Please chose style for art", Toast.LENGTH_SHORT).show()

                if(sizeForGeneration=="1:1"){
                    width = 512
                    height = 512
                }else if(sizeForGeneration=="3:4"){
                    width = 768
                    height = 1024
                }
                else if(sizeForGeneration=="4:3"){
                    width = 1024
                    height = 768
                }
                else if(sizeForGeneration=="2:3"){
                    width = 512
                    height = 768
                }else if(sizeForGeneration=="3:2"){
                    width = 768
                    height = 512
                }
            }
        }


        bind.generateSelect.setOnClickListener {
            if (bind.editTextText.text.isEmpty()) {
                Toast.makeText(this, "Your prompt is empty", Toast.LENGTH_SHORT).show()
            } else if (sizeForGeneration == "") {
                Toast.makeText(this, "Please chose size for art", Toast.LENGTH_SHORT).show()
            } else if (promptForArt == null) {
                Toast.makeText(this, "Please chose style for art", Toast.LENGTH_SHORT).show()
            } else {
                // Проверка количества доступных монеток
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userDoc = db.collection("Users").document(currentUser.uid)
                    userDoc.get()
                        .addOnSuccessListener { documentSnapshot ->
                            val numberOfTxt2Img = documentSnapshot.getLong("numberOfTxt2Img")
                            if (numberOfTxt2Img != null && numberOfTxt2Img > 0) {
                                // Перевод текста
                                lifecycleScope.launch {
                                    val translatedText =
                                        manage.translator(bind.editTextText.text.toString(), "uk")
                                    Log.i("translatedText", translatedText)

                                    // Создание JSON-объекта
                                    val jsonForRequest = """
                                {
                                    "model_id": "${promptForArt?.model_id}",
                                    "prompt": "$translatedText ${promptForArt?.prompt}",
                                    "negative_prompt": "${promptForArt?.negative_prompt}",
                                    "width": "$width",
                                    "height": "$height",
                                    "seed": null,
                                    "steps": ${promptForArt?.steps},
                                    "guidance_scale": ${promptForArt?.guidance_scale},
                                    "lora_model": "${promptForArt?.lora_model}",
                                    "lora_strength": ${promptForArt?.lora_strength}
                                }
                            """.trimIndent()
                                    Log.i("jsonForRequest", jsonForRequest)

                                    if (bind.textView16.text == "∞") {
                                        val generateArtIntent = Intent(
                                            this@MainActivity,
                                            generateArtProcess::class.java
                                        )
                                        generateArtIntent.putExtra(
                                            "jsonForRequest",
                                            jsonForRequest
                                        )
                                        generateArtIntent.putExtra(
                                            "styleName",
                                            promptForArt?.styleName.toString()
                                        )
                                        startActivity(generateArtIntent)
                                    } else {
                                        // Уменьшение количества монеток в Firestore
                                        userDoc.update("numberOfTxt2Img", numberOfTxt2Img - 1)
                                            .addOnSuccessListener {
                                                val generateArtIntent = Intent( this@MainActivity, generateArtProcess::class.java)
                                                generateArtIntent.putExtra("jsonForRequest", jsonForRequest)
                                                generateArtIntent.putExtra("styleName", promptForArt?.styleName.toString())
                                                startActivity(generateArtIntent)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Failed to update coins.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            } else {

                                    val alertDialogBuilder = AlertDialog.Builder(this)
                                    alertDialogBuilder.setTitle("Нет монеток")
                                    alertDialogBuilder.setMessage("У вас нет монеток. Получите монетки, просмотрев рекламу или подписавшись.")
                                    alertDialogBuilder.setPositiveButton("AD") { dialogInterface, _ ->
                                   Log.i("ad", "print ad")
                                        rewardedAd?.show(this){rewardItem ->
                                            Log.i("ad", "print ad")
                                       rewardItem.amount
                                            lifecycleScope.launch {
                                                val translatedText =
                                                    manage.translator(
                                                        bind.editTextText.text.toString(),
                                                        "uk"
                                                    )
                                                Log.i("translatedText", translatedText)

                                                // Создание JSON-объекта
                                                val jsonForRequest = """
                                {
                                    "model_id": "${promptForArt?.model_id}",
                                    "prompt": "$translatedText ${promptForArt?.prompt}",
                                    "negative_prompt": "${promptForArt?.negative_prompt}",
                                    "width": "$width",
                                    "height": "$height",
                                    "seed": null,
                                    "steps": ${promptForArt?.steps},
                                    "guidance_scale": ${promptForArt?.guidance_scale},
                                    "lora_model": "${promptForArt?.lora_model}",
                                    "lora_strength": ${promptForArt?.lora_strength}
                                }
                            """.trimIndent()
                                                Log.i("jsonForRequest", jsonForRequest)
                                                val generateArtIntent = Intent(
                                                    this@MainActivity,
                                                    generateArtProcess::class.java
                                                )
                                                generateArtIntent.putExtra(
                                                    "jsonForRequest",
                                                    jsonForRequest
                                                )
                                                generateArtIntent.putExtra(
                                                    "styleName",
                                                    promptForArt?.styleName.toString()
                                                )
                                                startActivity(generateArtIntent)
                                            }
                                   }
                                        dialogInterface.dismiss()
                                    }
                                    alertDialogBuilder.setNegativeButton("Pro") { dialogInterface, _ ->
                                        val toPro = Intent(this, pro_screen::class.java)
                                        startActivity(toPro)
                                        dialogInterface.dismiss()
                                    }

                                    val alertDialog: AlertDialog = alertDialogBuilder.create()
                                    alertDialog.show()

                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@MainActivity, "Failed to get user data.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }


    }

    fun selectSize(){
        bind.apply {
            runOnUiThread {
                if (size1.isChecked) {
                    size2.isChecked = false
                    size3.isChecked = false
                    size4.isChecked = false
                    size5.isChecked = false
                    width = 512
                    height = 512

                    size1.setBackgroundResource(R.drawable.radio_back_select)
                    size2.setBackgroundResource(R.drawable.radio_unselect)
                    size3.setBackgroundResource(R.drawable.radio_unselect)
                    size4.setBackgroundResource(R.drawable.radio_unselect)
                    size5.setBackgroundResource(R.drawable.radio_unselect)
                    generateSelect.visibility = View.VISIBLE
                    generate.visibility = View.INVISIBLE
                    sizeForGeneration = size1.text.toString()
                    Log.i("sizeForGeneration", sizeForGeneration)
                } else if (size2.isChecked) {
                    size1.isChecked = false
                    size3.isChecked = false
                    size4.isChecked = false
                    size5.isChecked = false
                    generateSelect.visibility = View.VISIBLE
                    generate.visibility = View.INVISIBLE
                    width = 768
                    height = 1024
                    size1.setBackgroundResource(R.drawable.radio_unselect)
                    size2.setBackgroundResource(R.drawable.radio_back_select)
                    size3.setBackgroundResource(R.drawable.radio_unselect)
                    size4.setBackgroundResource(R.drawable.radio_unselect)
                    size5.setBackgroundResource(R.drawable.radio_unselect)
                    sizeForGeneration = size2.text.toString()
                    Log.i("sizeForGeneration", sizeForGeneration)


                } else if (size3.isChecked) {
                    size2.isChecked = false
                    size1.isChecked = false
                    size4.isChecked = false
                    size5.isChecked = false
                    generateSelect.visibility = View.VISIBLE
                    generate.visibility = View.INVISIBLE
                    width = 1024
                    height = 768
                    size1.setBackgroundResource(R.drawable.radio_unselect)
                    size2.setBackgroundResource(R.drawable.radio_unselect)
                    size3.setBackgroundResource(R.drawable.radio_back_select)
                    size4.setBackgroundResource(R.drawable.radio_unselect)
                    size5.setBackgroundResource(R.drawable.radio_unselect)
                    sizeForGeneration = size3.text.toString()
                    Log.i("sizeForGeneration", sizeForGeneration)

                } else if (size4.isChecked) {
                    size2.isChecked = false
                    size3.isChecked = false
                    size1.isChecked = false
                    size5.isChecked = false
                    generateSelect.visibility = View.VISIBLE
                    generate.visibility = View.INVISIBLE
                    width = 512
                    height = 768
                    size1.setBackgroundResource(R.drawable.radio_unselect)
                    size2.setBackgroundResource(R.drawable.radio_unselect)
                    size3.setBackgroundResource(R.drawable.radio_unselect)
                    size4.setBackgroundResource(R.drawable.radio_back_select)
                    size5.setBackgroundResource(R.drawable.radio_unselect)
                    sizeForGeneration = size4.text.toString()
                    Log.i("sizeForGeneration", sizeForGeneration)

                } else if (size5.isChecked) {
                    size2.isChecked = false
                    size3.isChecked = false
                    size4.isChecked = false
                    size1.isChecked = false
                    generateSelect.visibility = View.VISIBLE
                    generate.visibility = View.INVISIBLE
                    width = 768
                    height = 512
                    size1.setBackgroundResource(R.drawable.radio_unselect)
                    size2.setBackgroundResource(R.drawable.radio_unselect)
                    size3.setBackgroundResource(R.drawable.radio_unselect)
                    size4.setBackgroundResource(R.drawable.radio_unselect)
                    size5.setBackgroundResource(R.drawable.radio_back_select)
                    sizeForGeneration = size5.text.toString()
                    Log.i("sizeForGeneration", sizeForGeneration)

                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        signInAnonymously()
        loadAd()
        Log.i("ad", "loaded")

    }

    override fun onResume() {
        super.onResume()
        loadAd()
        Log.i("ad", "loaded")


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

        if (user?.isAnonymous == true) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("Users").document(uid)

            // Проверяем, существует ли документ
            userDoc.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Документ существует, не нужно ничего обновлять
                        Log.i("USER INFORMATION", "Document already exists")
                        val isUserPaid = documentSnapshot.getBoolean("isUserPaid") ?: false
                        val txt2img = documentSnapshot.get("numberOfImg2Img")
                        Log.i("USER INFORMATION", "isUserPaid: $isUserPaid")

                        // В зависимости от значения isUserPaid, выводим соответствующее сообщение
                        if (isUserPaid) {
                            Log.i("USER INFORMATION", "The user is paid.")
                            bind.button4.visibility = View.INVISIBLE
                            bind.imageView5.visibility = View.VISIBLE
                            bind.textView16.visibility = View.VISIBLE
                            bind.textView16.text = "∞"
                            //bind.textView16.text = txt2img.toString()

                        } else {
                            Log.i("USER INFORMATION", "The user is not paid.")
                            bind.button4.visibility = View.VISIBLE
                            bind.imageView5.visibility = View.INVISIBLE
                            bind.textView16.visibility = View.INVISIBLE
                            bind.textView16.text = txt2img.toString()
                        }
                    } else {
                        // Документ не существует, выполняем запись данных
                        val userData = hashMapOf(
                            "isUserPaid" to false,
                            "numberOfTxt2Img" to 5,
                            "numberOfImg2Img" to 1,
                            "uuid" to uid
                        )

                        userDoc.set(userData)
                            .addOnSuccessListener {
                                // Данные успешно записаны
                                Log.i("USER INFORMATION", "Data written successfully")
                            }
                            .addOnFailureListener {
                                // Ошибка записи данных
                                Log.e("USER INFORMATION", "Failed to write data")
                            }
                    }
                }
                .addOnFailureListener {
                    // Ошибка получения документа
                    Log.e("USER INFORMATION", "Failed to get document")
                }
        }

    }

}
