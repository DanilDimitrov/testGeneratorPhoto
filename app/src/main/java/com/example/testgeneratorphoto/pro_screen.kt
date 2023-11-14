package com.example.testgeneratorphoto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import com.example.testgeneratorphoto.databinding.ActivityProScreenBinding

class pro_screen : AppCompatActivity() {

    val uiIntarface = UIIntreface()
    lateinit var bind: ActivityProScreenBinding
    private var selectedRadioButton: RadioButton? = null
    var buttonText = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityProScreenBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.textView22.paint?.shader = uiIntarface.textApp(bind.textView22, "or try one-time purchase")

        // Установка yearPro как выбранного по умолчанию перед установкой слушателя

        bind.weekPro.setOnCheckedChangeListener { _, isChecked ->
            handleRadioButtonState(bind.weekPro, isChecked)
        }

        bind.monthPro.setOnCheckedChangeListener { _, isChecked ->
            handleRadioButtonState(bind.monthPro, isChecked)
        }

        bind.yearPro.setOnCheckedChangeListener { _, isChecked ->
            handleRadioButtonState(bind.yearPro, isChecked)
        }

        bind.weekPro.setOnClickListener { buttonText = 6.99 }
        bind.monthPro.setOnClickListener { buttonText = 14.99 }
        bind.yearPro.setOnClickListener { buttonText = 29.99 }

        bind.contine.setOnClickListener {
            if(buttonText == 0.0)
                Toast.makeText(this, "Please choose your plan", Toast.LENGTH_SHORT).show()
           else
                Log.i("buttonText", buttonText.toString())
        }

        bind.imageButton5.setOnClickListener {
            onBackPressed()
        }
    }


    private fun handleRadioButtonState(radioButton: RadioButton, isChecked: Boolean) {
        if (isChecked) {
            radioButton.setBackgroundResource(R.drawable.radio_back_select)
            selectedRadioButton?.let {
                it.isChecked = false
                it.setBackgroundResource(R.drawable.radio_pro1)
            }
            selectedRadioButton = radioButton
        } else {
            radioButton.setBackgroundResource(R.drawable.radio_pro1)
            if (selectedRadioButton == radioButton) {
                selectedRadioButton = null
            }
        }
    }


}


