import java.net.URL
import java.io.File

val secrets = __FILE__.resolve("../../../secrets.properties")
val sheetIdAnimeExpo2024 = secrets.useLines { it.first { it.startsWith("sheetIdAnimeExpo2024=") } }.removePrefix("sheetIdAnimeExpo2024=")
val sheetIdAnimeExpo2023 = secrets.useLines { it.first { it.startsWith("sheetIdAnimeExpo2023=") } }.removePrefix("sheetIdAnimeExpo2023=")
val sheetIdAnimeExpo2025 = secrets.useLines { it.first { it.startsWith("sheetIdAnimeExpo2025=") } }.removePrefix("sheetIdAnimeExpo2025=")
val sheetIdAnimeNyc2024 = secrets.useLines { it.first { it.startsWith("sheetIdAnimeNyc2024=") } }.removePrefix("sheetIdAnimeNyc2024=")
val sheetIdAnimeNyc2025 = secrets.useLines { it.first { it.startsWith("sheetIdAnimeNyc2025=") } }.removePrefix("sheetIdAnimeNyc2025=")
val inputsFolder = __FILE__.resolve("../../inputs/")
val artistsAnimeExpo2024OutputFile = inputsFolder.resolve("2024/artists.csv")
val artistsAnimeExpo2023OutputFile = inputsFolder.resolve("2023/artists.csv")
val artistsAnimeExpo2025OutputFile = inputsFolder.resolve("2025/artists.csv")
val artistsAnimeNyc2024OutputFile = inputsFolder.resolve("animeNyc2024/artists.csv")
val artistsAnimeNyc2025OutputFile = inputsFolder.resolve("animeNyc2025/artists.csv")
val seriesOutputFile = inputsFolder.resolve("series.csv")
val merchOutputFile = inputsFolder.resolve("merch.csv")
val ralliesAnimeExpo2025OutputFile = inputsFolder.resolve("animeExpo2025/rallies.csv")

//downloadUrl(sheetIdAnimeExpo2023, "Artists", artistsAnimeExpo2023OutputFile, "A1:K")
//downloadUrl(sheetIdAnimeExpo2024, "Artists", artistsAnimeExpo2024OutputFile, "A1:M")
//downloadUrl(sheetIdAnimeExpo2025, "Artists", artistsAnimeExpo2025OutputFile, "A1:Q")

downloadUrl(sheetIdAnimeNyc2024, "Artists", artistsAnimeNyc2024OutputFile, "A1:Q")

downloadUrl(sheetIdAnimeNyc2025, "Artists", artistsAnimeNyc2025OutputFile, "A1:Q")
downloadUrl(sheetIdAnimeNyc2025, "Series", seriesOutputFile)
downloadUrl(sheetIdAnimeNyc2025, "Merch", merchOutputFile)

// For some reason Stamp Rallies doesn't download correctly, dev should overwrite manually.
// This is kept around in case optimistically it fixes itself.
//downloadUrl("Stamp Rallies", "rallies.csv", "A1:G")
//downloadUrl(sheetIdAnimeExpo2025, "Stamp Rallies", ralliesAnimeExpo2025OutputFile, "A1:K")

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
