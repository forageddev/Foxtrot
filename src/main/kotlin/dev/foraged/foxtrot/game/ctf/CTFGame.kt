package dev.foraged.foxtrot.game.ctf

import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.game.Game
import dev.foraged.foxtrot.hologram.FoxtrotHologram
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.entity.Zombie
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CTFGame(
    name: String,
    var initialLocation: Location
) : Game(name = name)
{
    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[CaptureTheFlag] "
    }

    init {
        initialLocation.yaw = 0f
        initialLocation.pitch = 0f
    }

    var startTime = 0L
    var flagHolder: Player? = null
    override var active: Boolean = false

    var holdingEntity: Villager? = null

    override fun start() {
        startTime = System.currentTimeMillis()
        active = true

        holdingEntity = initialLocation.world.spawnEntity(initialLocation, EntityType.VILLAGER) as Villager
        holdingEntity!!.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0))
        holdingEntity!!.setBaby()
        holdingEntity!!.ageLock = true
        holdingEntity!!.equipment.helmet = ItemBuilder.of(Material.BANNER).color(DyeColor.PINK.color).glow().build()
        holdingEntity!!.setMetadata("GameFlag", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))

        (holdingEntity as CraftEntity).handle.also {
            val comp = NBTTagCompound()
            it.c(comp)
            comp.setByte("NoAI", 1)
            it.f(comp)
            it.b(true)
        }
    }

    override fun stop(winner: Player?)
    {
        if (winner != null) {
            Bukkit.broadcastMessage("")
            Bukkit.broadcastMessage("${CC.B_PRI}The flag $name has been captured!")
            Bukkit.broadcastMessage("${CC.SEC}The flag was captured by ${CC.PRI}${winner.name}${CC.SEC}.")
            Bukkit.broadcastMessage("")
            Bukkit.broadcastMessage("${CC.SEC}The event was active for ${CC.PRI}${TimeUtil.formatIntoDetailedString(((System.currentTimeMillis() - startTime) / 1000).toInt())}${CC.SEC}.")
            Bukkit.broadcastMessage("")
        }

        holdingEntity!!.remove()
        flagHolder = null
        holdingEntity = null
        active = false
        startTime = 0L
    }
}