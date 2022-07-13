package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*

@AutoRegister
@CommandAlias("stats")
object StatsCommand : GoodCommand()
{
    @Default
    fun ores(sender: Player, player: UUID)
    {
        sender.sendMessage("${CC.SEC}Stats for ${CC.PRI}${ScalaStoreUuidCache.username(player)}")
        FoxtrotExtendedPlugin.statMaps.forEach {
            sender.sendMessage("${it.mongoName}: ${CC.WHITE}${it[player] ?: 0}")
        }
    }

    @CommandPermission("foxtrot.stats.management")
    @Subcommand("reset")
    fun reset(sender: Player, player: UUID)
    {
        FoxtrotExtendedPlugin.statMaps.forEach { it.reset(player) }
        sender.sendMessage("${CC.GREEN}You have reset the stats of ${ScalaStoreUuidCache.username(player)}")
    }

    @CommandPermission("foxtrot.stats.management")
    @Subcommand("set")
    fun set(sender: Player, player: UUID, statistic: String, amount: Int)
    {
        val map = FoxtrotExtendedPlugin.statMaps.firstOrNull { it.mongoName.equals(statistic, true) } ?: throw ConditionFailedException("Statistic type not defined with name $statistic")
        map[player] = amount
        sender.sendMessage("${CC.GREEN}You have set ${statistic.lowercase()}'s of ${ScalaStoreUuidCache.username(player)} to $amount")
    }
}