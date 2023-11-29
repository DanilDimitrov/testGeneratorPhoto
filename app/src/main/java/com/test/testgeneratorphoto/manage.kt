import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.test.testgeneratorphoto.Model
import com.test.testgeneratorphoto.artModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject


class Manage {
    var width: Int = 0
    var height: Int = 0

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

    suspend fun getBannedWords(): ArrayList<String> = withContext(Dispatchers.IO) {
        val allBannedWords = ArrayList<String>()

        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("BannedWords")

        try {
            val documents = collectionReference.get().await()

            for (document in documents) {
                val word = document.getString("word")
                word?.let {
                    allBannedWords.add(it)
                }
            }
        } catch (exception: Exception) {
            Log.i("ERROR", "No DATA")
        }

        allBannedWords
    }




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
                val preview = data["categoryImage"].toString()
                val headerImage = data["headerImage"].toString()
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

                val model = Model(preview, headerImage, styleName, count, modelId, strength, prompt, negativePrompt, controlModel, steps, lora, category)

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

    suspend fun getArtModels(): ArrayList<ArrayList<artModel>> = withContext(Dispatchers.IO) {
        val allModels = ArrayList<ArrayList<artModel>>()

        val artModels = ArrayList<artModel>()


        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("GoApiRequest")

        try {
            val documents = collectionReference.get().await()

            for (document in documents) {
                val data = document.data
                val preview = data["artImage"]?.toString()
                val lora_strength = data["loraStrength"]?.toString()?.toByte()
                val modelId = data["modelId"]?.toString()
                val guidance_scale = data["guidanceScale"]?.toString()?.toByte()
                val styleName = data["styleName"]?.toString()
                val prompt = data["prompt"]?.toString()
                val steps = data["steps"]?.toString()?.toByte()
                val negativePrompt = data["negativePrompt"]?.toString()
                val lora_model = data["loraModel"]?.toString()

                val model = artModel(preview, styleName, guidance_scale, lora_model, modelId, prompt, negativePrompt, steps, lora_strength)
                artModels.add(model)


            }
        } catch (exception: Exception) {
            Log.i("ERROR", "No DATA")
            // В случае ошибки, возвращаем пустой список
        }

        allModels.add(artModels)
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

    fun getImageSize(context: Context, imagePath: Uri): Pair<Int, Int>? {
        try {
            val inputStream = context.contentResolver.openInputStream(imagePath)
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

    suspend fun getArtPrompt():ArrayList<String> = withContext(Dispatchers.IO) {

        val artPrompts = ArrayList<String>()


        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("Inspiration")

        try {
            val documents = collectionReference.get().await()

            for (document in documents) {
                val data = document.data
                val prompt = data["prompt"].toString()

                artPrompts.add(prompt)

            }
        } catch (exception: Exception) {
            Log.i("ERROR", "No DATA")
            // В случае ошибки, возвращаем пустой список
        }

        Log.i("ARRAYS", artPrompts.toString())
        // Добавьте другие списки моделей в массив allModels, если необходимо

        artPrompts
    }


}
