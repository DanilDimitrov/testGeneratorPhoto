package com.example.testgeneratorphoto

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.example.testgeneratorphoto.databinding.ActivityProScreenBinding
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.play.core.integrity.e
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class pro_screen : AppCompatActivity(), PaymentResultWithDataListener {

    val uiIntarface = UIIntreface()
    lateinit var bind: ActivityProScreenBinding
    private var selectedRadioButton: RadioButton? = null
    var buttonText = 0.0
    lateinit var auth: FirebaseAuth
    var coinsPurchase = 0
    private val client = OkHttpClient()
    lateinit var user: FirebaseUser
    var isProPay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityProScreenBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = FirebaseAuth.getInstance()

        user = auth.currentUser!!

        bind.textView22.paint?.shader =
            uiIntarface.textApp(bind.textView22, "or try one-time purchase")

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

        bind.weekPro.setOnClickListener {
            buttonText = 6.99
            coinsPurchase = 10
        }
        bind.monthPro.setOnClickListener {
            buttonText = 14.99
            coinsPurchase = 40
        }
        bind.yearPro.setOnClickListener {
            buttonText = 29.99
            coinsPurchase = 500
        }

        bind.contine2.setOnClickListener {
            if (buttonText == 0.0)
                Toast.makeText(this, "Please choose your plan", Toast.LENGTH_SHORT).show()
            else
                startPayment(true)
        }



        bind.contine.setOnClickListener {
            if (buttonText == 0.0)
                Toast.makeText(this, "Please choose your plan", Toast.LENGTH_SHORT).show()
            else
                startPayment(false)
        }

        bind.imageButton5.setOnClickListener {
            onBackPressed()
        }
        bind.textView22.setOnClickListener {
            bind.purchase.visibility = View.VISIBLE
        }
        bind.imageButton2.setOnClickListener {
            bind.purchase.visibility = View.INVISIBLE
        }

        bind.radioButton.setOnCheckedChangeListener { _, isChecked ->
            handleRadioButtonStateForPurchase(bind.radioButton, isChecked)
        }
        bind.radioButton2.setOnCheckedChangeListener { _, isChecked ->
            handleRadioButtonStateForPurchase(bind.radioButton2, isChecked)
        }
        bind.radioButton3.setOnCheckedChangeListener { _, isChecked ->
            handleRadioButtonStateForPurchase(bind.radioButton3, isChecked)
        }
        bind.radioButton.setOnClickListener {
            buttonText = 3.99
            coinsPurchase = 10
        }
        bind.radioButton2.setOnClickListener {
            buttonText = 5.99
            coinsPurchase = 20
        }
        bind.radioButton3.setOnClickListener {
            buttonText = 9.99
            coinsPurchase = 50
        }

        Checkout.preload(applicationContext)
        val co = Checkout()

        co.setKeyID("rzp_test_MYngo4pQkize5I")

    }

    private fun updatePlanForPurchase(user: FirebaseUser?, coins: Int) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("Users").document(user.uid)
            userDoc.get()
                .addOnSuccessListener {documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val buyCoins = documentSnapshot.getLong("buyCoins")

                        userDoc.update("isUserPaid", true)
                            .addOnSuccessListener {
                                // Данные успешно записаны
                                Log.i("USER INFORMATION", "Data written successfully")
                            }
                            .addOnFailureListener {
                                // Ошибка записи данных
                                Log.e("USER INFORMATION", "Failed to write data")
                            }
                        userDoc.update("buyCoins", coins + buyCoins!!)
                            .addOnSuccessListener {
                                // Данные успешно записаны
                                Log.i("USER INFORMATION", "Data written successfully")
                            }
                            .addOnFailureListener {
                                // Ошибка записи данных
                                Log.e("USER INFORMATION", "Failed to write data")
                            }

                    }
                    }
                }
        }
    private fun updatePlanForPro(user: FirebaseUser?, coins: Int) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("Users").document(user.uid)
            userDoc.get()
                .addOnSuccessListener {documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val currentTime = com.google.firebase.Timestamp.now()
                        val nextWeek = currentTime.seconds + (7 * 24 * 60 * 60)

                        val buyCoins = documentSnapshot.getLong("buyCoins")

                        userDoc.update("isUserPaid", true)
                            .addOnSuccessListener {
                                // Данные успешно записаны
                                Log.i("USER INFORMATION", "Data written successfully")
                            }
                            .addOnFailureListener {
                                // Ошибка записи данных
                                Log.e("USER INFORMATION", "Failed to write data")
                            }

                        userDoc.update("buyCoins", coins + buyCoins!!)
                            .addOnSuccessListener {
                                // Данные успешно записаны
                                Log.i("USER INFORMATION", "Data written successfully")
                            }
                            .addOnFailureListener {
                                // Ошибка записи данных
                                Log.e("USER INFORMATION", "Failed to write data")
                            }

                        userDoc.update("isUserAccessGallery", true)
                            .addOnSuccessListener {
                                // Данные успешно записаны
                                Log.i("USER INFORMATION", "Data written successfully")
                            }
                            .addOnFailureListener {
                                // Ошибка записи данных
                                Log.e("USER INFORMATION", "Failed to write data")
                            }

                        userDoc.update("time", com.google.firebase.Timestamp(nextWeek, 0))
                            .addOnSuccessListener {
                                // Час успішно оновлено
                                Log.i("USER INFORMATION", "Time updated successfully")
                            }
                            .addOnFailureListener {
                                // Помилка оновлення часу
                                Log.e("USER INFORMATION", "Failed to update time")
                            }

                    }
                }
        }
    }

    private fun getOrder(amount: Int,callback: (String) -> Unit) {
        val credentials = Credentials.basic("rzp_test_MYngo4pQkize5I", "3eSpKWgCDypjEUta04f7Q4w8")


        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(), """
            {
                "amount": ${amount*100},
                "currency": "USD",
                "receipt": "qwsaq1",
                "partial_payment": true,
                "first_payment_min_amount": 100
            }
        """.trimIndent()
        )

        val request = Request.Builder()
            .url("https://api.razorpay.com/v1/orders")
            .post(requestBody)
            .addHeader("content-type", "application/json")
            .addHeader(
                "Authorization", credentials
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse  = response.body?.string()
                val gson = Gson()
                val jsonObject = gson.fromJson(jsonResponse, Map::class.java)
                Log.i("jsonResponse", jsonResponse.toString())
                Log.i("jsonObject", jsonObject.toString())

                val orderId = jsonObject["id"] as String
                callback(orderId)
                println(jsonResponse)
            }
        })
    }

    private fun startPayment(isPaymentForCoins: Boolean) {

        val activity: Activity = this
        val co = Checkout()
        getOrder(buttonText.toInt()) { orderId ->
            try {
                val options = JSONObject()
                options.put("name","ReImageApp")
                options.put("description","Demoing Charges")
                options.put("image","https://firebasestorage.googleapis.com/v0/b/reimageapp.appspot.com/o/Pictures%2FLOGO%20XL.png?alt=media&token=10fedf68-df42-4d66-807a-19aa7f7347c5")
                options.put("theme.color", "#3399cc");
                options.put("currency","USD");
                options.put("order_id", orderId); // Використовуйте orderId зі зворотнього виклику
                options.put("amount", buttonText.toInt())

                co.open(activity, options)

                isProPay = !isPaymentForCoins

            } catch (e: Exception) {
                Toast.makeText(activity, "Error in payment: "+ e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }




    private fun handleRadioButtonStateForPurchase(radioButton: RadioButton, isChecked: Boolean) {
        if (isChecked) {
            radioButton.setBackgroundResource(R.drawable.radio_back_select)
            selectedRadioButton?.let {
                it.isChecked = false
                it.setBackgroundResource(R.drawable.unselect)
            }
            selectedRadioButton = radioButton
        } else {
            radioButton.setBackgroundResource(R.drawable.unselect)
            if (selectedRadioButton == radioButton) {
                selectedRadioButton = null
            }
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

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        if(isProPay)
            updatePlanForPro(user, coinsPurchase)
        else
            updatePlanForPurchase(user, coinsPurchase)

    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Log.e("PAYMENT", "ALL BAD")
    }


}


