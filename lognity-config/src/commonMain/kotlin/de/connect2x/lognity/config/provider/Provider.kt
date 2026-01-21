package de.connect2x.lognity.config.provider

interface Provider<T> {
    companion object {
        inline fun <reified T : Any> named(name: String, value: T): Provider<T> = object : Provider<T> {
            override val name: String = name
            override val value: T = value
        }
    }

    val name: String
    val value: T
}