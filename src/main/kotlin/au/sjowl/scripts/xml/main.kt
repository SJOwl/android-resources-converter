package au.sjowl.scripts.xml

fun main(args: Array<String>) {
    try {
        println("""
Supported arguments are: xml and csv
first argument - resources foulder, second - output folder
Example:
cr csv /Users/sj/AndroidApps/SubtitlesPlayer/widgets/src/main/res /Users/sj/Downloads
cr xml /Users/sj/AndroidApps/SubtitlesPlayer/widgets/src/main/res /Users/sj/Downloads
        """.trimIndent())
        val convertTo = args[0]
        val pathResources = args[1]
        val pathOutput = args[2]
        convert(convertTo, pathResources, pathOutput)

        /*
                val convertTo = "xml"
                val pathResources = "/Users/sj/AndroidApps/SubtitlesPlayer/widgets/src/main/res"
                val pathOutput = "/Users/sj/Downloads"
                convert("csv", pathResources, pathOutput)
                convert("xml", pathResources, pathOutput)
                */
    } catch (e: Exception) {
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
            println("files converted to csv at ${pathOutput}/strings.tsv")
        }
        "xml" -> {
            converter.convertToXml()
            println("files converted to xml at ${pathResources}")
        }
    }

    println("\n\nSuccess!")
}
