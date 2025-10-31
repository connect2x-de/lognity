package net.folivo.lognity.api.logger

class ContextBuilder @PublishedApi internal constructor() {
    private val values: HashMap<Context.Key<*>, HashSet<Context.Element>> = HashMap()

    fun valuesFrom(context: Context) {
        context.fold(Unit) { _, element ->
            values.getOrPut(element.key) { HashSet() } += element
        }
    }

    fun values(values: Map<Context.Key<*>, Collection<Context.Element>>) {
        this.values += values.mapValues { (_, elements) -> elements.toHashSet() }
    }

    fun <T : Context.Element?> value(key: Context.Key<T>, value: T) {
        values.getOrPut(key) { HashSet() } += value ?: return
    }

    operator fun <T : Context.Element?> set(key: Context.Key<T>, value: T) = value(key, value)

    @PublishedApi
    internal fun build(): Context = DefaultContext(values)
}

typealias ContextSpec = ContextBuilder.() -> Unit

inline fun context(spec: ContextSpec): Context = ContextBuilder().apply(spec).build()