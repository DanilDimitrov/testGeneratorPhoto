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
            .url("http://185.174.100.215:9101/translate")
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

    suspend fun getAllCollections(): ArrayList<Model> = withContext(Dispatchers.IO) {
        val allModels = ArrayList<Model>()
        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("AItobo")

        try {
            val documents = collectionReference.get().await()

            for (document in documents) {
                val data = document.data
                val strength = data["strength"].toString().toDouble()
                val modelId = data["modelId"].toString()
                val count = data["count"].toString().toByte()
                val styleName = data["styleName"].toString()
                val prompt = data["prompt"].toString()
                val steps = data["steps"].toString().toByte()
                val negativePrompt = data["negativePrompt"].toString()
                val controlModel = data["controlModel"].toString()
                val lora = data["lora"].toString()



                val model = Model(R.drawable.dog, styleName, count, modelId, strength, prompt, negativePrompt, controlModel, steps, lora)
                allModels.add(model)
            }
        } catch (exception: Exception) {
            Log.i("ERROR", "No DATA")
            // В случае ошибки, возвращаем пустой список
        }

        allModels
    }



}

