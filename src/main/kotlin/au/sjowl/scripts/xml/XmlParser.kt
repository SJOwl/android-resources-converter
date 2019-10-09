package au.sjowl.scripts.xml

import au.sjowl.scripts.xml.elements.DELIMETER
import au.sjowl.scripts.xml.elements.MAIN_VALUES
import au.sjowl.scripts.xml.elements.PLURALS
import au.sjowl.scripts.xml.elements.PluralsItem
import au.sjowl.scripts.xml.elements.PluralsNode
import au.sjowl.scripts.xml.elements.STRING
import au.sjowl.scripts.xml.elements.STRING_ARRAY
import au.sjowl.scripts.xml.elements.StringArrayItem
import au.sjowl.scripts.xml.elements.StringArrayNode
import au.sjowl.scripts.xml.elements.StringNode
import au.sjowl.scripts.xml.elements.StringResource
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Node.CDATA_SECTION_NODE
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XmlParser {

    private lateinit var xmlDoc: Document

    fun parseToCsv(file: File): StringResource {

        xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).apply {
            documentElement.normalize()
        }

        val strings = parseStrings()
        val stringsMap = mutableMapOf<String, StringNode>().apply {
            strings.forEach { put(it.name, it) }
        }
        val plurals = parsePlurals().sortedBy { it.name }
        val pluralsMap = mutableMapOf<String, PluralsNode>().apply {
            plurals.forEach { put(it.name, it) }
        }
        val stringArrays = parseStringArrays().sortedBy { it.name }
        val arraysMap = mutableMapOf<String, StringArrayNode>().apply {
            stringArrays.forEach { put(it.name, it) }
        }
        return StringResource(
            file.parentFile.name,
            stringsMap,
            pluralsMap,
            arraysMap
        )
    }

    private val offset = 3

    fun parseCsvToXml(csvFile: File): List<StringResource> {

        val resources = mutableListOf<StringResource>()

        val lines = csvFile.readLines()

        lines[0].split(DELIMETER).filter { it.contains(MAIN_VALUES) }.forEach {
            resources.add(StringResource(it))
        }

        var i = 1
        while (i < lines.size) {

            val s = lines[i]
            val entries = s.split(DELIMETER)
            when (entries[0]) {
                STRING -> {
                    for (j in offset until entries.size) {
                        val name = entries[1]
                        resources[j - offset].strings[name] = StringNode(
                            name = name,
                            value = entries[j],
                            translatable = entries[2].toBoolean()
                        )
                    }
                }
                PLURALS -> {
                    val pluralName = entries[1]

                    resources.forEach {
                        it.plurals[pluralName] = PluralsNode(
                            name = pluralName,
                            items = mutableListOf()
                        )
                    }
                    while (i < lines.size - 1) {
                        val entriesPlurals = lines[i + 1].split(DELIMETER)

                        if (entriesPlurals[1].isNotBlank()) break

                        val qiantity = entriesPlurals[2]
                        for (j in offset until entriesPlurals.size) {
                            resources[j - offset].plurals[pluralName]!!.items.add(PluralsItem(
                                quantity = qiantity,
                                value = entriesPlurals[j]
                            ))
                        }
                        i++
                    }
                }
                STRING_ARRAY -> {
                    val arrayName = entries[1]

                    resources.forEach {
                        it.arrays[arrayName] = StringArrayNode(
                            name = arrayName,
                            items = mutableListOf()
                        )
                    }
                    while (i < lines.size - 1) {
                        val entriesStrings = lines[i + 1].split(DELIMETER)

                        if (entriesStrings[1].isNotBlank()) break

                        for (j in offset until entriesStrings.size) {
                            resources[j - offset].arrays[arrayName]!!.items.add(StringArrayItem(
                                value = entriesStrings[j]
                            ))
                        }
                        i++
                    }
                }
                else -> {
                }
            }

            i++
        }

        return resources
    }

    private fun <T> parse(
        xmlDoc: Document, tag: String,
        visitor: ((node: Element, attributes: Map<String, String>) -> T)
    ): List<T> {

        val nodes: NodeList = xmlDoc.getElementsByTagName(tag)

        val objects = arrayListOf<T>()

        for (i in 0 until nodes.length) {
            val node: Node = nodes.item(i)

            if (node.nodeType == Node.ELEMENT_NODE) {

                val elem = node as Element
                val attributes = mutableMapOf<String, String>()
                for (j in 0 until elem.attributes.length) {
                    attributes.putIfAbsent(elem.attributes.item(j).nodeName, elem.attributes.item(j).nodeValue)
                }

                objects.add(visitor(node, attributes))
            }
        }
        return objects
    }

    private fun <T> parse(
        xmlDoc: Node,
        visitor: ((node: Element, attributes: Map<String, String>) -> T)
    ): List<T> {

        val nodes: NodeList = xmlDoc.childNodes

        val objects = arrayListOf<T>()

        for (i in 0 until nodes.length) {
            val node: Node = nodes.item(i)

            if (node.nodeType == Node.ELEMENT_NODE) {

                val elem = node as Element
                val attributes = mutableMapOf<String, String>()
                for (j in 0 until elem.attributes.length) {
                    attributes.putIfAbsent(elem.attributes.item(j).nodeName, elem.attributes.item(j).nodeValue)
                }

                objects.add(visitor(node, attributes))
            }
        }
        return objects
    }

    private fun parseStrings(): List<StringNode> {
        return parse(xmlDoc, STRING) { node, attributes ->

            val text = if (node.childNodes.item(0)?.nodeType == CDATA_SECTION_NODE)
                node.firstChild.toString()
                    .replace("[#cdata-section: ", "<![CDATA[")
                    .replace("]$".toRegex(), "]]>")
            else
                node.textContent.orEmpty()

            StringNode(
                name = attributes["name"].orEmpty(),
                value = text,
                translatable = (attributes["translatable"] ?: "true").toBoolean()
            )
        }
    }

    private fun parsePlurals(): List<PluralsNode> {
        return parse(xmlDoc, PLURALS) { node, attributes ->
            PluralsNode(
                name = attributes["name"].orEmpty(),
                items = parse(node) { itemNode, itemNodeAttributes ->
                    PluralsItem(
                        quantity = itemNodeAttributes["quantity"].orEmpty(),
                        value = itemNode.textContent.orEmpty()
                    )
                }.toMutableList()
            )
        }
    }

    private fun parseStringArrays(): List<StringArrayNode> {
        return parse(xmlDoc, STRING_ARRAY) { node, attributes ->
            StringArrayNode(
                name = attributes["name"].orEmpty(),
                items = parse(node) { itemNode, _ ->
                    StringArrayItem(
                        value = itemNode.textContent.orEmpty()
                    )
                }.toMutableList()
            )
        }
    }
}
