package com.example.testgeneratorphoto

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.Fuel

import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.stripe.android.paymentsheet.PaymentSheet

import com.example.testgeneratorphoto.databinding.ActivityProScreenBinding
import com.github.kittinunf.fuel.core.extensions.authenticate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class PaymentType {
    PRO, // кнопка для PRO
    PURCHASE // кнопка для покупки
}
class pro_screen : AppCompatActivity() {



    private var paymentType: PaymentType = PaymentType.PURCHASE // по умолчанию выбрана кнопка для покупки


    val uiIntarface = UIIntreface()
    lateinit var bind: ActivityProScreenBinding
    private var selectedRadioButton: RadioButton? = null
    var buttonText = 0.0
    lateinit var auth: FirebaseAuth
    var coinsPurchase = 0
    lateinit var user: FirebaseUser
    var isProPay = false
    lateinit var paymentSheet: PaymentSheet
    var lateTime = 0

    private suspend fun presentPaymentSheet() {


            createCustomer { customerid ->
                lifecycleScope.launch {
                createEphemeralKey(customerid!!) { key ->
                    lifecycleScope.launch {
                        createPaymentIntent(
                            customerid,
                            buttonText.toInt() * 100,
                            "usd"
                        ) { clientSecret ->


                            val paymentIntentClientSecret = clientSecret
                            val customerId = customerid
                            val ephemeralKeySecret = key!!

                            val customerConfig = PaymentSheet.CustomerConfiguration(
                                id = customerId,
                                ephemeralKeySecret = ephemeralKeySecret
                            )

                            // Представление PaymentSheet
                            paymentSheet.presentWithPaymentIntent(
                                paymentIntentClientSecret!!,
                                PaymentSheet.Configuration(
                                    merchantDisplayName = "ReImage",
                                    customer = customerConfig,
                                    allowsDelayedPaymentMethods = true // Установите в true, если нужно поддерживать отложенные методы оплаты
                                )
                            )
                        }
                    }
                }

            }
        }
    }

     fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        // Обработка результатов оплаты
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                // Оплата была отменена
            }
            is PaymentSheetResult.Failed -> {
                // Оплата не удалась, обработка ошибки
                val errorMessage = paymentSheetResult.error
            }
            is PaymentSheetResult.Completed -> {
                if (paymentType == PaymentType.PRO) {
                    updatePlanForPro(user, coinsPurchase)
                } else {
                    updatePlanForPurchase(user, coinsPurchase)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityProScreenBinding.inflate(layoutInflater)
        setContentView(bind.root)

// Инициализация PaymentSheet
        paymentSheet = PaymentSheet(this) { paymentResult ->
            // Обработка результата платежа
            when (paymentResult) {
                is PaymentSheetResult.Canceled -> {
                    // Платеж был отменен
                }
                is PaymentSheetResult.Failed -> {
                    // Платеж завершился ошибкой
                    val errorMessage = paymentResult.error
                }
                is PaymentSheetResult.Completed -> {
                    if (paymentType == PaymentType.PRO) {
                        updatePlanForPro(user, coinsPurchase)
                    } else {
                        updatePlanForPurchase(user, coinsPurchase)
                    }
                }
            }
        }

        // Установка вашего publishable key Stripe (замените YOUR_PUBLISHABLE_KEY на ваш ключ)
        PaymentConfiguration.init(this, "pk_test_51OG5xKAfrrG9qEaCMPyGe3LOSbGuxm16fAwdP27LQ0tpA5jOipJ0k6BuNvbpWx3CwuxxntxslSIDBQIhjSg78GxM00JUtRGFBe")

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
            lateTime = 0
        }
        bind.monthPro.setOnClickListener {
            buttonText = 14.99
            coinsPurchase = 40
            lateTime = 1

        }
        bind.yearPro.setOnClickListener {
            buttonText = 29.99
            coinsPurchase = 500
            lateTime = 2

        }

        bind.contine2.setOnClickListener {
            if (buttonText == 0.0)
                Toast.makeText(this, "Please choose your plan", Toast.LENGTH_SHORT).show()
            else {
                paymentType = PaymentType.PURCHASE
                lifecycleScope.launch{
                    presentPaymentSheet()
                }

            }
        }

        bind.contine.setOnClickListener {
            if (buttonText == 0.0)
                Toast.makeText(this, "Please choose your plan", Toast.LENGTH_SHORT).show()
            else {
                paymentType = PaymentType.PRO
                lifecycleScope.launch{
                    presentPaymentSheet()
                }
            }
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
                        val nextMonth = currentTime.seconds + ((7 * 24 * 60 * 60) *4)
                        val nextYear = currentTime.seconds + (((7 * 24 * 60 * 60) *4)*12)

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
                        if(lateTime == 0){
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
                        else if(lateTime == 1){
                            userDoc.update("time", com.google.firebase.Timestamp(nextMonth, 0))
                                .addOnSuccessListener {
                                    // Час успішно оновлено
                                    Log.i("USER INFORMATION", "Time updated successfully")
                                }
                                .addOnFailureListener {
                                    // Помилка оновлення часу
                                    Log.e("USER INFORMATION", "Failed to update time")
                                }
                        }
                        else{
                            userDoc.update("time", com.google.firebase.Timestamp(nextYear, 0))
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
    }

    suspend fun createCustomer(callback: (String?) -> Unit) {
        try {
            val apiKey = "sk_test_51OG5xKAfrrG9qEaCqX43OcEI570XwisWG7W7dmqPUALeH3eK3mqWkd66MKeenbM2HlXPZlFWfjal1zYfVxQbtVnr00lxMzhOH4"
            val url = "https://api.stripe.com/v1/customers"

            val (_, response, result) = withContext(Dispatchers.IO) {
                Fuel.post(url)
                    .authenticate(apiKey, "")
                    .responseString()
            }

            Log.i("createCustomer", response.toString())
            Log.i("createCustomer", result.get().toString())

            result.fold(
                success = { data ->
                    Log.i("resultCustomer", data)
                    when (response.statusCode) {
                        200 -> {
                            val customerId = parseCustomerId(data)
                            callback(customerId)
                        }
                        else -> callback(null)
                    }
                },
                failure = { error ->
                    // Обработка ошибки при выполнении запроса
                    Log.e("resultCustomer", "Error: ${error.exception}")
                    callback(null)
                }
            )
        } catch (e: Exception) {
            // Обработка других исключений, если таковые возникнут
            Log.e("createCustomer", "Error: ${e.message}")
            callback(null)
        }
    }



    private fun parseCustomerId(responseBody: String): String? {
        val regex = Regex("""\"id\"\s*:\s*\"(\w+)\"""")
        val matchResult = regex.find(responseBody)
        return matchResult?.groupValues?.get(1)
    }

    suspend fun createEphemeralKey(customerId: String, callback: (String?) -> Unit) {
        try {
            val apiKey = "sk_test_51OG5xKAfrrG9qEaCqX43OcEI570XwisWG7W7dmqPUALeH3eK3mqWkd66MKeenbM2HlXPZlFWfjal1zYfVxQbtVnr00lxMzhOH4"
            val url = "https://api.stripe.com/v1/ephemeral_keys"
            val stripeVersion = "2023-10-16"

            val (_, response, result) = withContext(Dispatchers.IO) {
                Fuel.post(url)
                    .authenticate(apiKey, "")
                    .header("Stripe-Version", stripeVersion)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("customer=$customerId")
                    .responseString()
            }

            Log.i("createEphemeralKey", response.toString())
            Log.i("createEphemeralKey", result.get().toString())

            result.fold(
                success = { data ->
                    Log.i("createEphemeralKey", data)
                    when (response.statusCode) {
                        200 -> {
                            val key = parseCustomerId(data)
                            callback(key)
                        }
                        else -> callback(null)
                    }
                },
                failure = { error ->
                    // Обработка ошибки при выполнении запроса
                    Log.e("createEphemeralKey", "Error: ${error.exception}")
                    callback(null)
                }
            )
        } catch (e: Exception) {
            // Обработка других исключений, если таковые возникнут
            Log.e("createEphemeralKey", "Error: ${e.message}")
            callback(null)
        }
    }



    suspend fun createPaymentIntent(customerId: String, amount: Int, currency: String, callback: (String?) -> Unit) {
        try {
            val apiKey = "sk_test_51OG5xKAfrrG9qEaCqX43OcEI570XwisWG7W7dmqPUALeH3eK3mqWkd66MKeenbM2HlXPZlFWfjal1zYfVxQbtVnr00lxMzhOH4"
            val url = "https://api.stripe.com/v1/payment_intents"
            val automaticPaymentEnabled = true

            val (_, response, result) = withContext(Dispatchers.IO) {
                Fuel.post(url)
                    .authenticate(apiKey, "")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(
                        "customer=$customerId" +
                                "&amount=$amount" +
                                "&currency=$currency" +
                                "&automatic_payment_methods[enabled]=$automaticPaymentEnabled"
                    )
                    .responseString()
            }
            Log.i("createPaymentIntent", response.toString())
            Log.i("createPaymentIntent", result.get().toString())

            result.fold(
                success = { data ->
                    Log.i("createPaymentIntent", data)
                    when (response.statusCode) {
                        200 -> {
                            val responseBody = data.trimIndent()
                            val jsonObject = JSONObject(responseBody)
                            val clientSecret = jsonObject.getString("client_secret")
                            callback(clientSecret)
                        }
                        else -> callback(null)
                    }
                },
                failure = { error ->
                    // Обработка ошибки при выполнении запроса
                    Log.e("createPaymentIntent", "Error: ${error.exception}")
                    callback(null)
                }
            )
        } catch (e: Exception) {
            // Обработка других исключений, если таковые возникнут
            Log.e("createPaymentIntent", "Error: ${e.message}")
            callback(null)
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




}


