package com.jackie.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class JavassistPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        def android = project.extensions.getByType(AppExtension)

        def  classTransform = new JavassistTransform(project)
        android.registerTransform(classTransform)

        project.extensions.create("tryCatchInfo",TryCatchExtension)
        println("============create tryCatchInfor Extension")
        project.afterEvaluate {
            println("============afterEvaluate tryCatchInfor Extension")
            project.task("tryCatchTask",type:TryCatchTask)
        }

    }
}