package au.sjowl.scripts.xml.translate

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import kotlin.coroutines.resume

interface ITranslationService {
    suspend fun translate(text: String): String?
    suspend fun translate(list: List<String>): List<String>?
}

class GoogleTranslateUnofficialAPI(
    private val gson: Gson
) : ITranslationService {

    private var resp: String? = null

    var langFrom: String = "en"
    var langTo: String = "ru"

    override suspend fun translate(text: String): String? = suspendCancellableCoroutine { continuation ->

        try {
            val url = formatUrl(langFrom, langTo, text)
            resp = getResponse(url)
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resume(null)
        }

        if (resp != null) {
            try {
                val temp = ((gson.fromJson<List<Any>>(resp, object : TypeToken<List<Any>>() {}.type))[0] as List<Any?>)
                    .map { (it as? List<Any?>)?.get(0) }
                    .filterNotNull()
                    .joinToString("")

                if (temp.length > 2) {
                    continuation.resume(temp)
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun translate(list: List<String>): List<String>? {
        val partsToTranslateSeparately = splitListIntoParts3000(list)
        var translations: MutableList<String>? = null
        partsToTranslateSeparately.forEach { part ->
            val partTranslations = translatePart(part)
            if (translations == null && partTranslations != null) {
                translations = mutableListOf()
            }
            partTranslations?.let { translations?.addAll(it) }
        }
        return translations
    }

    private fun formatUrl(langFrom: String, langTo: String, text: String): String {
        return "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s"
            .format(langFrom, langTo, URLEncoder.encode(text, "UTF-8"))
    }

    private fun getResponse(url: String): String {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            setRequestProperty("User-Agent", "Mozilla/5.1")
        }
            .inputStream
            .bufferedReader()
            .readLines()
            .joinToString("\n")
    }

    private fun splitListIntoParts3000(list: List<String>): MutableList<List<String>> {
        val parts3000 = mutableListOf<List<String>>()
        var latestIndex = 0
        while (latestIndex < list.size) {
            val partList = LinkedList<String>()
            var partSize = 0
            while (partSize < 3000 && latestIndex < list.size) {
                partSize += list[latestIndex].length
                partList.add(list[latestIndex])
                latestIndex++
            }
            parts3000.add(partList)
        }
        return parts3000
    }

    private suspend fun translatePart(part: List<String>): List<String>? {
        val delimiter = "\n"
        val translation = translate(part.joinToString(delimiter))
        return translation?.split(delimiter)?.map { it.trim('\n', ' ', ',') }
    }
}



