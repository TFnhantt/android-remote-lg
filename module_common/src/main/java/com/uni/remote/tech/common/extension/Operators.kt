package com.uni.remote.tech.common.extension

fun <T : Any, U : Any> List<T>.joinBy(
    collection: List<U>,
    filter: (Pair<T, U>) -> Boolean
): List<Pair<T, U?>> = map { t ->
    val filtered = collection.firstOrNull { filter(Pair(t, it)) }
    Pair(t, filtered)
}