package ru.nordbird.tfsmessenger.data.mapper.base

interface Mapper<SRC, DST> {
    fun transform(data: SRC): DST
}