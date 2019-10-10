package au.sjowl.scripts.xml

import au.sjowl.scripts.xml.elements.OUTPUT_NAME
import au.sjowl.scripts.xml.elements.StringResource
import java.io.File

class AndroidStringsConverter(
    private val pathResources: String,
    pathOutput: String
) {
    private val pathCsv = "$pathOutput/$OUTPUT_NAME"
    private val stringsFileName = "strings.xml"

    fun convertToCsv() {
        val parser = XmlParser()
        val resourcesMap = mutableMapOf<String, StringResource>()
        File(pathResources).children()
            .filter { file: File -> file.name == stringsFileName }
            .map {
                parser.parseToCsv(it)
            }.forEach { stringResource: StringResource ->
                resourcesMap[stringResource.name] = stringResource
            }

        val merger = ObjectsMerger()
        val csvMerged = merger.merge(resourcesMap, pathCsv)
        File(pathCsv).writeText(csvMerged)
    }

    fun convertToXml() {

        XmlParser().parseCsvToXml(File(pathCsv))
            .forEach { stringResource: StringResource ->
                val folder = File(pathResources, stringResource.name).apply {
                    mkdirs()
                }
                File(folder, stringsFileName).writeText(stringResource.toString())
                stringResource.printCDATA()
            }
    }

    private fun File.children(): Sequence<File> {
        return walkTopDown()
            .filter { this.path != it.path }
    }
}