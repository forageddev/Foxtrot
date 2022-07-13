package dev.foraged.foxtrot.map.cooldown

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.CooldownPersistMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

@RegisterMap
@Listeners
object OpplePersistableMap : CooldownPersistMap("OppleCooldowns", "OppleCooldown", true), Listener {

    @EventHandler
    fun onConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type != Material.GOLDEN_APPLE) return
        if (event.item.durability.toInt() == 0) return

        val player = event.player
        val cooldown = getCooldown(player.uniqueId)

        if (cooldown > System.currentTimeMillis()) {
            event.isCancelled = true
            player.sendMessage("${CC.RED}You cannot use this for another ${CC.BOLD}${TimeUtil.formatIntoDetailedString(((cooldown - System.currentTimeMillis()) / 1000).toInt())}")
            return
        }

        val duration = 2 * 60 * 60L // TODO; make these values configurable
        startCooldown(player.uniqueId, duration)

        player.sendMessage(CC.DARK_GREEN + "███" + CC.BLACK + "██" + CC.DARK_GREEN + "███")
        player.sendMessage(CC.DARK_GREEN + "███" + CC.BLACK + "█" + CC.DARK_GREEN + "████")
        player.sendMessage(CC.DARK_GREEN + "██" + CC.GOLD + "████" + CC.DARK_GREEN + "██" + CC.GOLD + " Super Golden Apple:")
        player.sendMessage(CC.DARK_GREEN + "█" + CC.GOLD + "██" + CC.WHITE + "█" + CC.GOLD + "███" + CC.DARK_GREEN + "█" + CC.DARK_GREEN + "   Consumed")
        player.sendMessage(CC.DARK_GREEN + "█" + CC.GOLD + "█" + CC.WHITE + "█" + CC.GOLD + "████" + CC.DARK_GREEN + "█" + CC.YELLOW + " Cooldown Remaining:")
        player.sendMessage(
            CC.DARK_GREEN + "█" + CC.GOLD + "██████" + CC.DARK_GREEN + "█" + CC.BLUE + "   " + TimeUtil.formatIntoDetailedString(
                duration.toInt()
            )
        )
        player.sendMessage(CC.DARK_GREEN + "█" + CC.GOLD + "██████" + CC.DARK_GREEN + "█")
        player.sendMessage(CC.DARK_GREEN + "██" + CC.GOLD + "████" + CC.DARK_GREEN + "██")
    }
}