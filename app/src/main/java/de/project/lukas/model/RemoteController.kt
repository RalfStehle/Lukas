package de.project.lukas.model

/**
 * Strategy that maps a physical remote's three buttons (up / down / middle) onto an action on a
 * target device. This is what makes the app a mediator: a remote can drive any connected device.
 */
interface RemoteController {
    fun up(remote: Remote)
    fun down(remote: Remote)
    fun middle(remote: Remote)

    /** Snapshot label for the selection dropdowns. */
    val displayName: String

    companion object {
        val Noop: RemoteController = NoopController
    }
}

/** Does nothing; used when a remote button is unbound. */
object NoopController : RemoteController {
    override fun up(remote: Remote) {}
    override fun down(remote: Remote) {}
    override fun middle(remote: Remote) {}
    override val displayName: String get() = "None"
}

class MotorController(private val device: TrainHub) : RemoteController {
    override fun up(remote: Remote) = device.motorFaster()
    override fun down(remote: Remote) = device.motorSlower()
    override fun middle(remote: Remote) = device.motorStop()
    override val displayName: String get() = "Motor ${device.name.value}"
}

class LightController(private val device: TrainHub) : RemoteController {
    override fun up(remote: Remote) = device.lightBrighter()
    override fun down(remote: Remote) {
        device.setLedColorHub()
        remote.setLedColorRemote(device.currentColor)
    }

    override fun middle(remote: Remote) = device.lightOff()
    override val displayName: String get() = "Light ${device.name.value}"
}

class BaseMotorController(private val device: TrainBase) : RemoteController {
    override fun up(remote: Remote) = device.motorFaster()
    override fun down(remote: Remote) = device.motorSlower()
    override fun middle(remote: Remote) = device.motorStop()
    override val displayName: String get() = "Motor ${device.name.value}"
}

class BaseLightController(private val device: TrainBase) : RemoteController {
    override fun up(remote: Remote) {}
    override fun down(remote: Remote) {}
    override fun middle(remote: Remote) = device.setLedColorHub()
    override val displayName: String get() = "Light ${device.name.value}"
}

class SwitchController(private val device: Switch) : RemoteController {
    override fun up(remote: Remote) = device.toggle1()
    override fun down(remote: Remote) = device.toggle2()
    override fun middle(remote: Remote) {}
    override val displayName: String get() = "Switch ${device.name.value}"
}
