package com.xebialabs.gradle.integration.util

import org.gradle.api.Action
import org.gradle.api.Task

class TaskUtil {
    static void dontFailOnException(Task task) {
        task.setActions(task.getActions().collect {
            new Action<Task>() {
                @Override
                void execute(Task o) {
                    try {
                        it.execute(o)
                    } catch (swallowed) {
                        task.project.logger.lifecycle("${task.name} has failed but the failure is ignored. Reason: ${swallowed.cause}")
                    }
                }
            }
        })
    }
}
