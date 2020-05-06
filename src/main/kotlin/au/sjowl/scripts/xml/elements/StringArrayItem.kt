package au.sjowl.scripts.xml.elements

data class StringArrayItem(
    val value: String
) {
    override fun toString(): String {
        return "\t\t<item>$value</item>\n"
    }
}