import java.net.URL
import java.io.File

val secrets = __FILE__.resolve("../../../secrets.properties")
val sheetId2025 = secrets.useLines { it.first { it.startsWith("sheetId=") } }.removePrefix("sheetId=")
val sheetId2024 = secrets.useLines { it.first { it.startsWith("sheetId2024=") } }.removePrefix("sheetId2024=")
val sheetId2023 = secrets.useLines { it.first { it.startsWith("sheetId2023=") } }.removePrefix("sheetId2023=")
val inputsFolder = __FILE__.resolve("../../inputs/")
val artists2025OutputFile = inputsFolder.resolve("2025/artists.csv")
val artists2024OutputFile = inputsFolder.resolve("2024/artists.csv")
val artists2023OutputFile = inputsFolder.resolve("2023/artists.csv")
val seriesOutputFile = inputsFolder.resolve("series.csv")
val merchOutputFile = inputsFolder.resolve("merch.csv")
val rallies2025OutputFile = inputsFolder.resolve("2025/rallies.csv")

downloadUrl(sheetId2024, "Artists", artists2024OutputFile, "A1:M")
downloadUrl(sheetId2023, "Artists", artists2023OutputFile, "A1:K")

downloadUrl(sheetId2025, "Artists", artists2025OutputFile, "A1:Q")
downloadUrl(sheetId2025, "Series", seriesOutputFile)
downloadUrl(sheetId2025, "Merch", merchOutputFile)

// For some reason Stamp Rallies doesn't download correctly, dev should overwrite manually.
// This is kept around in case optimistically it fixes itself.
//downloadUrl("Stamp Rallies", "rallies.csv", "A1:G")
downloadUrl(sheetId2025, "Stamp Rallies", rallies2025OutputFile, "A1:K")

fun downloadUrl(sheetId: String, sheetName: String, outputFile: File, range: String? = null) {
    outputFile.parentFile.mkdirs()
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
