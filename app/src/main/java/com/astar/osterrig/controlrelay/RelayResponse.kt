package com.astar.osterrig.controlrelay

sealed class RelayResponse {

    enum class State { OPEN, CLOSE }

    data class RelayState(val state: State) : RelayResponse()

    data class CorrectPassword(val address: String): RelayResponse()

    data class ErrorPassword(val address: String): RelayResponse()
}
