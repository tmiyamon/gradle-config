package com.tmiyamon.config.ast

class SettingsRootClass implements SettingsElement {
    List<SettingsElement> children

    public SettingsRootClass(List<SettingsElement> children) {
        this.children = children
    }

    @Override
    String generateSource() {
        def classSources = [:]
        collectClassSources(classSources)

        def fieldsString = children.collect { it.generateSource() }.join("\n")
        def classesString = classSources.values().join("\n")

        "public final class Settings {\n" +
            "${fieldsString}\n" +
            "${classesString}\n" +
        "}"
    }

    @Override
    SettingsElement toTopLevel() {
        return this
    }

    @Override
    String typeString() {
        return null
    }

    @Override
    String name() {
        return null
    }

    @Override
    void collectClassSources(Map<String, String> classSources) {
        children.each { it.collectClassSources(classSources) }
    }
}
