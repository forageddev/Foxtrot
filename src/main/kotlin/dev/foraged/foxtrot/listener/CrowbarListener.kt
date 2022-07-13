package dev.foraged.foxtrot.listener

import com.cryptomorin.xseries.XMaterial
import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.event.CrowbarSpawnerBreakEvent
import dev.foraged.foxtrot.server.ServerHandler
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.apache.commons.lang.StringUtils
import org.bukkit.*
import org.bukkit.block.CreatureSpawner
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

@Listeners
object CrowbarListener : Listener
{
    val CROWBAR_NAME = "${CC.B_GOLD}Crowbar"
    val CROWBAR_PORTALS = 6
    val CROWBAR_SPAWNERS = 1

    fun isCrowbar(item: ItemStack) : Boolean { return item.isSimilar(ItemBuilder(XMaterial.IRON_AXE).name(CROWBAR_NAME).build()) }
    fun getCrowbarDescription(portals: Int, spawners: Int) : List<String> { return listOf("", "${CC.YELLOW}Can Break:", "${CC.YELLOW} - ${CC.BLUE}Portal Frames: ${CC.YELLOW}$portals", "${CC.YELLOW} - ${CC.BLUE}Spawners: ${CC.YELLOW}$spawners") }
    fun getCrowbarUsesPortal(item: ItemStack): Int { return ItemUtils.getLoreData(item, 2)?.toInt() ?: 0 }
    fun getCrowbarUsesSpawner(item: ItemStack): Int { return ItemUtils.getLoreData(item, 3)?.toInt() ?: 0 }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent)
    {
        if (event.item == null || !isCrowbar(event.item) || !(event.action == Action.LEFT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_BLOCK)) return

        if (!ServerHandler.isUnclaimedOrRaidable(event.clickedBlock.location) && !ServerHandler.isAdminOverride(event.player))
        {
            val team = LandBoard.getTeam(event.clickedBlock.location)
            if (team != null && (team is PlayerTeam && !team.isMember(event.player.uniqueId)) || team is SystemTeam)
            {
                event.player.sendMessage(
                    ChatColor.YELLOW.toString() + "You cannot crowbar in " + ChatColor.RED + team.getName(
                        event.player
                    ) + ChatColor.YELLOW + "'s territory!"
                )
                return
            }
        }
        if (SystemFlag.SAFE_ZONE.appliesAt(event.clickedBlock.location)) {
            event.player.sendMessage(ChatColor.YELLOW.toString() + "You cannot crowbar spawn!")
            return
        }

        if (event.clickedBlock.type == Material.ENDER_PORTAL_FRAME)
        {
            var portals = getCrowbarUsesPortal(event.item)
            if (portals == 0)
            {
                event.player.sendMessage(ChatColor.RED.toString() + "This crowbar has no more uses on end portals!")
                return
            }
            event.clickedBlock.world.playEffect(
                event.clickedBlock.location,
                Effect.STEP_SOUND,
                event.clickedBlock.typeId
            )
            event.clickedBlock.type = Material.AIR
            event.clickedBlock.state.update()
            event.clickedBlock.world.dropItemNaturally(
                event.clickedBlock.location,
                ItemStack(Material.ENDER_PORTAL_FRAME)
            )
            event.clickedBlock.world.playSound(event.clickedBlock.location, Sound.ANVIL_USE, 1.0f, 1.0f)
            for (x in -3..2)
            {
                for (z in -3..2)
                {
                    val block = event.clickedBlock.location.add(x.toDouble(), 0.0, z.toDouble()).block
                    if (block.type == Material.ENDER_PORTAL)
                    {
                        block.type = Material.AIR
                        block.world.playEffect(block.location, Effect.STEP_SOUND, Material.ENDER_PORTAL.id)
                    }
                }
            }
            portals -= 1
            if (portals == 0)
            {
                event.player.itemInHand = null
                event.clickedBlock.location.world.playSound(event.clickedBlock.location, Sound.ITEM_BREAK, 1.0f, 1.0f)
                return
            }
            val meta = event.item.itemMeta
            meta.lore = getCrowbarDescription(portals, 0)
            event.item.itemMeta = meta
            val max = Material.DIAMOND_HOE.maxDurability.toDouble()
            val dura = max / CROWBAR_PORTALS.toDouble() * portals
            event.item.durability = (max - dura).toInt().toShort()
            event.player.itemInHand = event.item
        } else if (event.clickedBlock.type == Material.MOB_SPAWNER)
        {
            val spawner = event.clickedBlock.state as CreatureSpawner
            var spawners: Int = getCrowbarUsesSpawner(event.item)
            if (spawners == 0)
            {
                event.player.sendMessage("${CC.RED}This crowbar has no more uses on mob spawners!")
                return
            }
            if (event.clickedBlock.world.environment != World.Environment.NORMAL)
            {
                event.player.sendMessage("${CC.RED}You cannot break spawners in this dimension.")
                event.isCancelled = true
                return
            }

            val crowbarSpawnerBreakEvent = CrowbarSpawnerBreakEvent(event.player, event.clickedBlock)
            Bukkit.getServer().pluginManager.callEvent(crowbarSpawnerBreakEvent)
            if (crowbarSpawnerBreakEvent.isCancelled) return

            event.clickedBlock.location.world.playEffect(
                event.clickedBlock.location,
                Effect.STEP_SOUND,
                event.clickedBlock.typeId
            )
            event.clickedBlock.type = Material.AIR
            event.clickedBlock.state.update()
            val drop = ItemStack(Material.MOB_SPAWNER)
            var meta = drop.itemMeta
            meta.displayName = ChatColor.RESET.toString() + StringUtils.capitaliseAllWords(
                spawner.spawnedType.toString().lowercase().replace("_".toRegex(), " ")
            ) + " Spawner"
            drop.itemMeta = meta
            event.clickedBlock.location.world.dropItemNaturally(event.clickedBlock.location, drop)
            event.clickedBlock.location.world.playSound(event.clickedBlock.location, Sound.ANVIL_USE, 1.0f, 1.0f)
            spawners -= 1
            if (spawners == 0)
            {
                event.player.itemInHand = null
                event.clickedBlock.location.world.playSound(event.clickedBlock.location, Sound.ITEM_BREAK, 1.0f, 1.0f)
                return
            }
            meta = event.item.itemMeta
            meta.lore = getCrowbarDescription(0, spawners)
            event.item.itemMeta = meta
            val max = Material.DIAMOND_HOE.maxDurability.toDouble()
            val dura = max / CROWBAR_SPAWNERS.toDouble() * spawners
            event.item.durability = (max - dura).toInt().toShort()
            event.player.itemInHand = event.item
        } else
        {
            event.player.sendMessage(ChatColor.RED.toString() + "Crowbars can only break end portals and mob spawners!")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event.player.world.environment == World.Environment.NETHER && event.block.type == Material.MOB_SPAWNER) {
            event.player.sendMessage(ChatColor.RED.toString() + "You cannot break spawners in this dimension!")
            event.isCancelled = true
        } else if (event.block.type == Material.MOB_SPAWNER) {
            event.player.sendMessage(ChatColor.RED.toString() + "You must use a crowbar to break spawners.")
            event.isCancelled = true
        }
    }
}