package com.xebialabs.gradle.integration.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator

/**
 * Yaml file manipulator
 */
class YamlFileUtil {

    static def mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))

    private def static getKeyParentAndLastToken(Map<String, Object> objectMap, String key) {
        def tokens = key.split("\\.")
        def keyChain = [tokens[0] + "." + tokens[1]]
        keyChain = keyChain.plus(tokens.init().drop(2)).flatten()

        def current = objectMap
        keyChain.each { keyItem ->
            def val = objectMap.get(keyItem)
            if (val == null) {
                objectMap.put(keyItem, new LinkedHashMap<String, Object>())
            }
            current = objectMap.get(keyItem)
        }
        return new Tuple(current, tokens.last())
    }

    private def static readKey(Map<String, Object> objectMap, String key) {
        def tuple = getKeyParentAndLastToken(objectMap, key)
        return tuple[0].get(tuple[1])
    }

    private def static addPair(Map<String, Object> initial, Map<String, Object> newPairs) {
        newPairs.each { pair ->
            def tuple = getKeyParentAndLastToken(initial, pair.key)
            tuple[0].put(tuple[1], pair.value)
        }
    }

    private def static mingleValues(Object source, Map<String, Object> pairs, File destinationFile) {
        def aggregatedValue = mapper.readValue(source, Object.class)
        addPair(aggregatedValue, pairs)
        mapper.writeValue(destinationFile, aggregatedValue)
    }

    private def static mingleValues(Map<String, Object> pairs, File destinationFile) {
        def aggregatedValue = new LinkedHashMap<String, Object>()
        addPair(aggregatedValue, pairs)
        mapper.writeValue(destinationFile, aggregatedValue)
    }

    /**
     * Creates the file if it doesn't exist
     * If new pair - creates it, if it was existed - overrides it.
     *
     * @param path
     * @param pairs
     */
    def static overlayFile(File sourceFle, Map<String, Object> pairs, File destinationFile) {
        if (!sourceFle.exists()) {
            sourceFle.createNewFile()
            mingleValues(pairs, destinationFile)
        } else {
            mingleValues(sourceFle, pairs, destinationFile)
        }
    }

    /**
     * Creates the file if it doesn't exist
     * If new pair - creates it, if it was existed - overrides it.
     *
     * @param path
     * @param pairs
     */
    def static overlayFile(File sourceAndDestinationFle, Map<String, Object> pairs) {
        if (!sourceAndDestinationFle.exists()) {
            sourceAndDestinationFle.createNewFile()
            mingleValues(pairs, sourceAndDestinationFle)
        } else {
            mingleValues(sourceAndDestinationFle, pairs, sourceAndDestinationFle)
        }
    }

    /**
     * Expects the resource exists.
     *
     * If new pair - creates it, if it was existed - overrides it.
     *
     * @param path
     * @param pairs
     */
    def static overlayResource(URL resource, Map<String, Object> pairs, File destinationFile) {
        mingleValues(resource, pairs, destinationFile)
    }

    def static readFileKey(File file, String key) {
        return readKey(mapper.readValue(file, Object.class), key)
    }

    def static readTree(Object resource) {
        return mapper.readTree(resource)
    }

    def static writeFileValue(File file, Object value) {
        mapper.writeValue(file, value)
    }
}
