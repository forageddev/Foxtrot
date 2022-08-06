package dev.foraged.foxtrot.game.supply

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.google.gson.reflect.TypeToken
import dev.foraged.commons.persist.PluginService
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.team.claim.LandBoard
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

@Service
object SupplyDropService : PluginService
{

    private val supplyDropContents: File = File(FoxtrotExtendedPlugin.instance.dataFolder, "supplyDropContents.json")
    var contents = mutableListOf<ItemStack>()
    val existingSupplyDrops = mutableListOf<Location>()

    @Configure
    override fun configure()
    {
        if (supplyDropContents.exists()) {
            Files.newReader(supplyDropContents, Charsets.UTF_8).use { reader ->
                contents = Serializers.gson.fromJson(reader, TypeToken.getParameterized(MutableList::class.java, ItemStack::class.java).type) as MutableList<ItemStack>
            }
        }
    }

    @Close
    fun close() {
        existingSupplyDrops.forEach(SupplyDropService::removeSupplyDrop)

        try {
            Files.write(Serializers.gson.toJson(contents), supplyDropContents, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            FoxtrotExtendedPlugin.instance.logger.severe("Failed to save suppplyDropContents.json!")
        }
    }

    fun summonSupplyDrop(announce: Boolean = false) {
        var x = 0
        var z = 0

        while (abs(x) <= 100) x = Random.nextInt(1000) - 500
        while (abs(z) <= 100) z = Random.nextInt(1000) - 500

        while (LandBoard.getTeam(Location(Bukkit.getWorld("world"), x.toDouble(), 100.0, z.toDouble())) != null) {
            x = 0
            z = 0

            while (abs(x) <= 100) x = Random.nextInt(1000) - 500
            while (abs(z) <= 100) z = Random.nextInt(1000) - 500
        }

        val y = Bukkit.getWorld("world").getHighestBlockYAt(x, z)
        var block = Bukkit.getWorld("world").getBlockAt(x, y, z)
        if (block == null) {
            summonSupplyDrop()
            return
        }

        block = block.getRelative(BlockFace.UP)
        block.type = Material.ENDER_CHEST
        block.setMetadata("SupplyDrop", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))

        existingSupplyDrops.add(block.location)
        if (announce) Bukkit.broadcastMessage("${CC.B_PRI}[Meteor] ${CC.SEC}A meteor has fallen at ${CC.PRI}$x, $z${CC.SEC} be the first to get there.")
        Tasks.delayed(10 * 60 * 20) {
            removeSupplyDrop(block.location, true)
        }
    }

    fun removeSupplyDrop(location: Location, announce: Boolean = false) {
        if (location in existingSupplyDrops) {
            location.block.type = Material.AIR
            location.block.removeMetadata("SupplyDrop", FoxtrotExtendedPlugin.instance)
            if (announce) Bukkit.broadcastMessage("${CC.B_PRI}[Meteor] ${CC.SEC}The meteor that fell at ${CC.PRI}${location.blockX}, ${location.blockZ}${CC.SEC} has burnt away.")
        }
    }
}