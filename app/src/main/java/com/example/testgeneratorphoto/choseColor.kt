package com.example.testgeneratorphoto

import Manage
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.testgeneratorphoto.databinding.ActivityChoseColorBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class choseColor : AppCompatActivity() {
    lateinit var bind: ActivityChoseColorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityChoseColorBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val manage = Manage()
        val gson = Gson()
        val auth = Firebase.auth

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

                        if (numberOfImg2Img != null && numberOfImg2Img > 0) {
                            // Уменьшить количество монеток
                            userDoc.update("numberOfImg2Img", numberOfImg2Img - 1)
                                .addOnSuccessListener {
                                    // Значение numberOfImg2Img уменьшено на 1
                                    Log.i("choseColor", "numberOfImg2Img decremented successfully.")
                                    toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
                                    toGenerateIntent.putExtra("color", color)
                                    toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
                                    startActivity(toGenerateIntent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Log.e("choseColor", "Failed to decrement numberOfImg2Img.")
                                }
                        } else {
                            Log.e("choseColor", "Not enough coins for numberOfImg2Img.")
                            toHome.putExtra("noCoins",true)
                            startActivity(toHome)
                            finish()
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


