package com.test.testgeneratorphoto
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView


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