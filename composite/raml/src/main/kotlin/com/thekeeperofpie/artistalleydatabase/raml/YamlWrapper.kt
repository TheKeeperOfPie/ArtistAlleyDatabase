package com.thekeeperofpie.artistalleydatabase.raml

import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.yamlList
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import com.charleskorn.kaml.yamlTaggedNode

data class YamlWrapper(val yamlNode: YamlNode) {

    val entries by lazy {
        yamlNode.yamlMap.entries
            .map { (key, value) -> key.content to YamlWrapper(value) }
            .associate { it }
    }

    val items by lazy { yamlNode.yamlList.items.map(::YamlWrapper) }

    val innerNode by lazy { YamlWrapper(yamlNode.yamlTaggedNode.innerNode) }

    operator fun get(key: String) = yamlNode.yamlMap.get<YamlNode>(key)?.let(::YamlWrapper)

    operator fun get(vararg keys: String): YamlWrapper? {
        val node = keys.dropLast(1)
            .fold(yamlNode) { node, key -> node.yamlMap[key]!! }
        return node.yamlMap.get<YamlNode>(keys.last())?.let(::YamlWrapper)
    }

    fun asString() = yamlNode.yamlScalar.content

    fun asBoolean() = yamlNode.yamlScalar.toBoolean()
}