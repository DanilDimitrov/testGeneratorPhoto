package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.testgeneratorphoto.databinding.ActivityPropertiesBinding

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
            // login
        }
        bind.cardView10.setOnClickListener {
            // notification
        }
        bind.cardView11.setOnClickListener {
            // restore purchase
        }
        bind.singoutCard.setOnClickListener {
            // sing Out
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
            val toHome = Intent(this, Gallery::class.java)
            startActivity(toHome)
            finish()
        }
    }
}