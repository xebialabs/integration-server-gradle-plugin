package ai.digital.integration.server.util

import org.gradle.api.Project

class PropertyUtil {
    companion object {
        @JvmStatic
        fun resolveValue(project: Project, propertyName: String, defaultValue: Any?): Any? {
            return if (project.hasProperty(propertyName)) {
                project.property(propertyName)
            } else {
                defaultValue
            }
        }

        @JvmStatic
        fun resolveIntValue(project: Project, propertyName: String, defaultValue: Any?): Any? {
            val value = resolveValue(project, propertyName, defaultValue)
            return value?.toString()?.toInt()
        }

        @JvmStatic
        fun resolveBooleanValue(project: Project, propertyName: String, defaultValue: Any?): Any? {
            val value = resolveValue(project, propertyName, defaultValue)
            return value?.toString()?.toBoolean()
        }
    }
}
