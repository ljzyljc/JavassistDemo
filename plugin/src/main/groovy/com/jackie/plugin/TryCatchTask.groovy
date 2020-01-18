package com.jackie.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Try

class TryCatchTask extends DefaultTask{

    TryCatchExtension tryCatchExtension;

    TryCatchTask(){
        println("====TryCatchTask======Constructor======")
        tryCatchExtension = project.tryCatchInfo
    }

    @TaskAction
    void run(){
        println("====Task run====")
        println(tryCatchExtension.toString())

    }



}