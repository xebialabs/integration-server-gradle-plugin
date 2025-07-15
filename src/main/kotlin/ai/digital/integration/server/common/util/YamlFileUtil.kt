package ai.digital.integration.server.common.util

import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit


class YamlFileUtil {
    companion object {

        private val yamlFactory: YAMLFactory = YAMLFactory.builder().build()

        private fun createMapper(minimizeQuotes: Boolean): ObjectMapper {
            val factory = YAMLFactory.builder()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            if (minimizeQuotes) {
                factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            }
            return ObjectMapper(factory.build())
        }

        // Creates different key chain, depends on what is the first key, contains a dot or not for a first key.
        private fun calcKeyChain(objectMap: MutableMap<String, Any>, tokens: List<String>): Array<String> {
            return if (tokens.size == 1) {
                emptyArray()
            } else if (objectMap[tokens[0] + "." + tokens[1]] != null) {
                val keyChain = arrayOf(tokens[0] + "." + tokens[1])
                keyChain.plus(tokens.drop(2).dropLast(1))
            } else {
                val keyChain = arrayOf(tokens[0])
                keyChain.plus(tokens.drop(1).dropLast(1))
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun getKeyParentAndLastToken(
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
                    current[pureKeyItem] =
                        if (isArray) ArrayList<MutableMap<String, Any>>() else LinkedHashMap<String, Any>()
                }

                current = if (isArray) {
                    val keyItemInd = keyItem.substring(keyItem.indexOf("[") + 1, keyItem.indexOf("]")).toInt()
                    val array: ArrayList<MutableMap<String, Any>> =
                        current[pureKeyItem] as ArrayList<MutableMap<String, Any>>
                    while (array.size <= keyItemInd) {
                        array.add(LinkedHashMap())
                    }
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
        private fun mingleValues(source: URL, pairs: MutableMap<String, Any>, destinationFile: File, minimizeQuotes: Boolean = true) {
            val mapper = createMapper(minimizeQuotes)
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
            }.replace("file: ", "file: !file ")

            destinationFile.writeText(
                fileContent
            )
        }

        private fun mingleValues(pairs: MutableMap<String, Any>, destinationFile: File, minimizeQuotes: Boolean = true) {
            val mapper = createMapper(minimizeQuotes)
            val aggregatedValue = LinkedHashMap<String, Any>()
            addPair(aggregatedValue, pairs)
            mapper.writeValue(destinationFile, aggregatedValue)
        }

        private fun mingleValuesWithYq(pairs: MutableMap<String, Any>, destinationFile: File) {
            pairs
                .map { entry ->
                    val key = if (entry.key.startsWith("(")) {
                        entry.key
                    } else {
                        "." + entry.key
                    }
                    Pair(key, entry.value)
                }
                .forEach { pair ->
                    if (pair.second is Collection<*>) {
                        val values = pair.second as Collection<*>
                        val value = values.joinToString(",", "[", "]") { entry -> getYqSimpleValue(entry) }
                        val process = ProcessBuilder(arrayListOf("yq", "-i", "${pair.first} = $value", destinationFile.absolutePath))
                            .inheritIO()
                            .start()
                        checkProcess(process)
                    } else if (pair.second is Array<*>) {
                        val values = pair.second as Array<*>
                        val value = values.joinToString(",", "[", "]") { entry -> getYqSimpleValue(entry) }
                        val process = ProcessBuilder(arrayListOf("yq", "-i", "${pair.first} = $value", destinationFile.absolutePath))
                            .inheritIO()
                            .start()
                        checkProcess(process)
                    } else if (pair.second is Map<*, *>) {
                        val map = pair.second as Map<*, *>
                        map.forEach{entry ->
                            val value = getYqSimpleValue(entry.value)
                            val process = ProcessBuilder(arrayListOf("yq", "-i", "${pair.first}.\"${entry.key}\" = $value", destinationFile.absolutePath))
                                .inheritIO()
                                .start()
                            checkProcess(process)
                        }
                    } else {
                        val value = getYqSimpleValue(pair.second)
                        val process = ProcessBuilder(arrayListOf("yq", "-i", "${pair.first} = $value", destinationFile.absolutePath))
                            .inheritIO()
                            .start()
                        checkProcess(process)
                    }
                }
        }

        private fun checkProcess(process: Process) {
            val result = process.waitFor(60, TimeUnit.SECONDS)
            if (!result || process.exitValue() != 0) {
                throw IllegalArgumentException("Failed to run yq with ${process.exitValue()}")
            }
        }

        private fun getYqSimpleValue(simpleValue: Any?): String {
            return if (simpleValue is Number || simpleValue is Boolean) {
                simpleValue.toString()
            } else if (simpleValue == null) {
                "null"
            } else if (simpleValue is String) {
                "\"${simpleValue}\""
            } else {
                throw IllegalArgumentException("not supported value $simpleValue")
            }
        }

        /**
         * Creates the file if it doesn't exist
         * If new pair - creates it, if it was existed - overrides it.
         *
         * @param sourceFle
         * @param pairs
         * @param destinationFile
         */
        fun overlayFileWithJackson(sourceFile: File, pairs: MutableMap<String, Any>, destinationFile: File = sourceFile, minimizeQuotes: Boolean = true) {
            if (!sourceFile.exists()) {
                sourceFile.createNewFile()
                mingleValues(pairs, destinationFile, minimizeQuotes)
            } else {
                mingleValues(sourceFile.toURI().toURL(), pairs, destinationFile, minimizeQuotes)
            }
        }

        fun overlayFile(sourceFile: File, pairs: MutableMap<String, Any>, destinationFile: File = sourceFile, minimizeQuotes: Boolean = true) {
            if (!sourceFile.exists()) {
                sourceFile.createNewFile()
                mingleValues(pairs, destinationFile, minimizeQuotes)
            } else {
                mingleValues(sourceFile.toURI().toURL(), pairs, destinationFile, minimizeQuotes)
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun readFileKey(file: File, key: String, minimizeQuotes: Boolean = true): Any? {
            val mapper = createMapper(minimizeQuotes)
            return readKey(mapper.readValue(file, MutableMap::class.java) as MutableMap<String, Any>, key)
        }

        fun readTree(resource: InputStream, minimizeQuotes: Boolean = true): TreeNode {
            val mapper = createMapper(minimizeQuotes)
            return mapper.readTree(resource)
        }

        fun writeFileValue(sourceFle: File, value: Any, minimizeQuotes: Boolean = true) {
            val mapper = createMapper(minimizeQuotes)
            if (!sourceFle.exists()) {
                File(sourceFle.parent).mkdirs()
                sourceFle.createNewFile()
            }
            mapper.writeValue(sourceFle, value)
        }
    }
}
