package com.ren

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.AaptOptions
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

public class Text implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task("ceshi").doLast {
            println("测试12134")
        }

        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        appExtension.aaptOptions({aaptOptions->

        } )
    }
}