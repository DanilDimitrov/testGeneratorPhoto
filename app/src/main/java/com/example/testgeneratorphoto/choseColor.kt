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
            toGenerateIntent.putExtra("selectedImageUri", selectedImageUri)
            toGenerateIntent.putExtra("color", color)
            toGenerateIntent.putExtra("promptModel", gson.toJson(prompt))
            startActivity(toGenerateIntent)
            finish()
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


