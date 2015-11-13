package com.tmiyamon.config

class SettingsClass implements SettingsElement {
    List<String> keys
    List<SettingsElement> children
    boolean isTopLevel

    public SettingsClass(List<String> keys, List<SettingsElement> children) {
        this.keys = keys
        this.children = children
    }

    @Override
    String typeString() {
        Util.className(keys.join('_'))
    }

    @Override
    String name() {
//        Util.camelize(key)
        keys.last()
    }

    String valueString() {
        "new ${typeString()}(${children.collect { it.generateSource() }.join(",")})"
    }

    @Override
    String generateSource() {
        if (isTopLevel) {
            "public static final ${typeString()} ${name()} = ${valueString()};"
        } else {
            valueString()
        }
    }

    @Override
    SettingsElement toTopLevel() {
        isTopLevel = true
        this
    }

    @Override
    void collectClassSources(Map<String, String> classSources) {
        classSources[typeString()] =
            "static final class ${typeString()} { ${fieldsString()} ${constructorString()} }"

        children.each { it.collectClassSources(classSources) }
    }

    String fieldString(SettingsElement element) {
        "public final ${element.typeString()} ${element.name()};"
    }

    String fieldsString() {
        children.collect { fieldString(it) }.join('')
    }

    String assignString(SettingsElement element) {
        "this.${element.name()} = ${element.name()};"
    }

    String assignsString() {
        children.collect { assignString(it) }.join('')
    }

    String paramString(SettingsElement element) {
        "${element.typeString()} ${element.name()}"
    }

    String paramsString() {
        children.collect { paramString(it) }.join(',')
    }

    String constructorString() {
        "public ${typeString()}(${paramsString()}){${assignsString()}}"
    }
}
