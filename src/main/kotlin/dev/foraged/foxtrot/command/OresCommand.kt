package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*

@AutoRegister
@CommandAlias("ores")
object OresCommand : GoodCommand()
{
    @Default
    @Description("View how many ores a player has mined")
    fun ores(sender: Player, @Default("self") player: UUID)
    {
        sender.sendMessage("${CC.SEC}Ores mined by ${CC.PRI}${ScalaStoreUuidCache.username(player)}")
        FoxtrotExtendedPlugin.oreMaps.forEach {
            sender.sendMessage("${it.displayName} mined: ${CC.WHITE}${it[player] ?: 0}")
        }
    }

    @CommandPermission("foxtrot.ores.management")
    @Subcommand("reset")
    @Description("Reset a players mined ores")
    fun reset(sender: Player, player: UUID)
    {
        FoxtrotExtendedPlugin.oreMaps.forEach { it.reset(player) }
        sender.sendMessage("${CC.GREEN}You have reset the mined ores of ${ScalaStoreUuidCache.username(player)}")
    }

    @CommandPermission("foxtrot.ores.management")
    @Subcommand("set")
    @Description("Set a players mined ores")
    fun set(sender: Player, player: UUID, ore: String, amount: Int)
    {
        val map = FoxtrotExtendedPlugin.oreMaps.firstOrNull { it.mongoName.split(".")[1].equals(ore, true) } ?: throw ConditionFailedException("Persistable ore type not defined with name $ore")
        map[player] = amount
        sender.sendMessage("${CC.GREEN}You have set the mined ${ore.lowercase()}'s of ${ScalaStoreUuidCache.username(player)} to $amount")
    }
}