package ai.digital.integration.server.common.domain.providers.terraform

import org.gradle.api.provider.Property;

interface Provider {
    var host: Property<String>
}
