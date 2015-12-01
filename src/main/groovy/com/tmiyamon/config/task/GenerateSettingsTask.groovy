package com.tmiyamon.config.task

import com.tmiyamon.config.SettingsClassGenerator
import com.tmiyamon.config.Util
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.Yaml

public class GenerateSettingsTask extends DefaultTask {
    @Input
    String packageName

    @Input
    def flavorName

    @Input
    def buildTypeName

    @Input
    def variantDirName

    @InputFiles
    def settingsFiles() {
        ['default', flavorName, buildTypeName].collect { [it, "${it}_secret"] }.flatten().collect {
            project.file(Util.pathJoin('config', "${it}.yml"))
        }
    }

    @OutputDirectory
    File outputDir() {
        project.file("${project.buildDir}/generated/source/settings/${variantDirName}")
    }

    @OutputFile
    File outputFile() {
        project.file("${outputDir().absolutePath}/${packageName.replace('.', '/')}/Settings.java")
    }

    @TaskAction
    def taskAction() {
        def yaml = new Yaml()
        def settingMaps = settingsFiles().collect { loadConfig(yaml, it) }

        def settings = Util.deepMerge(*settingMaps)
        if (!settings.isEmpty()) {
            def source = SettingsClassGenerator.buildAST(settings).generateSource()
            def outputFile = outputFile()
            if (!outputFile.isFile()) {
                outputFile.delete()
                outputFile.parentFile.mkdirs()
            }

            outputFile.text = "package ${packageName};\n" + source
        }
    }

    static public Map loadConfig(Yaml yaml, File f) {
        f.isFile() ? f.withReader { yaml.load(it) as Map } : [:]
    }
}
