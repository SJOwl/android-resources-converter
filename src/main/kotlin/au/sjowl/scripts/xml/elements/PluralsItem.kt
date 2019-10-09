package au.sjowl.scripts.xml.elements

class PluralsItem(
    val quantity: String,
    val value: String
) {
    override fun toString(): String {
        return "\t\t<item quantity=\"$quantity\">$value</item>\n"
    }
}