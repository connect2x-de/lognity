/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.format

import net.folivo.lognity.util.MultiMap

/**
 * A basic deterministic finite automaton for matching a sliding window of characters
 * against a list of substrings using a decision tree.
 */
internal class DFA {
    private sealed interface Node

    // An intermediary (or connecting) node with at least 1 incoming and one outgoing connection
    private data class INode(val children: MultiMap<Char, Node> = MultiMap()) : Node {
        fun getOrCreateChildren(key: Char): MutableList<Node> = children.getOrPut(key) {
            ArrayList()
        }
    }

    // A terminating node, which contains the final value that has been matched
    private data class TNode(val value: String) : Node

    private val matches: HashSet<String> = HashSet()
    private val rootNode: INode = INode()
    private var currentNode: INode = rootNode

    fun addMatch(value: String) {
        if (value in matches) return
        var currentNode = rootNode
        for (charIndex in value.indices) {
            val char = value[charIndex]
            // If this is the last char index, we create a terminating node
            if (charIndex == value.lastIndex) {
                currentNode.getOrCreateChildren(char) += TNode(value)
                break
            }
            // Otherwise we know this is an intermediary node
            val childNodes = currentNode.getOrCreateChildren(char)
            currentNode = childNodes.find { node -> node is INode }?.let { node -> node as INode }
                ?: INode().apply { childNodes += this }
        }
        matches += value
    }

    operator fun plus(value: String): DFA {
        addMatch(value)
        return this
    }

    operator fun plusAssign(value: String) {
        addMatch(value)
    }

    fun reset() {
        currentNode = rootNode
    }

    fun next(char: Char): String? {
        val children = currentNode.children[char]
        if (children == null) {
            // When there's no child nodes anymore, we hit a dead end, reset completely
            reset()
            return null
        }
        // If the current list of children contains a terminating node, we found a match
        children.find { node -> node is TNode }?.let { node ->
            reset()
            return (node as TNode).value
        }
        // Otherwise we need to keep looking deeper
        currentNode = children.first { node -> node is INode } as INode
        return null
    }
}