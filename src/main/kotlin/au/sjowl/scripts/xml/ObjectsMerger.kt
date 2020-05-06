package au.sjowl.scripts.xml

import au.sjowl.scripts.xml.elements.DELIMETER
import au.sjowl.scripts.xml.elements.MAIN_VALUES
import au.sjowl.scripts.xml.elements.PLURALS
import au.sjowl.scripts.xml.elements.PluralsNode
import au.sjowl.scripts.xml.elements.STRING
import au.sjowl.scripts.xml.elements.STRING_ARRAY
import au.sjowl.scripts.xml.elements.StringArrayNode
import au.sjowl.scripts.xml.elements.StringNode
import au.sjowl.scripts.xml.elements.StringResource
import java.io.File

class ObjectsMerger {

    fun merge(resources: Map<String, StringResource>, pathCsv: String): String {
        val sb = StringBuilder()
        val resourceNames = resources.keys.sorted()
        sb.append("type${DELIMETER}id${DELIMETER}translatable$DELIMETER${resourceNames.joinToString(DELIMETER)}") // header
        sb.append("\n")

        resources[MAIN_VALUES]!!.strings.values
            .toList()
            .sortedBy { it.value }
            .sortedBy { it.translatable }
            .sortedBy { stringNode ->
                var res = true
                resourceNames.forEach { name ->
                    res = res && !resources[name]!!.strings[stringNode.name]?.value.isNullOrEmpty()
                }
                res
            }
            .forEach { stringNode: StringNode ->
                sb.append(STRING)
                sb.appendDelimeter()
                sb.append(stringNode.name)
                sb.appendDelimeter()
                sb.append(stringNode.translatable)
                resourceNames.forEach { name ->
                    sb.appendDelimeter()
                    val string = resources[name]!!.strings[stringNode.name]?.value
                    if (string != null) {
                        sb.append(string)
                    }
                }
                sb.append("\n")
            }


        resources[MAIN_VALUES]!!.plurals.values.toList()
            .sortedBy { it.name }
            .forEach { pluralsNode: PluralsNode ->
                sb.append(PLURALS)
                sb.appendDelimeter()
                sb.append(pluralsNode.name)
                sb.appendDelimeter(2)

                sb.append("\n")
                for (i in 0 until pluralsNode.items.size) {

                    sb.appendDelimeter(2)
                    sb.append(pluralsNode.items[i].quantity)
                    resourceNames.forEach { name ->
                        sb.appendDelimeter()
                        try {
                            val pluralItem = resources[name]!!.plurals[pluralsNode.name]!!.items[i]
                            sb.append(pluralItem.value)
                        } catch (e: Exception) {
                        }
                    }
                    sb.append("\n")
                }
            }

        resources[MAIN_VALUES]!!.arrays.values.toList()
            .sortedBy { it.name }
            .forEach { stringArraysNode: StringArrayNode ->
                sb.append(STRING_ARRAY)
                sb.appendDelimeter()
                sb.append(stringArraysNode.name)
                sb.appendDelimeter(2)

                sb.append("\n")
                for (i in 0 until stringArraysNode.items.size) {

                    sb.appendDelimeter(2)
                    resourceNames.forEach { name ->
                        sb.appendDelimeter()
                        try {
                            val array = resources[name]!!.arrays[stringArraysNode.name]!!.items[i]
                            sb.append(array.value)
                        } catch (e: Exception) {
                        }
                    }
                    sb.append("\n")
                }
            }

        File(pathCsv).writeText(sb.toString())

        return sb.toString()
    }

    private fun StringBuilder.appendDelimeter(times: Int = 1) {
        repeat(times) { append(DELIMETER) }
    }
}