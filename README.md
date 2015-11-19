# Gradle Config Plugin

This plugin helps you to easily handle product flavor specific settings with yaml format.

## Usage

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'com.tmiyamon:gradle-config:0.1.0'
  }
}

repositories {
  jcenter()
}

apply plugin: 'com.android.application'
apply plugin: 'com.tmiyamon.config'
```

## Settings class
The entries in config yaml will be available in ```Settings``` class as static members.

```
Settings.entry
```

## Config files

```
config/default.yml
config/${productFlavor}.yml
```

For example, when you have product flavor ```dev```,
```
productFlavor {
  dev
}
```

the ```Settings``` class will be compiled from ```config/dev.yml``` with overriding ```config/default.yml```.
