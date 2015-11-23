package com.tmiyamon.config.ast

interface SettingsElement {
    String generateSource()
    SettingsElement toTopLevel()
    String typeString()
    String name()
    void collectClassSources(Map<String, String> classSources)
}
