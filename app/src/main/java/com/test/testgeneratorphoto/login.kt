package com.test.testgeneratorphoto

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.test.testgeneratorphoto.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class login : AppCompatActivity() {
    lateinit var bind: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val auth = Firebase.auth

    bind.contine4.setOnClickListener {
        if(bind.email2.text.toString() != "" && bind.password2.text.toString() != "") {

            auth.signInWithEmailAndPassword(
                bind.email2.text.toString(),
                bind.password2.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Успішно ввійшли в обліковий запис
                        val user = auth.currentUser
                        val uid = user?.uid
                        if (uid != null) {
                            Log.d("TAG", "User UID: $uid")
                        } else {
                            Log.d("TAG", "UID is null")
                        }
                    } else {
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        val dialog = Dialog(this)
                        dialog.setContentView(R.layout.alert_email)

                        val Ok = dialog.findViewById<TextView>(R.id.textView40)
                        Ok.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.show()
                    }
                }
        }
        else{
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.alert_email)

            val Ok = dialog.findViewById<TextView>(R.id.textView40)
            Ok.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
    }
}