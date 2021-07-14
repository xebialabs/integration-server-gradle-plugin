package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ApplicationsUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP


class IntegrationServerTask extends DefaultTask {

    IntegrationServerTask() {
        def dependencies = [
                ApplicationsUtil.detectApplication(ApplicationsUtil.applicationName(project))
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    static NAME = "integrationServer"

    @TaskAction
    void launch() {

    }
}
