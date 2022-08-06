package dev.foraged.foxtrot.game.supply

import com.google.common.collect.Sets
import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import me.lucko.helper.Helper.world
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random


@Listeners
object SupplyDropListener : Listener
{
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (event.clickedBlock == null || !event.clickedBlock.hasMetadata("SupplyDrop")) return

        val toGive = Sets.newHashSet<ItemStack>()
        while (toGive.size < 4) toGive.add(SupplyDropService.contents[Random.nextInt(SupplyDropService.contents.size)])


        for (item in toGive) event.player.world.dropItemNaturally(event.clickedBlock.location, item)

        event.clickedBlock.removeMetadata("SupplyDrop", FoxtrotExtendedPlugin.instance)
        event.clickedBlock.type = Material.AIR
        event.isCancelled = true
        Bukkit.broadcastMessage("${CC.B_PRI}[Meteor] ${CC.SEC}The meteor at ${CC.PRI}${event.clickedBlock.location.blockX}, ${event.clickedBlock.location.blockZ}${CC.SEC} has been broken apart.")
    }
}