# Gradle Config Plugin

This plugin helps you to easily handle productFlavor and buildConfig specific settings with yaml format.

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
config/${buildType}.yml
```

The lower one overrides upper one deeply.

## License
```
Copyright 2015 Takuya Miyamoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
