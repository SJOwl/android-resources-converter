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
        when (args[0]) {
            "csv" -> {
                AndroidStringsConverter(
                    pathResources = args[1],
                    pathOutput = args[2]
                ).convertToCsv()
                println("files converted to csv at ${args[2]}/strings.xml")
            }
            "xml" -> {
                AndroidStringsConverter(
                    pathResources = args[1],
                    pathOutput = args[2]
                ).convertToXml()
                println("files converted to xml at ${args[1]}")
            }
        }
    } catch (e: Exception) {
        println("\n\nError:")
        println(e)
    }
}
