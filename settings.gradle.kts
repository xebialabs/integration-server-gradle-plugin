pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            url = uri("https://oss.sonatype.org/service/local/repositories/releases/content")
        }
    }
}

rootProject.name = "integration-server-gradle-plugin"
