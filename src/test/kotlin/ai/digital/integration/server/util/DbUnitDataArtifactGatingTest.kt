package ai.digital.integration.server.util

import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractDbUnitDataDistTask.Companion.DEFAULT_DATA_ARTIFACT_NAME
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractDbUnitDataDistTask.Companion.dataArtifactName
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractDbUnitDataDistTask.Companion.isCustomDataArtifact
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractDbUnitDataDistTask.Companion.repositoryFolderName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Guards the gating rule that keeps the backend's xld-is-data path byte-for-byte legacy while the new
 * dist-mode DBUnit extraction/import applies only to overridden (FE) coordinates like xld-ci-explorer-data.
 * This single rule is shared by DownloadAndExtractDbUnitDataDistTask and ImportDbUnitDataTask.
 */
class DbUnitDataArtifactGatingTest {

    @Test
    fun defaultArtifactNameIsXldIsData() {
        // If this ever changes, the backend gate silently flips — assert it explicitly.
        assertEquals("xld-is-data", DEFAULT_DATA_ARTIFACT_NAME)
    }

    @Test
    fun backendDefaultCoordinateIsNotCustom() {
        // The exact default set on the extension.
        assertFalse(isCustomDataArtifact("com.xebialabs.deployit.plugins:xld-is-data"))
    }

    @Test
    fun bareBackendNameIsNotCustom() {
        // A coordinate with no group still resolves to the trailing name.
        assertFalse(isCustomDataArtifact("xld-is-data"))
    }

    @Test
    fun sameNameUnderADifferentGroupIsNotCustom() {
        // Only the artifact NAME gates behavior — group is irrelevant.
        assertFalse(isCustomDataArtifact("org.example.other:xld-is-data"))
    }

    @Test
    fun feCoordinateIsCustom() {
        // The real FE producer coordinate that must get the new dist-mode behavior.
        assertTrue(isCustomDataArtifact("com.xebialabs.deployit.plugins:xld-ci-explorer-data"))
    }

    @Test
    fun bareCustomNameIsCustom() {
        assertTrue(isCustomDataArtifact("xld-ci-explorer-data"))
    }

    @Test
    fun anyOtherArtifactNameIsCustom() {
        assertTrue(isCustomDataArtifact("com.example:some-other-data"))
    }

    @Test
    fun dataArtifactNameStripsGroup() {
        assertEquals("xld-is-data", dataArtifactName("com.xebialabs.deployit.plugins:xld-is-data"))
        assertEquals("xld-ci-explorer-data", dataArtifactName("com.xebialabs.deployit.plugins:xld-ci-explorer-data"))
        assertEquals("xld-is-data", dataArtifactName("xld-is-data"))
    }

    @Test
    fun repositoryFolderNameMatchesTheDownloadExtractAndImportReadPath() {
        // This is the write-path == read-path invariant: download extracts into <folder> and import reads
        // <folder>/data.xml. Both go through this one helper, so the folder name must be exactly this shape.
        assertEquals(
            "xld-ci-explorer-data-26.3.0-SNAPSHOT-repository",
            repositoryFolderName("com.xebialabs.deployit.plugins:xld-ci-explorer-data", "26.3.0-SNAPSHOT")
        )
        // Backend default resolves to the historical legacy folder name — unchanged.
        assertEquals(
            "xld-is-data-26.3.0-repository",
            repositoryFolderName("com.xebialabs.deployit.plugins:xld-is-data", "26.3.0")
        )
    }
}
