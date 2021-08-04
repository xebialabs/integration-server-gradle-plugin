package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class PropertyUtil {

    static def resolveValue(Project project, String propertyName, def defaultValue) {
        if (project.hasProperty(propertyName)) {
            project.property(propertyName)
        } else {
            defaultValue
        }
    }

    static def resolveIntValue(Project project, String propertyName, def defaultValue) {
        def value = resolveValue(project, propertyName, defaultValue)
        if (value == null) {
            null as Integer
        } else Integer.parseInt(value as String)
    }

    static def resolveBooleanValue(Project project, String propertyName, Boolean defaultValue) {
        if (project.hasProperty(propertyName)) {
            def value = project.property(propertyName)
            !value || Boolean.parseBoolean(value as String)
        } else {
            defaultValue
        }
    }
}
