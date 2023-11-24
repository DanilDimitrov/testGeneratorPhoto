package com.example.testgeneratorphoto

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testgeneratorphoto.databinding.ActivityAiSelfiiesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class aiSelfiies : AppCompatActivity() {

    lateinit var bind: ActivityAiSelfiiesBinding
    val uiIntarface = UIIntreface()
    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPhotoPath: String? = null
    lateinit var auth: FirebaseAuth

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath // Сохранение пути к файлу
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAiSelfiiesBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = Firebase.auth
        val user = auth.currentUser
        updateUI(user)

        // UI
        bind.reImageAiSelfie.paint?.shader = uiIntarface.textApp(bind.reImageAiSelfie, "ReImage")
        bind.selfi.setOnClickListener{
            val toChangePhoto = Intent(this, changePhoto::class.java)
            startActivity(toChangePhoto)
        }
        bind.aiArt.setOnClickListener{
            val toMainActivity = Intent(this, MainActivity::class.java)
            startActivity(toMainActivity)
        }
        // UI END

        bind.aiSelfies.setOnClickListener{
            val toAiSelfiies = Intent(this, aiSelfiies::class.java)
            startActivity(toAiSelfiies)
        }
        bind.openCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                checkCameraPermission()
            } else {
                dispatchTakePictureIntent()
            }
        }

        bind.button7.setOnClickListener {
            val toPro = Intent(this, pro_screen::class.java)
            startActivity(toPro)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?

            val photoFile: File = createImageFile()
            photoFile.let {

                val outputStream = FileOutputStream(photoFile)
                imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                val imageUri = Uri.fromFile(it)

                val image: InputImage = InputImage.fromBitmap(imageBitmap!!, 0)

                val faceDetector: FaceDetector = FaceDetection.getClient()

                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {

                            val toResult = Intent(this, resultAiSelfie::class.java)
                            toResult.putExtra("imageUri", imageUri.toString())
                            startActivity(toResult)
                            finish()
                        }else{
                            val dialog = Dialog(this)
                            dialog.setContentView(R.layout.alert_change_photo)

                            val Ok = dialog.findViewById<TextView>(R.id.Ok)
                            Ok.setOnClickListener {
                                dispatchTakePictureIntent()
                                dialog.dismiss()
                            }

                            dialog.show()
                        }
                    }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                12
            )

        } else {
            // Permission already granted
            // Your existing logic here
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 12) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                checkCameraPermission()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.i("USER INFORMATION", user?.isAnonymous.toString())
        val uid = user!!.uid
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("Users").document(uid)

        userDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                Log.i("USER INFORMATION", "Document already exists")

                val isUserAccessGallery = documentSnapshot.getBoolean("isUserAccessGallery") ?: false
                val buyCoins = documentSnapshot.get("buyCoins").toString()
                bind.textView16.text = buyCoins

                if(isUserAccessGallery){
                    bind.noPro.visibility = View.INVISIBLE
                    bind.openCamera.isClickable = true
                }else{
                    bind.noPro.visibility = View.VISIBLE
                    bind.openCamera.isClickable = false
                }


            }
            .addOnFailureListener {
                Log.e("USER INFORMATION", "Failed to get document")
            }

    }


}