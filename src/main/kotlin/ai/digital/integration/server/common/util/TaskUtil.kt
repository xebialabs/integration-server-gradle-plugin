package ai.digital.integration.server.common.util

import org.gradle.api.Action
import org.gradle.api.Task

class TaskUtil {
    companion object {
        fun dontFailOnException(task: Task) {
            task.actions = task.actions.map { action ->
                Action {
                    try {
                        action.execute(task)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }
    }
}
