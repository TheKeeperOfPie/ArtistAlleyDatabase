
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ComposeFilesParseTask : DefaultTask() {

    companion object {
        private const val PACKAGE_NAME = "com.thekeeperofpie.artistalleydatabase.generated"
    }

    @get:InputDirectory
    abstract val inputFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    init {
        inputFolder.convention(
            project.layout.projectDirectory.dir("src/commonMain/composeResources/files")
        )
        outputFolder.convention(project.layout.buildDirectory.dir("generated/source"))
    }

    private val listComposeFileType =
        List::class.asClassName().parameterizedBy(ClassName(PACKAGE_NAME, "ComposeFile"))

    @TaskAction
    fun run() {
        FileSpec.builder(PACKAGE_NAME, "ComposeFiles")
            .addType(accessorType())
            .addType(folderType())
            .build()
            .writeTo(outputFolder.asFile.get())
    }

    private fun accessorType(): TypeSpec {
        val folders = parseFolders()
        return TypeSpec.objectBuilder("ComposeFiles")
            .addProperty(
                PropertySpec.builder("folders", listComposeFileType)
                    .initializer(
                        CodeBlock.builder()
                            .apply {
                                add("listOf(\n")
                                folders.forEach {
                                    appendFileCode(it, 1)
                                }
                                add(")")
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun folderType() = TypeSpec.classBuilder("ComposeFile")
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("name", String::class)
                .addParameter(
                    ParameterSpec.builder("files", listComposeFileType)
                        .defaultValue("emptyList()")
                        .build()
                )
                .build()
        )
        .addProperty(
            PropertySpec.builder("name", String::class)
                .initializer("name")
                .build()
        )
        .addProperty(
            PropertySpec.builder("files", listComposeFileType)
                .initializer("files")
                .build()
        )
        .build()

    private fun parseFolders() = inputFolder.asFile.get().listFiles().map(::parseFile)

    private fun parseFile(file: File): ComposeFile = if (file.isDirectory) {
        ComposeFile(file.name, file.listFiles().map(::parseFile))
    } else {
        ComposeFile(file.name, emptyList())
    }

    private fun CodeBlock.Builder.appendFileCode(file: ComposeFile, level: Int) {
        if (file.files.isEmpty()) {
            addStatement("ComposeFile(name = %S),", file.name)
            return
        }
        addStatement("ComposeFile(")
        addStatement("name = %S,", file.name)
        addStatement("files = listOf(")
        file.files.forEach {
            appendFileCode(it, level + 1)
        }
        add("),\n),\n")
    }

    data class ComposeFile(
        val name: String,
        val files: List<ComposeFile>,
    )
}
