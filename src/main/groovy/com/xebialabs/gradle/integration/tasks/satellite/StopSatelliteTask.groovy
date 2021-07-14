package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.util.ApplicationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.PluginUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP
import static com.xebialabs.gradle.integration.util.PluginUtil.getDistLocation

class StopSatelliteTask extends DefaultTask {
    static NAME = "stopSatellite"
    static STOP_SATELLITE_SCRIPT = "stopSatellite.sh"


    StopSatelliteTask() {
        group = PLUGIN_GROUP
    }

    private def copyStopSatelliteScript() {
        def from = StopSatelliteTask.class.classLoader.getResourceAsStream("satellite/bin/$STOP_SATELLITE_SCRIPT")
        def intoDir = getDistLocation(project).resolve(STOP_SATELLITE_SCRIPT)
        FileUtil.copyFile(from, intoDir)
    }

    private def getWorkingDir() {
        return getDistLocation(project).toFile()
    }

    private void stopSatellite() {
        project.logger.lifecycle("Stopping satellite")

        ProcessUtil.chMod(project, "777", getDistLocation(project).resolve(STOP_SATELLITE_SCRIPT).toAbsolutePath().toString())
        ProcessUtil.exec([
                command: "stopSatellite",
                workDir: getWorkingDir()
        ])
    }

    private void readPidKill(){
        def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        def pid = project.file("${targetDir}/${ApplicationsUtil.SATELLITE_START}").text.toLong()

        project.logger.lifecycle("pid from file -> ${pid}")
        ProcessHandle parentProcess = ProcessHandle.of(pid).get().parent().get()
        project.logger.lifecycle("parent process id:"+parentProcess.pid());
        project.logger.lifecycle("current process id:"+ProcessHandle.current());

       // ProcessUtil.killPid(project, pid.toString())
       // ProcessUtil.killPid(project, currentProcess.pid().toString())
        //parentProcess.destroy()
/*        project.exec {
            it.executable 'kill'
            //it.args "-9", pid.toString(), currentProcess.pid().toString()
            it.args "-9", pid.toString()
        }

        project.exec {
            it.executable 'kill'
            //it.args "-9", pid.toString(), currentProcess.pid().toString()
            it.args "-9", parentProcess.pid().toString()
        }*/
    }

    @TaskAction
    void stop() {
        copyStopSatelliteScript()
        stopSatellite()
        //readPidKill()
    }
}
