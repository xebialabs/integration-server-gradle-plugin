package ai.digital.integration.server.util

import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

/**
 * Locks in the backward-compatible defaults that keep the backend (xld-is-data) unaffected: the DBUnit
 * coordinate defaults to xld-is-data and the opt-in dist-mode export (dbUnitDistExport) defaults to false.
 */
class DeployIntegrationServerExtensionTest {

    @Test
    fun defaultsKeepBackendBehavior() {
        val project = ProjectBuilder.builder().build()
        DeployExtensionUtil.createDeployExtension(project)
        val extension = DeployExtensionUtil.getExtension(project)

        assertEquals("com.xebialabs.deployit.plugins:xld-is-data", extension.xldIsDataArtifact)
        // Opt-in flag for the producer dist-mode export — MUST default off so the backend producer no-ops.
        assertFalse(extension.dbUnitDistExport)
    }

    @Test
    fun initializeReadsXldIsDataArtifactOverride() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties.set("xldIsDataArtifact", "com.xebialabs.deployit.plugins:xld-ci-explorer-data")
        DeployExtensionUtil.createDeployExtension(project)

        DeployExtensionUtil.initialize(project)

        assertEquals(
            "com.xebialabs.deployit.plugins:xld-ci-explorer-data",
            DeployExtensionUtil.getExtension(project).xldIsDataArtifact
        )
    }
}
