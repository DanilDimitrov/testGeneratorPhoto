package com.example.testgeneratorphoto

import android.Manifest
import android.app.Activity
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testgeneratorphoto.databinding.ActivityAiSelfiiesBinding
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
    private var currentPhotoPath: String? = null // Добавьте это как переменную класса
    // Функция для создания файла изображения
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

    // Функция для сохранения изображения в файл



    // Функция для запуска камеры
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
    }
    // Обработка результата из камеры
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?

            val photoFile: File = createImageFile()
            photoFile.let {

                val imageUri = Uri.fromFile(it) // Получение URI файла

                val image: InputImage = InputImage.fromFilePath(this, imageUri)

                val faceDetector: FaceDetector = FaceDetection.getClient()

                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {

                            // Передача URI в другую активити
                            val toResult = Intent(this, resultAiSelfie::class.java)
                            toResult.putExtra("imageUri", imageUri.toString())
                            startActivity(toResult)
                            finish()
                        }else{
                            Toast.makeText(this, "Change Photo", Toast.LENGTH_SHORT).show()
                            dispatchTakePictureIntent()
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


}