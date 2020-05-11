package au.sjowl.scripts.xml.converter

import au.sjowl.scripts.xml.elements.PluralsNode
import au.sjowl.scripts.xml.elements.StringArrayNode
import au.sjowl.scripts.xml.elements.StringNode
import au.sjowl.scripts.xml.elements.StringResource

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
            addAll(nontranslatables.map { it.copy(value = it.value.replaceAll()) })
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

    private val DELIMITER_LINES_ENCODE = "\n"
    private val DELIMITER_LINES_DECODE = "\n"

    private val DELIMITER_ENCODE = " @@ "
    private val DELIMITER_DECODE = DELIMITER_ENCODE.trim()
}