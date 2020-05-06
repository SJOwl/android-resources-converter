package au.sjowl.scripts.xml

import au.sjowl.scripts.xml.elements.PluralsItem
import au.sjowl.scripts.xml.elements.PluralsNode
import au.sjowl.scripts.xml.elements.StringArrayItem
import au.sjowl.scripts.xml.elements.StringArrayNode
import au.sjowl.scripts.xml.elements.StringNode
import au.sjowl.scripts.xml.elements.StringResource
import au.sjowl.scripts.xml.translate.TranslateAPI
import kotlinx.coroutines.delay
import kotlin.random.Random


class StringResourceEncoder(
    resource: StringResource
) {

    private val translatables: List<StringNode> = resource.strings.values.filter { it.translatable }.sortedBy { it.name }
    private val nontranslatables = resource.strings.values.filter { !it.translatable }.sortedBy { it.name }
    private val arrays = resource.arrays.values.sortedBy { it.name }
    private val plurals = resource.plurals.values.sortedBy { it.name }


    private fun String.replaceAll() = trim().replace("color = ", "color=")
            .replace("# ", "#")
            .replace("'", "\\'")
            .replace("\\ \\'", "\\\\'")
            .replace("\\\\'", "\\'")
            .replace("% ", "%")
            .replace("! [CDATA [", "![CDATA[")
            .replace(" \\'>", "\\'>")
            .replace("…", "...")
            .replace("</ ", "</")
            .replace("Https", "https")
            .replace("""\ n""", "\n")
            .replace("""\ N""", "\n")
            .replace(" , ", " ")
            .replace(" , ", "")
            .replace(">]>", ">]]>")
            .replace("^, ".toRegex(), "")
            .replace(" ?\\^\\^\\^ ?".toRegex(), "")
            .replace("^, ".toRegex(), "")
            .replace(Regex("> *"), ">")
            .replace(Regex(">"), "> ")
            .replace(Regex(">"), "> ")
            .replace(Regex("> *<"), "><")
            .replace(Regex(" *<"), "<")
            .replace(" {2,}".toRegex(), " ")
            .capitalize()

    private fun String.trim() = trim(',', ' ')

    /** strings */
    fun encodeStrings(): String {
        return translatables.map { it.value }.joinToString(DELIMITER_LINES_ENCODE)
    }

    fun decodeStrings(string: String): MutableMap<String, StringNode> {

        println("lines size must me ${translatables.size}")

        val partStrings = string.split(DELIMITER_LINES_DECODE).trim()
        val strings = mutableListOf<StringNode>().apply {
            addAll(nontranslatables.map { it.copy(value = it.value.clean()) })
            addAll(translatables.mapIndexed { index, stringNode -> stringNode.copy(value = partStrings[index].replaceAll()) })
        }
        return strings.toMutableMap { it.name }
    }

    fun encodeArrays(): String {
        return arrays.map { it.items.joinToString(DELIMITER_LINES_ENCODE) { it.value } }.joinToString(DELIMITER_ENCODE)
    }

    fun decodeArrays(string: String): MutableMap<String, StringArrayNode> {
        val partArrays = string.split(DELIMITER_DECODE).trim()

        val arrays = arrays.mapIndexed { index, stringArrayNode ->
            val items = partArrays[index].split(DELIMITER_LINES_DECODE)

            stringArrayNode.copy(
                items = stringArrayNode.items
                        .mapIndexed { index2, stringArrayItem ->
                            stringArrayItem.copy(value = items[index2].replaceAll())
                        }.toMutableList())
        }
        return arrays.toMutableMap { it.name }
    }

    fun encodePlurals(): String {
        return plurals.map { it.items.joinToString(DELIMITER_LINES_ENCODE) { it.value } }.joinToString(DELIMITER_ENCODE)
    }

    fun decodePlurals(string: String): MutableMap<String, PluralsNode> {
        val partPlurals = string.split(DELIMITER_DECODE).trim()
        val plurals = plurals.mapIndexed { index, pluralsNode ->
            val items = partPlurals[index].split(DELIMITER_LINES_DECODE)
            pluralsNode.copy(
                items = pluralsNode.items
                        .mapIndexed { index2, pluralsItem ->
                            pluralsItem.copy(value = items[index2].replaceAll())
                        }.toMutableList())
        }
        return plurals.toMutableMap { it.name }
    }

    private fun List<String>.trim() = filter { it.trim().isNotEmpty() }
            .map { it.trim() }

    fun <K, V> List<V>.toMutableMap(key: (item: V) -> K): MutableMap<K, V> {
        val map = mutableMapOf<K, V>()
        forEach {
            map[key(it)] = it
        }
        return map
    }

    private val DELIMITER_LINES_ENCODE = "^^^\n"
    private val DELIMITER_LINES_DECODE = Regex("(\\^\\^\\^\n|\n,)")

    private val DELIMITER_ENCODE = " @@\n\n "
    private val DELIMITER_DECODE = DELIMITER_ENCODE.trim()
}

class ResourcesTranslator(
    englishResource: StringResource
) {
    private val translator = TranslateAPI()

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

    private suspend fun translate(stringArrayNode: StringArrayNode, languageCode: String): StringArrayNode {
        return stringArrayNode.copy(items = stringArrayNode.items.map { translate(it, languageCode) }.toMutableList())
    }

    private suspend fun translate(stringArrayItem: StringArrayItem, languageCode: String): StringArrayItem {
        return stringArrayItem.copy(value = translate(stringArrayItem.value, languageCode))
    }

    private suspend fun translate(pluralsNode: PluralsNode, languageCode: String): PluralsNode {
        return pluralsNode.copy(items = pluralsNode.items.map { translate(it, languageCode) }.toMutableList())
    }

    private suspend fun translate(pluralsItem: PluralsItem, languageCode: String): PluralsItem {
        return pluralsItem.copy(value = translate(pluralsItem.value, languageCode))
    }

    private suspend fun translate(stringNode: StringNode, languageCode: String): StringNode {
        return when {
            !stringNode.translatable -> stringNode
            else -> stringNode.copy(value = translate(stringNode.value, languageCode))
        }
    }

    private suspend fun translate(text: String, languageCode: String): String {
        val delay = Random.nextLong(2000, 10000)
        println("delay for $delay ms")
        delay(delay)
        val translated = translator.translate(text, "en", languageCode)
        //                println("$languageCode translate '$text' -> '$translated'")
        return translated
    }
}

private val encodedRu = """Application version !! Apply !! Suby Audio Extractor !! Extracting audio for sentences … !! Extracting audio for sentences … !! Autogenerated subtitles !! Auto skip is off !! Auto skip activated !! Cancel !! Back !! Cover image !! Select subtitles !! Share !! Skip to next sentence !! Open on YouTube !! Clear cache !! Clear YouTube videos !! Contact us !! Convert !! Copied to clipboard !! Default Translator for sentences !! Default Translator for words !! <![CDATA[Your dictionary is<b><font color =\'#fb8e53\'>   empty</font></b>   ]]> !! Did you not find your video? !! Done !! Download subtitles !! Download subtitles from !! Download videos to see them here. !! No favorite videos !! Open any video from the Explore tab to see it here. !! No watched videos !! No saved playlists !! Subscribe to channels to see them here !! No subscriptions yet !! Suby video player !! Suby player !! Long press the words in the player to save them here !! Failed to parse URL !! FAQ !! File deleted from the device. !! Files !! Channels !! Playlists !! Video !! Folder deleted !! Unsupported video files found. !! Got it !! Grant permissions !! Help us make Suby better. !! Tap on a sentence to translate it. Long tap on a word in a sentence to translate just a word !! What do you think about Suby? !! Press on Repeat icon to switch sentences repeat mode. Long click on ita to change base repeat count !! Search !! Search on device !! Subtitle Search !! Search on YouTube !! Auto skip to next sentence !! Long press on one word in subtitles to translate it !! Tap on a sentence while playing to start play from it !! Click on the Heart icon from the player to see videos here. !! Add playlist to favorites to see it here !! Non-existent email !! Install !! Suby integrates Google Translate. We highly recommend to install it for a richer experience !! Language settings !! Later !! Add to favorites !! Local video files !! Scanning for video files … !! Suby Media scanner !! Minutes !! More !! A new video is uploaded! !! New videos !! Next !! Night mode !! System !! Day !! Night !! No !! Check your network connection. !! You are offline !! No free space available !! No Internet connection !! No matches !! No storage permissions !! To watch videos on your device. !! No subtitles found !! No video found !! No results found !! No videos with %s subtitles that match your query.\nTry different keywords !! Suby audio player !! Words !! OK !! Open dictionary !! Open Suby on Google Play !! Open on YouTube !! Other !! Play In background !! Playback speed !! Normal !! Player settings !! Privacy Policy !! <![CDATA[<b>   Total sentences:</b>   %d]]> !! <![CDATA[<b>   New words:</b>   %d]]> !! Rate Suby !! Rate now !! Rescan downloads !! Show tutorial !! Saved to Dictionary !! Clear !! No results. Open OpenSubtitles.org !! Search is empty !! Video search !! Select a folder to scan !! Please, select an SRT file. !! Send email … !! Send feedback !! Send %s !! Sentence deleted !! Server error !! The delay between sentences, ms !! Profile !! Learn a language: !! Native language: !! Minimum subtitle length to repeat !! Repeat a sentence, times !! Scan completed !! Restore Defaults !! Rewind after resuming, ms !! Show captions !! Show files with subtitles only !! Show hints !! Subtitle Text Height !! Share app link !! Share your experience !! Show channel !! Sign in !! Sign out !! Skip !! Subtitles are from OpenSubtitles.org !! Subscribe !! No, thanks !! Take a survey !! What can we do better? !! Dictionary !! Explore !! Favorites !! Local !! Watched !! Home !! Playlists !! Subscriptions !! Terms and Conditions !! Thanks for your feedback! !! Dictionary !! Gallery !! Player !! Progress !! Sentences !! Settings !! Top 1000 !! Words !! To close the application, click \"Back\" again. !! Translate !! Which of them do you want to use for translation? !! The text is copied to the clipboard. !! Open the Watched tab to see your watched videos. !! Open YouTube video !! Discover videos on YouTube !! Look for YouTube channels. !! Subscribe to see it in the Subscriptions tab !! Explore your favorite channels !! Change repeat sentence mode:\n\nClick to turn repetitions on or off\n\nLong click to select repetitions count !! Click the Translate button to translate a sentence !! Long click on any word to translate it !! Undo !! We do our best but sometimes miss some bugs. !! Something unexpected happened !! Unknown error !! Unsubscribe !! The player does not support this video format. !! Usage statistics !! This video format is not supported.\nDo you wish to copy it as mp4?\nIt can take several minutes. !! This video has no subtitles. !! Video removed !! Visit website !! <![CDATA[I want to<b><font color =\'#fb8e53\'>   learn</font></b>   …]]> !! Web !! <![CDATA[Which<b><font color =\'#fb8e53\'>   language</font></b>    do you<b><font color =\'#fb8e53\'>   speak</font></b>   ?]]> !! Word deleted !! Delay between words !! New words !! Words player !! YouTube rating !!  ||  !! Select a subtitle file from your device or download it directly from the application ## Make the player repeat each phrase so you can understand the meaning. ## Click on unknown words to save them in the dictionary, or translate. ## All words and sentences are saved in your dictionary so you can repeat them. !! Read subtitles ## Repeat phrases ## Translate ## Repeat later !!  ||  !! %d episode ## %d episodes ## %d episodes ## %d episodes"""