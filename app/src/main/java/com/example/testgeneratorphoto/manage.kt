import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.testgeneratorphoto.Model
import com.example.testgeneratorphoto.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject

class Manage {
    suspend fun translator(inputText: String, fromLang: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val jsonRequestBody = """
        {
            "text": "$inputText", 
            "from_lang": "$fromLang"
        }
    """.trimIndent()

        val request = Request.Builder()
            .url("http://216.22.1.106:6700/translate")
            .post(RequestBody.create(jsonMediaType, jsonRequestBody))
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            try {
                val jsonString = response.body?.string()
                val jsonObject = JSONObject(jsonString!!)
                return@withContext jsonObject.getString("translated_text")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("TEXT", "NONE")
                return@withContext ""
            }
        } else {
            Log.i("ERROR", "NONE")

            return@withContext ""
        }
    }
    // Определяем функцию-колбэк для передачи данных

    suspend fun getAllCollections(): ArrayList<ArrayList<Model>> = withContext(Dispatchers.IO) {
        val allModels = ArrayList<ArrayList<Model>>()

        val headersModels = ArrayList<Model>()
        val popularModels = ArrayList<Model>()
        val anotherModels = ArrayList<Model>()


        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("AituboRequest")

        try {
            val documents = collectionReference.get().await()

            for (document in documents) {
                val data = document.data
                val preview = data["gifPath"].toString()
                val strength = data["strength"].toString().toDouble()
                val modelId = data["modelId"].toString()
                val count = data["count"].toString().toByte()
                val styleName = data["styleName"].toString()
                val prompt = data["prompt"].toString()
                val steps = data["steps"].toString().toByte()
                val negativePrompt = data["negativePrompt"].toString()
                val controlModel = data["controlModel"].toString()
                val lora = data["lora"].toString()
                val category = data["category"].toString()

                val model = Model(preview, styleName, count, modelId, strength, prompt, negativePrompt, controlModel, steps, lora, category)

                when (category) {
                    "Header" -> headersModels.add(model)
                    "Popular" -> popularModels.add(model)
                    // Добавьте обработку других категорий, если необходимо
                    else -> anotherModels.add(model)
                }
            }
        } catch (exception: Exception) {
            Log.i("ERROR", "No DATA")
            // В случае ошибки, возвращаем пустой список
        }

        allModels.add(headersModels)
        allModels.add(popularModels)
        allModels.add(anotherModels)
Log.i("ARRAYS", allModels.toString())
        // Добавьте другие списки моделей в массив allModels, если необходимо

        allModels
    }

    fun resizeImage(width: Int, height: Int, afterWidth: Int): Int {
        Log.i("height width", "$width $height")

        if (width != 0) { // Защита от деления на ноль
            val kof = (height.toFloat() / width.toFloat())
            Log.i("kof", kof.toString())
            val afterHeight = (kof * afterWidth).toInt()
            Log.i("afterHeight", afterHeight.toString())
            return afterHeight
        }

        return 0 // Вернуть значение по умолчанию или другое значение, если width == 0
    }

    fun getImageSize(context: Context, imagePath: String): Pair<Int, Int>? {
        try {
            val uri = Uri.parse(imagePath)
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                val width = options.outWidth
                val height = options.outHeight
                inputStream.close()
                return Pair(width, height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


}

