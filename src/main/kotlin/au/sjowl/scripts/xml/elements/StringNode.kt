package au.sjowl.scripts.xml.elements

data class StringNode(
    var name: String,
    var value: String,
    val translatable: Boolean
) {
    override fun toString(): String {
        return "<string name=\"$name\"${if (!translatable) " translatable=\"false\"" else ""}>$value</string>"
    }
}