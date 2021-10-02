package ai.digital.integration.server.util

import org.gradle.api.Action
import org.gradle.api.Task

class TaskUtil {
    companion object {
        @JvmStatic
        fun dontFailOnException(task: Task) {
            task.setActions(task.actions.map { action ->
                Action { current ->
                    action.execute(current)
                }
            })
        }
    }
}
