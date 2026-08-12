package de.project.lukas.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base type for every connected Bluetooth device. State is exposed as [StateFlow]
 * so the Compose UI can collect it lifecycle-aware, independent of any Android view.
 */
abstract class Device {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _battery = MutableStateFlow(0)
    val battery: StateFlow<Int> = _battery.asStateFlow()

    abstract val address: String

    protected fun setName(value: String) {
        _name.value = value
    }

    protected fun setMessage(value: String) {
        _message.value = value
    }

    protected fun setBattery(value: Int) {
        _battery.value = value
    }

    protected fun setConnected(value: Boolean) {
        _isConnected.value = value
    }

    abstract fun disconnect()

    open fun switchOff() {}

    override fun toString(): String = _name.value
}
