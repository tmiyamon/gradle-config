package com.tmiyamon.config

interface SettingsElement {
    String generateSource()
    SettingsElement toTopLevel()
    String typeString()
    String name()
    void collectClassSources(Map<String, String> classSources)
}
