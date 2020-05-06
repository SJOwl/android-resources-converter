package au.sjowl.scripts.xml.elements

class StringResource(
    val name: String,/* can be values-ru, values-es, etc.*/
    val strings: MutableMap<String, StringNode> = mutableMapOf(),
    val plurals: MutableMap<String, PluralsNode> = mutableMapOf(),
    val arrays: MutableMap<String, StringArrayNode> = mutableMapOf()
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources xmlns:tools=\"http://schemas.android.com/tools\" tools:ignore=\"TypographyEllipsis, UnusedQuantity\">\n")

        val translatable = strings.values
            .filter { !it.translatable }
            .sortedBy { it.name }

        if (name == MAIN_VALUES) {
            sb.append("\t<!-- Non-translatable -->\n")
            sb.append(translatable
                .joinToString("\n") { "\t$it" })
            sb.append("\n")
            sb.append("\n")
        }

        sb.append("\t<!-- Translatable -->\n")

        val translatables = strings.values
            .filter { it.translatable }
            .sortedBy { it.name }

        sb.append(translatables
            .filter { it.value.contains("CDATA", ignoreCase = false) }
            .joinToString("\n") { "\t$it" })

        sb.append("\n\n")

        sb.append(translatables
            .filter { !it.value.contains("CDATA", ignoreCase = false) }
            .joinToString("\n") { "\t$it" })

        if (plurals.isNotEmpty()) {
            sb.append("\n\n\t<!-- Plurals -->\n")
            sb.append(plurals.values
                .sortedBy { it.name }
                .joinToString("\n") { "\t$it" })
        }

        if (arrays.isNotEmpty()) {
            sb.append("\n\t<!-- Arrays -->\n")
            sb.append(arrays.values
                .sortedBy { it.name }
                .joinToString("\n") { "\t$it" })
        }

        sb.append("\n")
        sb.append("</resources>")
        return sb.toString()
    }

    fun getCDATA(): String {
        val sb = StringBuilder()
        val cdatas = strings.values
            .filter { it.translatable }
            .filter { it.value.contains("CDATA", ignoreCase = false) }
            .sortedBy { it.name }

        sb.append(name)
        sb.append("\n<br>")

        cdatas.forEach {
            sb.append(it.value
                .replace("<![CDATA[", "")
                .replace("]]>", "<br>")
            )
            sb.append("\n")
        }

        sb.append("\n<br><br>")
        return sb.toString()
    }
}