package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class WorkerUtil {

    static def isWorkerEnabled(Project project) {
        project.hasProperty("externalWorker") ?
                project.getProperty("externalWorker").toBoolean() : false
    }

}
