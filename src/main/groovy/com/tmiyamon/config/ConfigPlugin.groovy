package com.tmiyamon.config

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml
import com.android.build.gradle.internal.dsl.ProductFlavor

class ConfigPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            project.plugins.withId('com.android.application') {
                def packageName = getPackageName(project)
                def yaml = new Yaml()
                def configDir = project.file("config")

                if (configDir.isDirectory()) {
                    def defaultConfig = loadIfExist(yaml, new File(configDir, "default.yml"))

                    project.android.productFlavors.each { productFlavor ->
                        def name = productFlavor.name

                        def productFlavorConfig = loadIfExist(yaml, new File(configDir, "${name}.yml"))

                        def config = Util.deepMerge(defaultConfig, productFlavorConfig)

                        if (!config.isEmpty()) {
                            def source = SettingsClassGenerator.buildAST(config).generateSource()

                            project.android.sourceSets[name].java.srcDirs += getSrcDirPath(name)

                            def outputDir = project.file(getOutputDirPath(name, packageName))
                            outputDir.mkdirs()
                            new File(outputDir, 'Settings.java').text = "package ${packageName};\n" + source
                        }
                    }
                }
            }
        }
    }

    static public String getOutputDirPath(String productFlavorName, String packageName) {
        Util.pathJoin(getSrcDirPath(productFlavorName), *packageName.split("\\."))
    }

    static public String getSrcDirPath(String productFlavorName) {
        Util.pathJoin('build', 'generated', 'source', 'settings', productFlavorName)
    }

    static public String getPackageName(Project project) {
        new XmlParser().parse(project.file(Util.pathJoin('src', 'main', 'AndroidManifest.xml'))).attribute('package')
    }

    static public Map loadIfExist(Yaml yaml, File f) {
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
