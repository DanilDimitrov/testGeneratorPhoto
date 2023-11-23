package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.testgeneratorphoto.databinding.ActivityEmailSignUpBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlin.math.sign

class emailSignUp : AppCompatActivity() {
    lateinit var bind: ActivityEmailSignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityEmailSignUpBinding.inflate(layoutInflater)
        setContentView(bind.root)
        fun emailAuth() {
            val credential = EmailAuthProvider.getCredential(bind.email.text.toString(), bind.password.text.toString())
            auth = Firebase.auth
            val currentUser = auth.currentUser
            val uid = currentUser?.uid

            uid?.let {
                val userDoc = FirebaseFirestore.getInstance().collection("Users").document(it)
                userDoc.get().addOnCompleteListener { documentSnapshot ->
                    if (documentSnapshot.isSuccessful && documentSnapshot.result.exists()) {
                        currentUser.linkWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("TAG", "linkWithCredential:success")
                                    val linkedUser = task.result?.user
                                    Log.d("linkedUser", linkedUser.toString())

                                } else {
                                    Log.w("TAG", "linkWithCredential:failure", task.exception)
                                }
                            }
                    } else {
                        Log.d("TAG", "No such document")
                    }
                }
            }
        }
        bind.imageButton7.setOnClickListener {
            val back = Intent(this, signUp::class.java)
            startActivity(back)
            finish()
        }

        bind.contine3.setOnClickListener {
            emailAuth()
        }





    }
}