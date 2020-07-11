package au.sjowl.scripts.xml.translate

import au.sjowl.scripts.xml.converter.StringResourceEncoder
import au.sjowl.scripts.xml.elements.PluralsItem
import au.sjowl.scripts.xml.elements.StringArrayItem
import au.sjowl.scripts.xml.elements.StringResource
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlin.random.Random

class ResourcesTranslator(
    englishResource: StringResource
) {
    private val translator = GoogleTranslateUnofficialAPI(Gson())

    private val encoder = StringResourceEncoder(englishResource)
    private val encodedStrings = encoder.encodeStrings()
    private val encodedArrays = encoder.encodeArrays()
    private val encodedPlurals = encoder.encodePlurals()

    suspend fun translate(languageCode: String): StringResource {

        println("LANGUAGE = $languageCode")

        val translatedStrings = translate(encodedStrings, languageCode)
        val decodedStrings = encoder.decodeStrings(translatedStrings)

        val translatedPlurals = translate(encodedPlurals, languageCode)
        val decodedPlurals = encoder.decodePlurals(translatedPlurals)


        val translatedArrays = translate(encodedArrays, languageCode)
        val decodedArrays = encoder.decodeArrays(translatedArrays)

        return StringResource(
            name = "values-${languageCode}",
            strings = decodedStrings,
            arrays = decodedArrays,
            plurals = decodedPlurals
        )
    }

    private suspend fun translate(stringArrayItem: StringArrayItem, languageCode: String): StringArrayItem {
        return stringArrayItem.copy(value = translate(stringArrayItem.value, languageCode))
    }

    private suspend fun translate(pluralsItem: PluralsItem, languageCode: String): PluralsItem {
        return pluralsItem.copy(value = translate(pluralsItem.value, languageCode))
    }

    private suspend fun translate(text: String, languageCode: String): String {
        val delay = Random.nextLong(2000, 10000)
        println("delay for $delay ms")
        delay(delay)
        translator.langFrom = "en"
        translator.langTo = languageCode
        return translator.translate(text)!!
    }
}
