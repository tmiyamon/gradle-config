package com.tmiyamon.config

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.Yaml
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.AppExtension

class ConfigPlugin implements Plugin<Project> {
    class SampleTask extends DefaultTask {
    }

    @Override
    void apply(Project project) {
        project.plugins.withId('com.android.application') {
            def android = project.extensions.getByType(AppExtension)
            android.applicationVariants.all { BaseVariant variant ->

                println variant.generateBuildConfig

                File srcDir = project.file("${project.buildDir}/generated/source/settings/${variant.dirName}")
                def generateSettingsTask = project.tasks.create(name: "generateSettings${variant.name.capitalize()}", type: GenerateSettings) {
                    def yaml = new Yaml()
                    def configDir = project.file('config')

                    defaultSettings loadConfig(yaml, new File(configDir, "default.yaml"))
                    productFlavorSettings loadConfig(yaml, new File(configDir, "${variant.flavorName}.yaml"))
                    buildTypeSettings loadConfig(yaml, new File(configDir, "${variant.buildType.name}.yaml"))
                    packageName variant.generateBuildConfig.buildConfigPackageName

                    outputFile project.file(Util.pathJoin(srcDir.absolutePath, *variant.generateBuildConfig.buildConfigPackageName.split('\\.')))
                }


//                // create a task that generates a java class
//                File sourceFolder = project.file("${project.buildDir}/customCode/${variant.dirName}")
//                def javaGenerationTask = project.tasks.create(name: "generatedCodeFor${variant.name.capitalize()}", type: GenerateSettings) {
//                    value new Date().format("yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC"))
//                    outputFile project.file("${sourceFolder.absolutePath}/com/custom/Foo.java")
//                }

                variant.registerJavaGeneratingTask(generateSettingsTask, srcDir)
            }

//            project.afterEvaluate {
//                project.android.applicationVariants.all { BaseVariant variant ->
//                    println variant.sourceSets.javaDirectories
//                    println variant.generateBuildConfig.sourceOutputDir
//                    println variant.generateBuildConfig.buildConfigPackageName
//
//                }
//            }
        }
    }


//        project.afterEvaluate {
//            project.plugins.withId('com.android.application') {
//                def packageName = getPackageName(project)
//                def yaml = new Yaml()
//                def configDir = project.file("config")
//                project.file(getSrcBaseDirPath()).deleteDir()
//
//                if (configDir.isDirectory()) {
//                    def defaultConfig = loadIfExist(yaml, new File(configDir, "default.yml"))
//                    def variants = project.android.applicationVariants as List
//
//                    if (variants.isEmpty()) {
//                        installSettings(project, 'main', packageName, defaultConfig)
//                    } else {
//                        variants.each { variant ->
//                            def flavorName = variant.getFlavorName()
//                            def buildTypeName = variant.getBuildType().getName()
//
//                            def flavorConfig = loadIfExist(yaml, new File(configDir, "${flavorName}.yml"))
//                            def buildTypeConfig = loadIfExist(yaml, new File(configDir, "${buildTypeName}.yml"))
//                            def config = Util.deepMerge(defaultConfig, flavorConfig, buildTypeConfig)
//                            if (!config.isEmpty()) {
//                                def source = SettingsClassGenerator.buildAST(config).generateSource()
////                                def sourceDir = project.file(getSrcDirPath(variant.getDirName()))
////                                println "adding $sourceDir to sourceSets"
////                                variant.sourceSets.java.srcDirs.add(sourceDir)
//
//                                def outputDir = project.file(getOutputDirPath(variant.getDirName(), packageName))
//                                outputDir.mkdirs()
//                                new File(outputDir, 'Settings.java').text = "package ${packageName};\n" + source
//                                println("installed " + new File(outputDir, 'Settings.java'))
//                            }
//                        }
//                    }
//                }
//
////                if (configDir.isDirectory()) {
////                    def defaultConfig = loadIfExist(yaml, new File(configDir, "default.yml"))
////                    def productFlavors = project.android.productFlavors as List
////
////                    if (productFlavors.isEmpty()) {
////                        installSettings(project, 'main', packageName, defaultConfig)
////                    } else {
////
////                        productFlavors.each { productFlavor ->
////                            def name = productFlavor.name
////                            def productFlavorConfig = loadIfExist(yaml, new File(configDir, "${name}.yml"))
////                            def config = Util.deepMerge(defaultConfig, productFlavorConfig)
////
////                            if (!config.isEmpty()) {
////                                installSettings(project, name, packageName, config)
////                            }
////                        }
////                    }
////                }
//            }
//        }
//
//    }

    static public installSettings(Project project, String sourceSetName, String packageName, Map config) {
        def source = SettingsClassGenerator.buildAST(config).generateSource()

        project.android.sourceSets[sourceSetName].java.srcDirs += getSrcDirPath(sourceSetName)

        def outputDir = project.file(getOutputDirPath(sourceSetName, packageName))
        outputDir.mkdirs()
        new File(outputDir, 'Settings.java').text = "package ${packageName};\n" + source
        println("installed " + new File(outputDir, 'Settings.java'))
    }

    static public String getOutputDirPath(String productFlavorName, String packageName) {
        Util.pathJoin(getSrcDirPath(productFlavorName), *packageName.split("\\."))
    }

    static public  String getSrcBaseDirPath() {
        Util.pathJoin('build', 'generated', 'source', 'settings')
    }

    static public String getSrcDirPath(String productFlavorName) {
        Util.pathJoin(getSrcBaseDirPath(), productFlavorName)
    }

    static public String getPackageName(Project project) {
        new XmlParser().parse(project.file(Util.pathJoin('src', 'main', 'AndroidManifest.xml'))).attribute('package')
    }

    static public Map loadConfig(Yaml yaml, File f) {
        f.isFile() ? f.withReader { yaml.load(it) as Map } : [:]
    }

    static String defaultKey(List<String> keys) {
        return keys.collect { it.toUpperCase() }.join('_')
    }


    def setupBuildConfigField(ProductFlavor productFlavor, List<String> keys, Object value) {
        if (value instanceof String) {
            productFlavor.buildConfigField "String", defaultKey(keys), "\"${value}\""
        } else if (value instanceof Integer) {
            productFlavor.buildConfigField "int", defaultKey(keys), "${value}"
        } else if (value instanceof Float) {
            productFlavor.buildConfigField "float", defaultKey(keys), "${value}"
        } else if (value instanceof Double) {
            productFlavor.buildConfigField "double", defaultKey(keys), "${value}"
        } else if (value instanceof Boolean) {
            productFlavor.buildConfigField "boolean", defaultKey(keys), "${value}"
        } else if (value instanceof List) {
            def list = value as List
            def classSet = list.inject(new HashSet<Class>()) { acc, v -> (acc as Set).add(v.getClass()); acc } as Set

            if (classSet.size() == 1) {
                if (classSet.first() == String.class) {
                    productFlavor.buildConfigField "String[]", defaultKey(keys), "new String[] { ${list.collect { "\"${it}\"" }.join(',')} }"
                } else if(classSet.first() == Integer.class) {
                    productFlavor.buildConfigField "int[]", defaultKey(keys), "new int[] { ${list.join(',')} }"
                } else if(classSet.first() == Float.class) {
                    productFlavor.buildConfigField "float[]", defaultKey(keys), "new float[] { ${list.join(',')} }"
                } else if(classSet.first() == Double.class) {
                    productFlavor.buildConfigField "double[]", defaultKey(keys), "new double[] { ${list.join(',')} }"
                } else if(classSet.first() == Boolean.class) {
                    productFlavor.buildConfigField "boolean[]", defaultKey(keys), "new boolean[] { ${list.join(',')} }"
                } else {
                    println("Not supported type: list of ${classSet.first()} at ${keys.join(('.'))} in ${productFlavor.name}")
                }
            } else {
                println("Not supported type: list of mixed ${classSet} at ${keys.join(('.'))} in ${productFlavor.name}")
            }
        } else if (value instanceof Map<String, Object>) {
            (value as Map<String, Object>).each { k, v ->
                setupBuildConfigField(productFlavor, keys + [k] as List, v)
            }
        } else {
            println("Not supported type: ${value.getClass()} at ${keys.join(('.'))} in ${productFlavor.name}")
        }
    }
}

public class GenerateSettings extends DefaultTask {
    @Input
    Map defaultSettings

    @Input
    Map buildTypeSettings

    @Input
    Map productFlavorSettings

    @Input
    String packageName

    @OutputFile
    File outputFile

    @TaskAction
    void taskAction() {
        def settings = Util.deepMerge(defaultSettings, productFlavorSettings, buildTypeSettings)
        if (!settings.isEmpty()) {
            def source = SettingsClassGenerator.buildAST(settings).generateSource()
            outputFile.text = "package ${packageName};\n" + source
        }
//
//        getOutputFile().text =
//            "package com.custom;\n" +
//                "public class Foo {\n" +
//                "    public static String getBuildDate() { return \"${getValue()}\"; }\n" +
//                "}\n";
    }
}
