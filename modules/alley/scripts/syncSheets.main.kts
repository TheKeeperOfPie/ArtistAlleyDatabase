import java.net.URL

val secrets = __FILE__.resolve("../../secrets.properties")
val sheetId = secrets.useLines { it.first { it.startsWith("sheetId=") } }.removePrefix("sheetId=")
val assetsFolder = __FILE__.resolve("../../src/main/assets")

downloadUrl("Artists", "artists.csv", "A1:L")
downloadUrl("Series", "series.csv")
downloadUrl("Merch", "merch.csv")

// For some reason Stamp Rallies doesn't download correctly, dev should overwrite manually.
// This is kept around in case optimistically it fixes itself.
downloadUrl("Stamp Rallies", "rallies.csv", "A1:G")

fun downloadUrl(sheetName: String, fileName: String, range: String? = null) {
    val outputFile = assetsFolder.resolve(fileName)
    val url = "https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:csv&sheet=" +
            sheetName.replace(" ", "%20") +
            "&range=$range".takeIf { range != null }.orEmpty()
    println("Writing $url to $outputFile")
    URL(url).openConnection().getInputStream().use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}
