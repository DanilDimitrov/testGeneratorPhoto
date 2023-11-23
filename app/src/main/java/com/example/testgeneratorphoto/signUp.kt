package com.example.testgeneratorphoto

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.testgeneratorphoto.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class signUp : AppCompatActivity() {
    lateinit var bind: ActivitySignUpBinding
    val uiIntarface = UIIntreface()
    private lateinit var auth: FirebaseAuth
    val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(bind.root)

        //UI
        bind.textView50.paintFlags = bind.textView50.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        bind.textView45.paint.shader = uiIntarface.textApp(bind.textView45, "Welcome to Relmage!")

        //UI END

        bind.imageButton7.setOnClickListener {
            val back = Intent(this, properties::class.java)
            startActivity(back)
            finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("155712490881-lfdc04hq7r9qcslt5661e29265t34o5r.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        bind.imageButton15.setOnClickListener {
            val toemailSignUp = Intent(this, emailSignUp::class.java)
            startActivity(toemailSignUp)
        }


        bind.imageButton12.setOnClickListener {
            Log.i("currentUser", currentUser.toString())
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)

        }

        bind.textView50.setOnClickListener {
            val tologin = Intent(this, login::class.java)
            startActivity(tologin)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (task != null) {
                if (task.isSuccess) {
                    val account = task.signInAccount
                    firebaseAuthWithGoogle(account!!)
                } else {
                    Log.i("task", task.status.toString())
                }
            }
        }
    }


    // Подальша обробка аутентифікації через Google
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        val currentUser = auth.currentUser

        val uid = currentUser?.uid

        uid?.let {
            val userDoc = FirebaseFirestore.getInstance().collection("Users").document(it)
            userDoc.get().addOnCompleteListener { documentSnapshot ->
                if (documentSnapshot.isSuccessful && documentSnapshot.result.exists()) {
                    // Об'єднання облікового запису Google з анонімним користувачем
                    currentUser.linkWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("TAG", "linkWithCredential:success")
                                val linkedUser = task.result?.user
                                // Оновлення інтерфейсу після об'єднання облікових записів
                                // updateUI(linkedUser)
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

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}