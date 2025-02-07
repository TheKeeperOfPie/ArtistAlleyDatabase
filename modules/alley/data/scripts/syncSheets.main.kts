import java.net.URL
import java.io.File

val secrets = __FILE__.resolve("../../../secrets.properties")
val sheetId = secrets.useLines { it.first { it.startsWith("sheetId=") } }.removePrefix("sheetId=")
val inputsFolder = __FILE__.resolve("../../inputs/")
val artistsOutputFile = inputsFolder.resolve("2025/artists.csv")
val seriesOutputFile = inputsFolder.resolve("series.csv")
val merchOutputFile = inputsFolder.resolve("merch.csv")

downloadUrl("Artists", artistsOutputFile, "A1:Q")
downloadUrl("Series", seriesOutputFile)
downloadUrl("Merch", merchOutputFile)

// For some reason Stamp Rallies doesn't download correctly, dev should overwrite manually.
// This is kept around in case optimistically it fixes itself.
//downloadUrl("Stamp Rallies", "rallies.csv", "A1:G")

fun downloadUrl(sheetName: String, outputFile: File, range: String? = null) {
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
