package com.uni.remote.tech.common.mapper

interface Mapper<E, D> {
    fun mapFromEntity(item: E): D
    fun mapToEntity(item: D): E
}