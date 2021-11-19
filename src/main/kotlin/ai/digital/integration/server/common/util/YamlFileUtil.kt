package ai.digital.integration.server.common.util

import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import java.io.File
import java.io.InputStream
import java.net.URL

class YamlFileUtil {
    companion object {

        private val mapper: ObjectMapper = ObjectMapper(
            YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        )

        // Creates different key chain, depends on what is the first key, contains a dot or not for a first key.
        private fun calcKeyChain(objectMap: MutableMap<String, Any>, tokens: List<String>): Array<String> {
            return if (objectMap[tokens[0] + "." + tokens[1]] != null) {
                val keyChain = arrayOf(tokens[0] + "." + tokens[1])
                keyChain.plus(tokens.drop(2).dropLast(1))
            } else {
                val keyChain = arrayOf(tokens[0])
                keyChain.plus(tokens.drop(1).dropLast(1))
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun getKeyParentAndLastToken(
            objectMap: MutableMap<String, Any>,
            key: String
        ): Pair<MutableMap<String, Any>, String> {
            val tokens: List<String> = key.split(".")

            val keyChain = calcKeyChain(objectMap, tokens)

            var current: MutableMap<String, Any> = objectMap
            keyChain.forEach { keyItem ->
                val value: Any? = current[keyItem]
                if (value == null) {
                    current[keyItem] = LinkedHashMap<String, Any>()
                }
                current = current[keyItem] as MutableMap<String, Any>
            }
            return Pair(current, tokens.last())
        }

        private fun readKey(objectMap: MutableMap<String, Any>, key: String): Any? {
            val pair = getKeyParentAndLastToken(objectMap, key)
            return pair.first[pair.second]
        }

        private fun addPair(initial: MutableMap<String, Any>, newPairs: MutableMap<String, Any>) {
            newPairs.forEach { pair ->
                val p = getKeyParentAndLastToken(initial, pair.key)
                p.first[p.second] = pair.value
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun mingleValues(source: URL, pairs: MutableMap<String, Any>, destinationFile: File) {
            val aggregatedValue: MutableMap<String, Any> =
                mapper.readValue(source, MutableMap::class.java) as MutableMap<String, Any>

            addPair(aggregatedValue, pairs)
            mapper.writeValue(destinationFile, aggregatedValue)
        }

        private fun mingleValues(pairs: MutableMap<String, Any>, destinationFile: File) {
            val aggregatedValue = LinkedHashMap<String, Any>()
            addPair(aggregatedValue, pairs)
            mapper.writeValue(destinationFile, aggregatedValue)
        }


        /**
         * Creates the file if it doesn't exist
         * If new pair - creates it, if it was existed - overrides it.
         *
         * @param sourceFle
         * @param pairs
         * @param destinationFile
         */
        fun overlayFile(sourceFle: File, pairs: MutableMap<String, Any>, destinationFile: File) {
            if (!sourceFle.exists()) {
                sourceFle.createNewFile()
                mingleValues(pairs, destinationFile)
            } else {
                mingleValues(sourceFle.toURI().toURL(), pairs, destinationFile)
            }
        }

        /**
         * Creates the file if it doesn't exist
         * If new pair - creates it, if it was existed - overrides it.
         *
         * @param sourceAndDestinationFle
         * @param pairs
         */

        fun overlayFile(sourceAndDestinationFle: File, pairs: MutableMap<String, Any>) {
            if (!sourceAndDestinationFle.exists()) {
                sourceAndDestinationFle.createNewFile()
                mingleValues(pairs, sourceAndDestinationFle)
            } else {
                mingleValues(sourceAndDestinationFle.toURI().toURL(), pairs, sourceAndDestinationFle)
            }
        }

        /**
         * Expects the resource exists.
         *
         * If new pair - creates it, if it was existed - overrides it.
         *
         * @param resource
         * @param pairs
         * @param destinationFile
         */
        fun overlayResource(resource: URL, pairs: MutableMap<String, Any>, destinationFile: File) {
            return mingleValues(resource, pairs, destinationFile)
        }

        @Suppress("UNCHECKED_CAST")
        fun readFileKey(file: File, key: String): Any? {
            return readKey(mapper.readValue(file, MutableMap::class.java) as MutableMap<String, Any>, key)
        }

        fun readTree(resource: InputStream): TreeNode {
            return mapper.readTree(resource)
        }

        fun writeFileValue(sourceFle: File, value: Any) {
            if (!sourceFle.exists()) {
                File(sourceFle.parent).mkdirs()
                sourceFle.createNewFile()
            }
            mapper.writeValue(sourceFle, value)
        }
    }
}
