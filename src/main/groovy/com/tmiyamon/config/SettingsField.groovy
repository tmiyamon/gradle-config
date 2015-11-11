package com.tmiyamon.config

import java.text.SimpleDateFormat

class SettingsField implements SettingsElement {
    String key;
    Object value;
    List<String> modifiers = ['final']


    SettingsField(String key, Object value) {
        this.key = key
        this.value = value
    }

    String typeString() {
        value.getClass().getName()
    }

    String valueString() {
        if (value instanceof String) {
            "\"${value}\""
        } else if (
        value instanceof Integer ||
            value instanceof Double ||
            value instanceof Boolean
        ) {
            value
        } else if (value instanceof Date) {
            "new java.util.Date(${(value as Date).getTime()}L)"
        } else {
            throw new RuntimeException("Not supported $value as SettingsField")
        }
    }

    @Override
    String generateSource() {
        "public ${modifiers.join(' ')} ${typeString()} ${key} = ${valueString()};"
    }

    @Override
    SettingsElement toTopLevel() {
        modifiers = ['static final']
        this
    }
}
