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
    private val htmlFileName = "$pathOutput/strings.html"

    /**
    val translateAPI = TranslateAPI()
    val t = measureTimeMillis {
    val translatedText = translateAPI.translate("кривой", Language.AUTO_DETECT, Language.ENGLISH)
    println("onSuccess: $translatedText")
    }
    println("translation: $t ms")
     */

    suspend fun convertToCsv() {
        val resourcesMap: MutableMap<String, StringResource> = parseToResourcesMap()
        translate(resourcesMap)

        val csvMerged = ObjectsMerger().merge(resourcesMap, pathCsv)
        File(pathCsv).writeText(csvMerged)
    }

    private suspend fun translate(resources: MutableMap<String, StringResource>) {

        val prefix = "values-"

        val names = resources.keys

        val translator = ResourcesTranslator(resources["values"]!!)

        names.forEach { name: String ->
            val code = name.replace(prefix, "")
            if (code != "values") {
                resources[name] = translator.translate(code)
            }
        }
    }

    private fun parseToResourcesMap(): MutableMap<String, StringResource> {

        val parser = XmlParser()
        val resourcesMap = mutableMapOf<String, StringResource>()

        val htmlFile = File(htmlFileName)
        htmlFile.delete()
        htmlFile.createNewFile()

        File(pathResources).children()
                .filter { file: File -> file.name == stringsFileName }
                .map {
                    val stringResource = parser.parseToCsv(it)
                    htmlFile.appendText(stringResource.getCDATA())
                    stringResource
                }.forEach { stringResource: StringResource ->
                    resourcesMap[stringResource.name] = stringResource
                }

        return resourcesMap
    }

    fun convertToXml() {

        XmlParser().parseCsvToXml(File(pathCsv))
                .forEach { stringResource: StringResource ->
                    val folder = File(pathResources, stringResource.name).apply {
                        mkdirs()
                    }
                    File(folder, stringsFileName).writeText(stringResource.toString())
                }
    }

    private fun File.children(): Sequence<File> {
        return walkTopDown()
                .filter { this.path != it.path }
    }
}