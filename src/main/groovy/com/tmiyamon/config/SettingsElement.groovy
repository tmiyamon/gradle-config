package com.tmiyamon.config

interface SettingsElement {
    String generateSource()
    SettingsElement toTopLevel()
}
