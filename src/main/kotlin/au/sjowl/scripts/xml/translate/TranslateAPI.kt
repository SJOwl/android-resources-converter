package au.sjowl.scripts.xml.translate

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oracle.javafx.jmx.json.JSONException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume

class TranslateAPI {

    private var resp: String? = null

    private val gson = Gson()

    private fun formatUrl(langFrom: String, langTo: String, text: String): String {
        return "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$langFrom&tl=$langTo&dt=t&q=${URLEncoder.encode(text, "UTF-8")}"
    }

    private fun getResponse(url: String): String {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            setRequestProperty("User-Agent", "Mozilla/5.1")
        }
                .inputStream
                .bufferedReader()
                .readLines()
                .joinToString()
    }

    suspend fun translate(text: String, langFrom: String, langTo: String): String = suspendCancellableCoroutine { continuation ->

        try {
            val url = formatUrl(langFrom, langTo, text)
            resp = getResponse(url)
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resumeWith(Result.failure(IOException()))
        }

        if (resp != null) {
            try {
                println("translate $text")
                val temp = ((gson.fromJson<List<Any>>(resp, object : TypeToken<List<Any>>() {}.type))[0] as List<Any?>)
                        .map { (it as? List<Any?>)?.get(0) }
                        .filterNotNull()
                        .joinToString()

                if (temp.length > 2) {
                    continuation.resume(temp)
                } else {
                    continuation.resumeWith(Result.failure(Exception("Invalid Input String")))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                continuation.resume("")
            }
        }
    }
}


