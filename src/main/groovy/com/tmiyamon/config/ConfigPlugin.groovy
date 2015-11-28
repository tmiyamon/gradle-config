package com.tmiyamon.config

import com.tmiyamon.config.task.GenerateSettingsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.AppExtension

class ConfigPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.plugins.withId('com.android.application') {
            def android = project.extensions.getByType(AppExtension)
            android.applicationVariants.all { BaseVariant variant ->

                def task = project.tasks.create(
                    name: "generate${variant.name.capitalize()}Settings",
                    type: GenerateSettingsTask) {
                        packageName    variant.generateBuildConfig.buildConfigPackageName
                        flavorName     variant.flavorName
                        buildTypeName  variant.buildType.name
                        variantDirName variant.dirName
                    }

                variant.registerJavaGeneratingTask(task, task.outputDir())
            }
        }
    }
}

