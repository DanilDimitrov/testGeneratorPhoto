package com.test.testgeneratorphoto

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.test.testgeneratorphoto.databinding.ActivityChoseColorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class choseColor : AppCompatActivity() {
    lateinit var bind: ActivityChoseColorBinding
lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityChoseColorBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val gson = Gson()
        var savedToGallery = false
        auth = FirebaseAuth.getInstance()
        auth = Firebase.auth



        val yourImageButton = findViewById<ImageButton>(R.id.imageButton3)

        val styleinjson = intent.getStringExtra("promptModel")



        val selectedImageUri = intent.getParcelableExtra<Uri>("selectedImageUri")
        val type = object : TypeToken<List<Model>>() {}.type
        Log.i("styleinjson", styleinjson.toString())
        Log.i("selectedImageUri", selectedImageUri.toString())

        val promptModel = gson.fromJson<List<Model>>(styleinjson, type)

        Log.i("promptModel", promptModel.toString())

        fun sendToGenerate(color: String, selectedImageUri: Uri, prompt: List<Model>) {
            val toGenerateIntent = Intent(this@choseColor, generatePhoto::class.java)
            val toHome = Intent(this@choseColor, changePhoto::class.java)

            // Пользователь хочет уменьшить numberOfImg2Img
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("Users").document(currentUser.uid)
                userDoc.get()
                    .addOnSuccessListener { documentSnapshot ->
                        val numberOfImg2Img = documentSnapshot.getLong("numberOfImg2Img")
                        val isUserAccessGallery = documentSnapshot.getBoolean("isUserAccessGallery")
                        val isUserPaid = documentSnapshot.getBoolean("isUserPaid")

                        Log.i("isUserAccessGallery", isUserAccessGallery.toString())


                        val imageList = documentSnapshot.get("imagesUrls") as? ArrayList<String>
                        val buyCoins = documentSnapshot.getLong("buyCoins")


                        if(isUserAccessGallery == true){
                            savedToGallery = true

                            if(imageList!!.size == 10){
                                savedToGallery = false
                                val dialog = Dialog(this)
                                dialog.setContentView(R.layout.alert_galerry_full)

                                val Cancel = dialog.findViewById<TextView>(R.id.Cancel)
                                val Ok = dialog.findViewById<TextView>(R.id.Ok)

                                Cancel.setOnClickListener {
                                    val toGallery = Intent(this, Gallery::class.java)
                                    startActivity(toGallery)
                                    dialog.dismiss()
                                    finish()
                                }

                                Ok.setOnClickListener {
                                    dialog.dismiss()
                                    if ((numberOfImg2Img != null && buyCoins != null) && (numberOfImg2Img > 0 || buyCoins > 0)) {
                                        if(!isUserPaid!!){
                                            // Уменьшить количество монеток
                                            userDoc.update("numberOfImg2Img", numberOfImg2Img - 1)
                                                .addOnSuccessListener {
                                                    // Значение numberOfImg2Img уменьшено на 1
                                                    Log.i("choseColor", "numberOfImg2Img decremented successfully.")
                                                    toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
                                                    toGenerateIntent.putExtra("savedToGallery", savedToGallery)
                                                    toGenerateIntent.putExtra("color", color)
                                                    toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
                                                    Log.i("savedToGallery", savedToGallery.toString())

                                                    startActivity(toGenerateIntent)
                                                    finish()
                                                }
                                                .addOnFailureListener {
                                                    Log.e("choseColor", "Failed to decrement numberOfImg2Img.")
                                                }
                                        }else{
                                            // Уменьшить количество монеток
                                            userDoc.update("buyCoins", buyCoins - 1)
                                                .addOnSuccessListener {
                                                    // Значение numberOfImg2Img уменьшено на 1
                                                    Log.i("choseColor", "numberOfImg2Img decremented successfully.")
                                                    toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
                                                    toGenerateIntent.putExtra("savedToGallery", savedToGallery)
                                                    toGenerateIntent.putExtra("color", color)
                                                    toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
                                                    startActivity(toGenerateIntent)
                                                    finish()
                                                }
                                                .addOnFailureListener {
                                                    Log.e("choseColor", "Failed to decrement numberOfImg2Img.")
                                                }
                                        }

                                    } else {
                                        Log.e("choseColor", "Not enough coins for numberOfImg2Img.")
                                        toHome.putExtra("noCoins",true)
                                        startActivity(toHome)
                                        finish()
                                    }
                                }

                                dialog.show()
                            }
                            else{
                                savedToGallery = true
                                if ((numberOfImg2Img != null && buyCoins != null) && (numberOfImg2Img > 0 || buyCoins > 0)) {
                                    if(!isUserPaid!!) {

                                        // Уменьшить количество монеток
                                        userDoc.update("numberOfImg2Img", numberOfImg2Img - 1)
                                            .addOnSuccessListener {
                                                // Значение numberOfImg2Img уменьшено на 1
                                                Log.i("choseColor", "numberOfImg2Img decremented successfully.")
                                                toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
                                                toGenerateIntent.putExtra("savedToGallery", savedToGallery)
                                                toGenerateIntent.putExtra("color", color)
                                                toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
                                                Log.i("savedToGallery", savedToGallery.toString())

                                                startActivity(toGenerateIntent)
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                Log.e("choseColor", "Failed to decrement numberOfImg2Img.")
                                            }
                                    }else{

                                        // Уменьшить количество монеток
                                        userDoc.update("buyCoins", buyCoins - 1)
                                            .addOnSuccessListener {
                                                // Значение numberOfImg2Img уменьшено на 1
                                                Log.i("choseColor", "numberOfImg2Img decremented successfully.")
                                                toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
                                                toGenerateIntent.putExtra("savedToGallery", savedToGallery)
                                                toGenerateIntent.putExtra("color", color)
                                                toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
                                                startActivity(toGenerateIntent)
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                Log.e("choseColor", "Failed to decrement numberOfImg2Img.")
                                            }
                                    }

                                } else {
                                    Log.e("choseColor", "Not enough coins for numberOfImg2Img.")
                                    toHome.putExtra("noCoins",true)
                                    startActivity(toHome)
                                    finish()
                                }
                            }
                        }else {
                            savedToGallery = false

                            if ((numberOfImg2Img != null && buyCoins != null) && (numberOfImg2Img > 0 || buyCoins > 0)) {
                                if(!isUserPaid!!) {
                                    // Уменьшить количество монеток
                                    userDoc.update("numberOfImg2Img", numberOfImg2Img - 1)
                                        .addOnSuccessListener {
                                            // Значение numberOfImg2Img уменьшено на 1
                                            Log.i("choseColor", "numberOfImg2Img decremented successfully.")
                                            toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
                                            toGenerateIntent.putExtra("savedToGallery", savedToGallery)
                                            toGenerateIntent.putExtra("color", color)
                                            toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
                                            startActivity(toGenerateIntent)
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Log.e("choseColor", "Failed to decrement numberOfImg2Img.")
                                        }
                                }else{
                                    // Уменьшить количество монеток
                                    userDoc.update("buyCoins", buyCoins - 1)
                                        .addOnSuccessListener {
                                            // Значение numberOfImg2Img уменьшено на 1
                                            Log.i("choseColor", "numberOfImg2Img decremented successfully.")
                                            toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
                                            toGenerateIntent.putExtra("savedToGallery", savedToGallery)
                                            toGenerateIntent.putExtra("color", color)
                                            toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
                                            startActivity(toGenerateIntent)
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Log.e("choseColor", "Failed to decrement numberOfImg2Img.")
                                        }
                                }

                            } else {
                                Log.e("choseColor", "Not enough coins for numberOfImg2Img.")
                                toHome.putExtra("noCoins",true)
                                startActivity(toHome)
                                finish()
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Ошибка при получении данных пользователя
                        Log.e("choseColor", "Failed to get user data.")
                    }
            }


        }
        bind.imageButton4.setOnClickListener {
            val toChoseStyle = Intent(this, changePhoto::class.java)
            startActivity(toChoseStyle)
            finish()
        }
        fun select(){
            runOnUiThread {
                if (bind.latinaRadioButton.isChecked) {
                    bind.latinaRadioButton.setBackgroundResource(R.drawable.radio_back_select)
                    bind.asiaHandRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.negrRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.whiteHandRadioButton2.setBackgroundResource(R.drawable.radio_unselect)
                } else if (bind.whiteHandRadioButton2.isChecked) {
                    bind.latinaRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.asiaHandRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.negrRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.whiteHandRadioButton2.setBackgroundResource(R.drawable.radio_back_select)
                } else if (bind.asiaHandRadioButton.isChecked) {
                    bind.latinaRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.asiaHandRadioButton.setBackgroundResource(R.drawable.radio_back_select)
                    bind.negrRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.whiteHandRadioButton2.setBackgroundResource(R.drawable.radio_unselect)
                } else if (bind.negrRadioButton.isChecked) {
                    bind.latinaRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.asiaHandRadioButton.setBackgroundResource(R.drawable.radio_unselect)
                    bind.negrRadioButton.setBackgroundResource(R.drawable.radio_back_select)
                    bind.whiteHandRadioButton2.setBackgroundResource(R.drawable.radio_unselect)
                }
            }

        }
      bind.latinaRadioButton.setOnClickListener {
          yourImageButton.visibility = View.INVISIBLE
          bind.button.visibility = View.VISIBLE
          bind.asiaHandRadioButton.isChecked = false
          bind.negrRadioButton.isChecked = false
          bind.whiteHandRadioButton2.isChecked = false
          bind.latinaRadioButton.isChecked = true
          runOnUiThread {select()}
          bind.button.setOnClickListener {
              sendToGenerate("Latina", selectedImageUri!!, promptModel)
          }

      }
        bind.whiteHandRadioButton2.setOnClickListener {
                        yourImageButton.visibility = View.INVISIBLE
                        bind.button.visibility = View.VISIBLE
                        bind.asiaHandRadioButton.isChecked = false
                        bind.negrRadioButton.isChecked = false
                        bind.whiteHandRadioButton2.isChecked = true
                        bind.latinaRadioButton.isChecked = false
            runOnUiThread {select()}
                        bind.button.setOnClickListener {
                            sendToGenerate("White", selectedImageUri!!, promptModel)
                        }
                    }
        bind.asiaHandRadioButton.setOnClickListener {
                        yourImageButton.visibility = View.INVISIBLE
                        bind.button.visibility = View.VISIBLE
                        bind.asiaHandRadioButton.isChecked = true
                        bind.negrRadioButton.isChecked = false
                        bind.whiteHandRadioButton2.isChecked = false
                        bind.latinaRadioButton.isChecked = false
            runOnUiThread {select()}
                        bind.button.setOnClickListener {
                            sendToGenerate("Asia", selectedImageUri!!, promptModel)
                        }
                    }

        bind.negrRadioButton.setOnClickListener {
                        yourImageButton.visibility = View.INVISIBLE
                        bind.button.visibility = View.VISIBLE
                        bind.asiaHandRadioButton.isChecked = false
                        bind.negrRadioButton.isChecked = true
                        bind.whiteHandRadioButton2.isChecked = false
                        bind.latinaRadioButton.isChecked = false
            runOnUiThread {select()}
                        bind.button.setOnClickListener {
                            sendToGenerate("Black", selectedImageUri!!, promptModel)
                        }
                    }




                }

}


