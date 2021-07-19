package com.xebialabs.gradle.integration.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

class YamlUtil {

    static def mapper = new ObjectMapper(new YAMLFactory())
}
