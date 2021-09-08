package ai.digital.integration.server.util

import org.apache.commons.lang3.BooleanUtils
import org.gradle.api.Project

class PropertyUtil {

    static def resolveValue(Project project, String propertyName, def defaultValue) {
        if (project.hasProperty(propertyName)) {
            project.property(propertyName)
        } else {
            defaultValue
        }
    }

    static Integer resolveIntValue(Project project, String propertyName, def defaultValue) {
        def value = resolveValue(project, propertyName, defaultValue)
        if (value == null) {
            null as Integer
        } else Integer.parseInt(value as String)
    }

    static Boolean resolveBooleanValue(Project project, String propertyName, def defaultValue) {
        def value = resolveValue(project, propertyName, defaultValue)
        if (value == null) {
            null as Boolean
        } else BooleanUtils.toBoolean(value as String)
    }

}
