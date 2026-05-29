import java.util.concurrent.TimeUnit

val inputDir = __FILE__.parentFile.resolve("build/icons")
inputDir.listFiles()!!.forEach {
    // SVGO can't handle spaces somehow
    if (it.name.contains(" ")) {
        it.renameTo(it.resolveSibling(it.name.replace(" ", "_")))
    }
}

val inkscapeOutputDir = __FILE__.parentFile.resolve("build/inkscape")
inputDir.listFiles()!!.forEach {
    runCommand(
        "C:\\Program Files\\Inkscape\\bin\\inkscape.com",
        "--actions=\"select-all:all;object-stroke-to-path\"",
        "--export-filename=\"${inkscapeOutputDir.resolve(it.name).absolutePath}\"",
        it.absolutePath
    )
}

val svgoOutputDir = __FILE__.parentFile.resolve("build/svgo")
inkscapeOutputDir.listFiles()!!.forEach {
    runCommand(
        "bunx",
        "svgo",
        "-i",
        it.absolutePath,
        "-o",
        svgoOutputDir.resolve(it.name).absolutePath,
    )
}

val valkyrieOutputDir = __FILE__.parentFile.resolve("build/valkyrie")
svgoOutputDir.listFiles()!!.forEach {
    runCommand(
        "D:\\Downloads\\valkyrie-cli-1.1.1\\bin\\valkyrie.bat",
        "svgxml2imagevector",
        "--package-name=com.thekeeperofpie.artistalleydatabase.alley.merch.icons",
        "--input-path=${it.absolutePath}",
        "--output-path=${valkyrieOutputDir.absolutePath}",
    )
}

fun runCommand(vararg params: String) {
    val process = ProcessBuilder(params.toList())
        .inheritIO()
        .redirectErrorStream(true)
        .start()
    val exited = process.waitFor(150, TimeUnit.SECONDS)
    if (!exited) {
        throw IllegalStateException("Command failed to exit")
    }

    val exitValue = process.exitValue()
    if (exitValue != 0) {
        throw IllegalStateException("Failed to run command: $exitValue")
    }
}
