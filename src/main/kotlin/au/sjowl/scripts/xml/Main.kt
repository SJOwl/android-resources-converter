package au.sjowl.scripts.xml

import au.sjowl.scripts.xml.converter.AndroidStringsConverter
import au.sjowl.scripts.xml.elements.OUTPUT_NAME
import kotlinx.coroutines.runBlocking

object Main {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        try {
            val help = """
        Supported arguments are: xml and csv, translate
        first argument - resources folder, second - output folder
        Example:
        cr csv /Users/sj/AndroidApps/suby/widgets/src/main/res /Users/sj/Downloads
        cr xml /Users/sj/AndroidApps/suby/widgets/src/main/res /Users/sj/Downloads""".trimMargin()

            println(help)

            val debug = false
            if (debug) {
                val pathResources = "/Users/sj/AndroidApps/suby/widgets/src/main/res"
                val pathOutput = "/Users/sj/Downloads"
                convert("csv", pathResources, pathOutput)
                convert("xml", pathResources, pathOutput)
            } else {
                val command = args[0]
                val pathResources = args[1]
                val pathOutput = args[2]
                when (command) {
                    "csv", "xml" -> convert(command, pathResources, pathOutput)
                    "full" -> {
                        convert("csv", pathResources, pathOutput)
                        convert("xml", pathResources, pathOutput)
                    }
                    "translate" -> translateReleaseNotes()
                    else -> println("select options")
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("\n\nError:")
            println(e)
        }
    }

    private fun translateReleaseNotes() {
    }

    private suspend fun convert(convertTo: String, pathResources: String, pathOutput: String) {
        val converter = AndroidStringsConverter(
            pathResources = pathResources,
            pathOutput = pathOutput
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