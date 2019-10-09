package au.sjowl.scripts.xml.elements

class StringArrayNode(
    val name: String,
    val items: MutableList<StringArrayItem>
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("<string-array name=\"$name\">\n")
        for (item in items) {
            sb.append(item.toString())
        }
        sb.append("\t</string-array>\n")
        return sb.toString()
    }
}