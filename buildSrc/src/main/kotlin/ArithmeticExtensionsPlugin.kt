import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.OutputFile
import org.gradle.kotlin.dsl.get
import java.io.File
import java.nio.file.Files

private open class GenerateArithmeticExtensionsTask : DefaultTask() {

    private val typeFileMapping = getTypeFileMapping("Arithmetic")
    // this annotation has to be on a fun, not a val
    // https://docs.gradle.org/current/userguide/custom_plugins.html#sec:working_with_files_in_custom_tasks_and_plugins
    @OutputFile fun getFileRA() = typeFileMapping["RA"]?.second
    @OutputFile fun getFileRAI() = typeFileMapping["RAI"]?.second


    @org.gradle.api.tasks.TaskAction
    fun runTask() {
        for ((`as`, name) in typeFileMapping) {
            println("generating arithmetic extensions for $`as` ($name)")
            Files.write(name.second.toPath(), generateSource(`as`, name.first).toByteArray())
        }
    }

}


class ArithmeticExtensionsPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        tasks.register(generateArithmeticExtensionsName, GenerateArithmeticExtensionsTask::class.java)
        tasks["compileKotlin"].dependsOn(tasks[generateArithmeticExtensionsName])
    }
}

private const val generateArithmeticExtensionsName = "generateArithmeticExtensions"
