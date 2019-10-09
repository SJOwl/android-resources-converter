package au.sjowl.scripts.xml.elements

class PluralsNode(
    val name: String,
    val items: MutableList<PluralsItem>
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("<plurals name=\"$name\">\n")
        for (item in items) {
            sb.append(item.toString())
        }
        sb.append("\t</plurals>\n")
        return sb.toString()
    }
}