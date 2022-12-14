package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB

class BlocksmcFly : FlyMode("Blocksmc") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2.5f, 0f, 5f)

    private var flyable = false
    private val timer = MSTimer()

    override fun onEnable() {
        LiquidBounce.hud.addNotification(
            Notification(
                "Blocksmc",
                "Bypass speed ${speedValue.get()}",
                NotifyType.SUCCESS
            )
        )
        mc.netHandler.addToSendQueue(
            C04PacketPlayerPosition(
                mc.thePlayer.posX,
                mc.thePlayer.posY + 3.35,
                mc.thePlayer.posZ,
                false
            )
        )
        mc.netHandler.addToSendQueue(
            C04PacketPlayerPosition(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                false
            )
        )
        mc.netHandler.addToSendQueue(
            C04PacketPlayerPosition(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                true
            )
        )
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
        flyable = true
        mc.timer.timerSpeed = 1.6f
        fly.launchY += 0.42
        timer.reset()
    }

    override fun onUpdate(event: UpdateEvent) {
        if (timer.hasTimePassed(300)) {
            mc.timer.timerSpeed = 1f
        }

        if (timer.hasTimePassed(1500)) {
            if (flyable) {
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.001f)
            }
            flyable = false
        }

        if (flyable && timer.hasTimePassed(100)) {
            MovementUtils.strafe(speedValue.get())
        } else if (!timer.hasTimePassed(100)) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = true
        }

        if (packet is S08PacketPlayerPosLook) {
            LiquidBounce.hud.addNotification(Notification("Blocksmc", "Oops!Got LAG!", NotifyType.WARNING))
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(
                event.x.toDouble(),
                event.y.toDouble(),
                event.z.toDouble(),
                event.x + 1.0,
                fly.launchY,
                event.z + 1.0
            )
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }
}