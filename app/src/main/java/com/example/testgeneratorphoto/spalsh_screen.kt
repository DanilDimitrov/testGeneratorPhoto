package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.testgeneratorphoto.databinding.ActivitySpalshScreenBinding

class spalsh_screen : AppCompatActivity() {
    lateinit var bind: ActivitySpalshScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        bind = ActivitySpalshScreenBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        val handler = Handler()
        var progress = 0
        runOnUiThread {

            val runnable = object : Runnable {
                override fun run() {
                    if (progress <= 100) {
                        progress++
                        bind.progressBar2.setProgress(progress, true)
                        bind.progressBar2.progress = progress

                        handler.postDelayed(this, 25) //

                    } else {
                        val toHome = Intent(this@spalsh_screen, changePhoto::class.java)
                        startActivity(toHome)
                        finish()
                    }
                }
            }
            handler.post(runnable)
        }
    }

}