package au.sjowl.scripts.xml.elements

class StringResource(
    val name: String,/* can be values-ru-rRU*/
    val strings: MutableMap<String, StringNode> = mutableMapOf(),
    val plurals: MutableMap<String, PluralsNode> = mutableMapOf(),
    val arrays: MutableMap<String, StringArrayNode> = mutableMapOf()
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n")

        val translatable = strings.values.filter { !it.translatable }.sortedBy { it.name }
        if (name == MAIN_VALUES) {
            sb.append(translatable.joinToString("\n") { "\t$it" })
            sb.append("\n")
            sb.append("\n")
        }

        sb.append(strings.values.filter { it.translatable }.sortedBy { it.name }.sortedBy { it.translatable }.joinToString("\n") { "\t$it" })
        sb.append("\n\n")
        sb.append(plurals.values.sortedBy { it.name }.joinToString("\n") { "\t$it" })
        sb.append("\n")
        sb.append(arrays.values.sortedBy { it.name }.joinToString("\n") { "\t$it" })
        sb.append("\n")
        sb.append("</resources>")
        return sb.toString()
    }
}