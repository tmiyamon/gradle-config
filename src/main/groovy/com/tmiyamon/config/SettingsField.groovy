package com.tmiyamon.config

import java.text.SimpleDateFormat

class SettingsField implements SettingsElement {
    String key;
    Object value;
    boolean isTopLevel

    SettingsField(String key, Object value) {
        this.key = key
        this.value = value
    }

    @Override
    String typeString() {
        value.getClass().getName()
    }

    @Override
    String name() {
        key
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

    }
}
