package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Copy
import static com.xebialabs.gradle.integration.util.PluginUtils.*

class CopyLicenseTask extends Copy {
    static NAME = "copyLicense"

    CopyLicenseTask() {
        this.configure {
            group = PLUGIN_GROUP
            dependsOn project.tasks.getByName(DownloadAndExtractServerDistTask.NAME)
            def licenseFile = ExtensionsUtil.getExtension(project).serverLicense
            from { licenseFile }
            into { "${ExtensionsUtil.getServerWorkingDir(project)}/conf" }
        }
    }
}