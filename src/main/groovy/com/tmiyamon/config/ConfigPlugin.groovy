package com.tmiyamon.config

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml
import com.android.build.gradle.internal.dsl.ProductFlavor

class ConfigPlugin implements Plugin<Project> {
    private static final String CLASS_TEMPLATE = "public final class <%= className %> { <%= contents %> }";

    @Override
    void apply(Project project) {

        project.afterEvaluate {
            project.plugins.withId('com.android.application') {
                project.android.applicationVariants.all  {
                    println(it.name)
                    println(it.flavorName)
                    println('')
                }
                def yaml = new Yaml()
                project.android.productFlavors.each { productFlavor ->
                    def f = project.file("config/${productFlavor.name}.yml")
                    if (f.isFile()) {
                        f.withReader {
                            def y = yaml.load(it)
                            //setupBuildConfigField(productFlavor, [], y)

                            println('##########')
                            println(SettingsClassGenerator.buildAST(y as Map<String, Object>).generateSource())
                        }
                    } else {
                        println("Not found $f")
                    }
                }
            }
        }
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
