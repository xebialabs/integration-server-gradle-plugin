package ai.digital.integration.server.common.util

import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.net.URL


class YamlFileUtil {
    companion object {

        private val yamlFactory: YAMLFactory = YAMLFactory.builder().build()

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
                val pair = if (keyItem.contains("[")) {
                    Pair(keyItem.substring(0, keyItem.indexOf("[")), true)
                } else {
                    Pair(keyItem, false)
                }

                val pureKeyItem = pair.first
                val isArray = pair.second

                val value: Any? = current[pureKeyItem]
                if (value == null) {
                    current[pureKeyItem] = LinkedHashMap<String, Any>()
                }

                current = if (isArray) {
                    val keyItemInd = keyItem.substring(keyItem.indexOf("[") + 1, keyItem.indexOf("]")).toInt()
                    val array: ArrayList<MutableMap<String, Any>> =
                        current[pureKeyItem] as ArrayList<MutableMap<String, Any>>
                    array[keyItemInd]
                } else {
                    current[pureKeyItem] as MutableMap<String, Any>
                }
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
            val yamlParser = yamlFactory.createParser(source)
            val aggregatedValue: MappingIterator<MutableMap<String, Any>> =
                mapper.readValues(yamlParser, MutableMap::class.java) as MappingIterator<MutableMap<String, Any>>

            val itemContents = mutableListOf<String>()
            aggregatedValue.forEach { item ->
                addPair(item as MutableMap<String, Any>, pairs)
                itemContents.add(mapper.writeValueAsString(item))
            }

            val fileContent = if (itemContents.size > 1) {
                itemContents.joinToString(prefix = "---\n", separator = "---\n")
            } else {
                itemContents[0]
            }.replace("file: \"", "file: !file \"")

            destinationFile.writeText(
                fileContent
            )
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

        fun readValues(resource: InputStream) {
            val yaml = Yaml()
            val values = yaml.loadAll(resource).toMutableList()
            mapper.writeValue(File("/tmp/multi.yaml"), values)
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
