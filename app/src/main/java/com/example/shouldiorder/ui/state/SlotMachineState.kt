package com.example.shouldiorder.ui.state

/**
 * État de la Slot Machine
 */
sealed class SlotMachineState {
    data object Idle : SlotMachineState()
    data object Spinning : SlotMachineState()
    data class Result(val message: String) : SlotMachineState()
}

