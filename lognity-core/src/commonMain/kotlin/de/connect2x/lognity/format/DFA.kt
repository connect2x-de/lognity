package de.connect2x.lognity.format

import de.connect2x.lognity.util.MultiMap

/**
 * A very small deterministic finite automaton (DFA) for matching a predefined set of strings
 * character by character.
 *
 * Usage pattern:
 * - Add one or more strings to match using [addMatch], or the operator helpers [plus] and [plusAssign].
 * - Feed incoming characters sequentially through [next].
 * - When a full match is detected, [next] returns the matched string and the DFA resets to the start state.
 * - If the current path is invalid (no transition for a character), the DFA resets to the start state and returns null.
 *
 * Notes:
 * - Matching is case-sensitive.
 * - After a successful match or a dead end, the internal state is reset (see [reset]).
 */
class DFA {
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

    /**
     * Registers a new string that should be detected by the DFA.
     *
     * If the same value is added multiple times, subsequent calls are ignored.
     *
     * @param value the exact string to match
     */
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

    /**
     * Adds a new matchable [value] and returns this DFA to enable chaining.
     *
     * Example: `dfa + "INFO" + "WARN"`
     *
     * @param value the string to add
     * @return this instance, for fluent chaining
     */
    operator fun plus(value: String): DFA {
        addMatch(value)
        return this
    }

    /**
     * Adds a new matchable [value] to this DFA in-place.
     *
     * Example: `dfa += "ERROR"`
     *
     * @param value the string to add
     */
    operator fun plusAssign(value: String) {
        addMatch(value)
    }

    /**
     * Resets the DFA to its start state, discarding any progress of a partially matched value.
     */
    fun reset() {
        currentNode = rootNode
    }

    /**
     * Consumes the next input [char] and advances the DFA.
     *
     * Behavior:
     * - If the transition exists and completes a registered match, the matched string is returned and the DFA resets.
     * - If the transition exists but does not complete a match yet, null is returned and the DFA waits for further input.
     * - If no transition exists for [char] in the current state, the DFA resets and returns null.
     *
     * @param char the next input character
     * @return the matched string if a match completes with this character; otherwise null
     */
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