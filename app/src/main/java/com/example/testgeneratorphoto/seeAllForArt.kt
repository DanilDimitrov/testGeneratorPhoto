package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testgeneratorphoto.databinding.ActivitySeeAllForArtBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class seeAllForArt : AppCompatActivity() {
    lateinit var bind: ActivitySeeAllForArtBinding
    private lateinit var allStyles: List<artModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySeeAllForArtBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val allStylesJson = intent.getStringExtra("allStylesInCategory")

        val gson = Gson()
        val type = object : TypeToken<List<artModel>>() {}.type
        allStyles = gson.fromJson(allStylesJson, type)
        Log.i("allStyles", allStyles.toString())

        val seeAllAdapter = seeAllArtAdapter(allStyles)
        val seeAllAdapterLayoutManager = GridLayoutManager(this,2)
        bind.seeAllArtStyles.adapter = seeAllAdapter
        bind.seeAllArtStyles.layoutManager = seeAllAdapterLayoutManager

        seeAllAdapter.setOnItemClickListener {
                prompt ->
            Log.i("PROMPT", prompt.toString())
            val styleForMainActivity = Intent(this@seeAllForArt, MainActivity::class.java)
            val styleInJson = gson.toJson(prompt)
            Log.i("styleInJson", styleInJson.toString())

            styleForMainActivity.putExtra("promptFromSeeAll", styleInJson)
            startActivity(styleForMainActivity)
            finish()

        }

        bind.backFromAllArtStyle.setOnClickListener{
            val FromAllStyle = Intent(this, MainActivity::class.java)
            startActivity(FromAllStyle)
            finish()
        }
    }
}