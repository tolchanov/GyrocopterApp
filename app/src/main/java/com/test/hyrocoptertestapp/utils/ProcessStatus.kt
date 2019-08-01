package com.test.hyrocoptertestapp.utils

enum class Status{
    LOADING,
    SUCCESS,
    ERROR
}

data class ProcessStatus(
        val status: Status,
        val message: String? = null
)