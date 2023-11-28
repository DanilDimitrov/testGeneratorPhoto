package com.example.testgeneratorphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.testgeneratorphoto.databinding.ActivityLoginPageBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class loginPage : AppCompatActivity() {
    lateinit var bind: ActivityLoginPageBinding
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("TAG", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = Firebase.auth

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

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                val account = task.getResult(ApiException::class.java)
                if (account != null ){
                    firebaseAuthwithGoogle(account.idToken!!)

                }
            } catch(e: ApiException){}

        }

        bind.imageButton19.setOnClickListener {
            val tologinEmail = Intent(this, login::class.java)
            startActivity(tologinEmail)
        }

        bind.imageButton13.setOnClickListener {
            onBackPressed()
        }
        bind.textView57.setOnClickListener {
            val toSignUp = Intent(this, signUp::class.java)
            startActivity(toSignUp)
            finish()
        }

        //GOOGLE
        bind.imageButton17.setOnClickListener {
                signInWithGoogle()
        }

        bind.imageButton18.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this, listOf("email", "public_profile")
            )
        }

    }
    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("155712490881-lfdc04hq7r9qcslt5661e29265t34o5r.apps.googleusercontent.com")
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle(){
        val signInClient = getClient()
        signInClient.signOut().addOnCompleteListener {
            val signInIntent = signInClient.signInIntent
            signInIntent.putExtra("forcePrompt", true)
            launcher.launch(signInClient.signInIntent)
        }


    }

    private fun firebaseAuthwithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = auth.currentUser
                val uid = user?.uid // Получение UID пользователя

                // Проверка наличия UID в базе данных
                if (uid != null) {
                    val db = FirebaseFirestore.getInstance()
                    val usersCollection = db.collection("Users")
                    usersCollection.document(uid).get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Пользователь уже зарегистрирован в вашем приложении, выполнить вход
                            Log.d("auth", "User already registered")
                            // Здесь можно выполнить вход в аккаунт
                            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                            val toHome = Intent(this, MainActivity::class.java)
                            startActivity(toHome)
                            finish()
                        } else {
                            user.delete().addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Log.d("auth", "User account deleted")
                                    Toast.makeText(this, "Please register this account ", Toast.LENGTH_SHORT).show()

                                    val toSignUp = Intent(this, signUp::class.java)
                                    startActivity(toSignUp)
                                    finish()

                                } else {
                                    Log.e("auth", "Error deleting user account")
                                }
                            }
                        }
                    }
                    Log.d("auth", "Success")
                } else {
                    Log.d("auth", "Error")

                }
            }
        }
    }
}

