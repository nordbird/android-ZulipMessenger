package ru.nordbird.tfsmessenger.data.mapper

interface Mapper<SRC, DST> {
    fun transform(data: SRC): DST
}