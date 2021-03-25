package ru.nordbird.tfsmessenger.data.model

data class Resource<out T>(val status: Status, val data: T? = null) {

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data)
        }

        fun <T> error(): Resource<T> {
            return Resource(Status.ERROR)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING)
        }

    }

}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}