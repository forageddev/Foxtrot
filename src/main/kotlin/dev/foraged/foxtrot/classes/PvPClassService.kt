package dev.foraged.foxtrot.classes

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.classes.impl.ArcherClass
import dev.foraged.foxtrot.classes.impl.MinerClass
import dev.foraged.foxtrot.classes.impl.RogueClass
import dev.foraged.foxtrot.event.BardRestoreEvent
import gg.scala.flavor.service.Service
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

@Service
@Listeners
object PvPClassService : Listener
{
    val equippedKits = hashMapOf<String, PvPClass>()
    val savedPotions = hashMapOf<UUID, PvPClass.SavedPotion>()
    val classes = listOf(MinerClass, ArcherClass, RogueClass)

    fun getPvPClass(player: Player): PvPClass?
    {
        return if (equippedKits.containsKey(player.name)) equippedKits[player.name] else null
    }

    fun hasKitOn(player: Player, pvpClass: PvPClass): Boolean
    {
        return equippedKits.containsKey(player.name) && equippedKits[player.name] === pvpClass
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent)
    {
        if (equippedKits.containsKey(event.player.name)) {
            equippedKits[event.player.name]!!.remove(event.player)
            equippedKits.remove(event.player.name)
        }
    }

    @EventHandler
    fun onPlayerKick(event: PlayerKickEvent)
    {
        if (equippedKits.containsKey(event.player.name)) {
            equippedKits[event.player.name]!!.remove(event.player)
            equippedKits.remove(event.player.name)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent)
    {
        if (equippedKits.containsKey(event.player.name)) {
            equippedKits[event.player.name]!!.remove(event.player)
            equippedKits.remove(event.player.name)
        }
        for (potionEffect in event.player.activePotionEffects) if (potionEffect.duration > 1000000) event.player.removePotionEffect(potionEffect.type)
    }

    fun checkSavedPotions()
    {
        val idIterator: MutableIterator<Map.Entry<UUID, PvPClass.SavedPotion>> = savedPotions.entries.iterator()
        while (idIterator.hasNext())
        {
            val (key, value) = idIterator.next()
            val player = Bukkit.getPlayer(key)
            if (player != null && player.isOnline) {
                BardRestoreEvent(player, value).call()
                if (value.time < System.currentTimeMillis() && !value.perm) {
                    if (player.hasPotionEffect(value.potionEffect.type)) {
                        player.activePotionEffects.forEach {
                            val restore = value.potionEffect
                            if (it.type === restore.type && it.duration < restore.duration && it.amplifier <= restore.amplifier) player.removePotionEffect(restore.type)
                        }
                    }
                    if (player.addPotionEffect(value.potionEffect, true)) {
                        Bukkit.getLogger().info(value.potionEffect.type.name + ", " + value.potionEffect.duration + ", " + value.potionEffect.amplifier)
                        idIterator.remove()
                    }
                }
            } else idIterator.remove()
        }
    }
}