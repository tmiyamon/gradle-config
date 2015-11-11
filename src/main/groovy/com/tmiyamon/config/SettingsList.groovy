package com.tmiyamon.config

class SettingsList implements SettingsElement {
    String key
    List<SettingsElement> children
    List<String> modifiers = ['final']

    SettingsList(String key, List<SettingsElement> children) {
        this.key = key
        this.children = children
    }

    String typeString() {
        "ArrayList<${key}Element>"
    }

    String name() {
        key
    }

    @Override
    String generateSource() {
        "public ${modifiers.join(' ')} ${typeString()} ${name()} = new ${typeString()}();"
    }

    @Override
    SettingsElement toTopLevel() {
        modifiers = ['static final']
        this
    }

    @Override
    void collectClassSources(Map<String, String> classSources) {

    }
}
