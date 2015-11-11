package com.tmiyamon.config

class SettingsRootClass implements SettingsElement {
    List<SettingsElement> children

    public SettingsRootClass(List<SettingsElement> children) {
        this.children = children
    }

    @Override
    String generateSource() {
        "public final class Settings {\n" +
            children.collect { it.generateSource() }.join("\n") + "\n" +
        "}"
    }

    @Override
    SettingsElement toTopLevel() {
        return this
    }
}
