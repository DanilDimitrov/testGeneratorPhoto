package com.test.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.test.testgeneratorphoto.databinding.ActivityPropertiesBinding
import com.google.firebase.auth.FirebaseAuth

class properties : AppCompatActivity() {
    lateinit var bind: ActivityPropertiesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPropertiesBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.cardView8.setOnClickListener {
            val toPro = Intent(this, pro_screen::class.java)
            startActivity(toPro)
            finish()
        }
        // Account
        bind.cardView9.setOnClickListener {
            val toLogin = Intent(this, signUp::class.java)
            startActivity(toLogin)
        }
        bind.cardView10.setOnClickListener {
            // notification
        }
        bind.cardView11.setOnClickListener {
            // restore purchase
        }
        bind.singoutCard.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            val toHome = Intent(this, changePhoto::class.java)
            startActivity(toHome)

        }
        // General
        bind.cardView13.setOnClickListener {
            // RateApp
        }
        bind.cardView14.setOnClickListener {
            // Support
        }
        bind.languageCard.setOnClickListener {
            // Language
        }

        // Sait
        bind.cardView16.setOnClickListener {
            //privacy policy
        }
        bind.cardView17.setOnClickListener {
            // terms of use
        }
        bind.manageData.setOnClickListener {
            // Manage Data
        }

        bind.goToHome.setOnClickListener {
            onBackPressed()
        }
    }
}