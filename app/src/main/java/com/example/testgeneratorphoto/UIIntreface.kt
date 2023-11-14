package com.example.testgeneratorphoto
import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme



class UIIntreface {
    fun textApp(textView: TextView, text: String): LinearGradient {

        textView.text = text

        val paint = textView.paint
        val widthText = paint.measureText("ReImage")

        return LinearGradient(
            0f, 0f, widthText, textView.textSize,
            intArrayOf(
                Color.parseColor("#BF5AF2"),
                Color.parseColor("#64D2FF"),
                Color.parseColor("#0A84FF"),

                ), null, Shader.TileMode.CLAMP
        )
    }




}