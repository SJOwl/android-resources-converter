package au.sjowl.scripts.xml

import au.sjowl.scripts.xml.converter.AndroidStringsConverter
import au.sjowl.scripts.xml.elements.OUTPUT_NAME
import kotlinx.coroutines.runBlocking

object Arguments {
    const val ARG_RESOURCES = "-resources"
    const val ARG_OUTPUT = "-output"
    const val ARG_STRING_FILE = "-string_file"
    const val ARG_COMMAND = "-command"
    const val ARG_LANGUAGES = "-languages"
    const val ARG_HELP = "-h"
}

val argumentKeys = listOf(
        Arguments.ARG_RESOURCES,
        Arguments.ARG_OUTPUT,
        Arguments.ARG_STRING_FILE,
        Arguments.ARG_COMMAND,
        Arguments.ARG_LANGUAGES,
        Arguments.ARG_HELP
)

object Main {
    const val help = """
Arguments:
${Arguments.ARG_COMMAND}      :   command to convert into this format [xml, csv, full]
${Arguments.ARG_RESOURCES}      :   path to res folder
${Arguments.ARG_OUTPUT}      :   output folder
${Arguments.ARG_STRING_FILE}      :   name of string file
${Arguments.ARG_LANGUAGES}      :   languages list, comma-separated 
        """

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        try {

            val argsMap = mutableMapOf<String, String>()
            var i = 0
            while (i < args.size) {
                if (args[i] in argumentKeys) {
                    argsMap[args[i]] = args[i + 1]
                    i++
                }
                i++
            }
            argsMap[Arguments.ARG_LANGUAGES] = (argsMap[Arguments.ARG_LANGUAGES] ?: "de,es,fr,pt,ru")

            val languages = argsMap[Arguments.ARG_LANGUAGES]!!.split(",")

            val debug = false
            if (debug) {
                val pathResources = "/Users/sj/AndroidApps/suby/widgets/src/main/res"
                val pathOutput = "/Users/sj/Downloads"
                convert("csv", pathResources, pathOutput, null, languages)
                convert("xml", pathResources, pathOutput, null, languages)
            } else {
                val command = argsMap[Arguments.ARG_COMMAND] ?: error("not specified command:  use ${Arguments.ARG_COMMAND}")
                val pathResources = argsMap[Arguments.ARG_RESOURCES] ?: error("not specified resources folder: ${Arguments.ARG_RESOURCES}")
                val pathOutput = argsMap[Arguments.ARG_OUTPUT] ?: error("not specified output folder: ${Arguments.ARG_OUTPUT}")
                val stringFileName = argsMap[Arguments.ARG_STRING_FILE]
                when (command) {
                    "csv", "xml" -> convert(command, pathResources, pathOutput, stringFileName, languages)
                    "full" -> {
                        convert("csv", pathResources, pathOutput, stringFileName, languages)
                        convert("xml", pathResources, pathOutput, stringFileName, languages)
                    }
                    "translate" -> translateReleaseNotes()
                    else -> println("select options")
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(help)
            println("\n\nError:")
            println(e)
        }
    }

    private fun translateReleaseNotes() {
    }

    private suspend fun convert(convertTo: String, pathResources: String, pathOutput: String, stringsFileName: String?, languages: List<String>) {
        val converter = AndroidStringsConverter(
                pathResources = pathResources,
                pathOutput = pathOutput,
                stringsFileName = stringsFileName ?: "strings.xml",
                languages = languages
        )
        when (convertTo) {
            "csv" -> {
                converter.convertToCsv()
                println("files converted to csv at ${pathOutput}/$OUTPUT_NAME")
            }
            "xml" -> {
                converter.convertToXml()
                println("files converted to xml at ${pathResources}")
            }
        }

        println("\n\nSuccess!")
    }

}