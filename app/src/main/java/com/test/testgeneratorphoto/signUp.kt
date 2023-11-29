package com.test.testgeneratorphoto

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.test.testgeneratorphoto.databinding.ActivitySignUpBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
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
    private lateinit var callbackManager: CallbackManager

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            user.linkWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val linkedUser = task.result?.user
                        val toHome = Intent(this, MainActivity::class.java)
                        startActivity(toHome)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == RESULT_OK) {
            callbackManager.onActivityResult(FACEBOOK_REQUEST_CODE, resultCode, data)
        } else {
            // Обработка отмены или ошибки регистрации
            // Можно добавить соответствующую логику или обработку здесь
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(bind.root)
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    // Пользователь отменил вход через Facebook
                }

                override fun onError(error: FacebookException) {
                    Log.e("FacebookLogin", "Error: ${error.message}")
                }
            })


        //UI
        bind.textView50.paintFlags = bind.textView50.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        bind.textView45.paint.shader = uiIntarface.textApp(bind.textView45, "Welcome to Relmage!")

        //UI END

        bind.imageButton7.setOnClickListener {
            val back = Intent(this, properties::class.java)
            startActivity(back)
            finish()
        }
bind.imageButton14.setReadPermissions(listOf("email", "public_profile"))
        bind.imageButton14.setOnClickListener {
            val loginManager = LoginManager.getInstance()
            loginManager.logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }
        bind.imageButton14.setBackgroundResource(R.drawable.facebook);
        bind.imageButton14.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        bind.imageButton14.setCompoundDrawablePadding(0);
        bind.imageButton14.setPadding(0, 0, 0, 0);
        bind.imageButton14.setText("");

        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN)
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
            mGoogleSignInClient.signOut()

                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)



        }

        bind.textView50.setOnClickListener {
            val tologin = Intent(this, loginPage::class.java)
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
        else{
            callbackManager.onActivityResult(resultCode, resultCode, data)
        }
    }


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
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                val toHome = Intent(this, MainActivity::class.java)
                                startActivity(toHome)
                                finish()

                            } else {
                                Log.w("TAG", "linkWithCredential:failure", task.exception)
                                Toast.makeText(this, "Sign out of this account", Toast.LENGTH_SHORT).show()
                                val toProperties = Intent(this, properties::class.java)
                                startActivity(toProperties)
                                finish()
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
        private const val FACEBOOK_REQUEST_CODE = 64206

    }

}