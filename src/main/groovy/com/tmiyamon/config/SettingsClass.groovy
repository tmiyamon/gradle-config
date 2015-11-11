package com.tmiyamon.config

class SettingsClass implements SettingsElement {
    String key
    List<SettingsElement> children
    List<String> modifiers = ['final']

    public SettingsClass(String key, List<SettingsElement> children) {
        this.key = key
        this.children = children
    }

    String typeString() {
//        Util.camelize(key, true)
        key
    }

    String name() {
//        Util.camelize(key)
        key
    }

    @Override
    String generateSource() {
        "public ${modifiers.join(' ')} ${typeString()} ${name()} = new ${typeString()}();\n" +
            "static final class ${typeString()} {\n" +
            children.collect { it.generateSource() }.join("\n") + "\n"+
            "}"
    }

    @Override
    SettingsElement toTopLevel() {
        modifiers = ['static final']
        this
    }
}
