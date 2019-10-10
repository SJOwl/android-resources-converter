package au.sjowl.scripts.xml

import au.sjowl.scripts.xml.elements.OUTPUT_NAME

fun main(args: Array<String>) {
    try {
        println("""
Supported arguments are: xml and csv
first argument - resources foulder, second - output folder
Example:
cr csv /Users/sj/AndroidApps/suby/widgets/src/main/res /Users/sj/Downloads
cr xml /Users/sj/AndroidApps/suby/widgets/src/main/res /Users/sj/Downloads
        """.trimIndent())

        val debug = false
        if (debug) {
            val pathResources = "/Users/sj/AndroidApps/suby/widgets/src/main/res"
            val pathOutput = "/Users/sj/Downloads"
            convert("csv", pathResources, pathOutput)
            convert("xml", pathResources, pathOutput)
        } else {
            val convertTo = args[0]
            val pathResources = args[1]
            val pathOutput = args[2]
            convert(convertTo, pathResources, pathOutput)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("\n\nError:")
        println(e)
    }
}

private fun convert(convertTo: String, pathResources: String, pathOutput: String) {
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
