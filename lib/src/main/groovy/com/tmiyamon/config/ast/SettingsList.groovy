package com.tmiyamon.config.ast

class SettingsList implements SettingsElement {
    List<String> keys
    List<SettingsElement> children
    boolean isTopLevel

    SettingsList(List<String> keys, List<SettingsElement> children) {
        this.keys = keys
        this.children = children
    }

    String typeString() {
        def childTypes = children.collect { it.typeString() }.unique()
        if (childTypes.size() != 1) {
            throw new RuntimeException("Not supported lis with mixed type: $childTypes")
        }
        "java.util.ArrayList<${childTypes.first()}>"
    }

    String name() {
        keys.last()
    }

    String valueString() {
        "new ${typeString()}(){{ ${children.collect{ "add(${it.generateSource()});" }.join((''))} }}"
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
        this.isTopLevel = true
        this
    }

    @Override
    void collectClassSources(Map<String, String> classSources) {
        children.each { it.collectClassSources(classSources) }
    }
}
