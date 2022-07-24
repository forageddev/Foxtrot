package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.Duration
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.seconds

@CommandAlias("pvp|prot|protection")
@AutoRegister
object PvPCommand : GoodCommand()
{
    @HelpCommand
    @Default
    fun help(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("enable")
    @Description("Disable your pvp timer protection")
    fun enable(player: Player, @Default("self") target: UUID) {
        if (player.uniqueId != target && !player.hasPermission("foxtrot.pvp.management")) throw ConditionFailedException("You cannot disable other players pvp timers.")
        if (!PvPTimerPersistableMap.isOnCooldown(target)) throw ConditionFailedException("You do not have an active pvp timer.")

        PvPTimerPersistableMap.resetCooldown(target)
        if (player.uniqueId == target) player.sendMessage("${CC.SEC}You have disabled your pvp timer.")
        else player.sendMessage("${CC.SEC}You have disabled the pvp timer of ${ScalaStoreUuidCache.username(target)}${CC.SEC}.")
    }

    @Subcommand("grant")
    @CommandPermission("foxtrot.pvp.management")
    @Description("Give a player a pvp protection timer")
    fun grant(player: Player, target: UUID, duration: Duration) {
        PvPTimerPersistableMap.startCooldown(target, duration.get().seconds.inWholeSeconds / 1000)
        player.sendMessage("${CC.SEC}You have granted ${ScalaStoreUuidCache.username(target)}${CC.PRI} ${TimeUtil.formatIntoDetailedString(duration.get().seconds.inWholeSeconds.toInt() / 1000)}${CC.SEC} of pvp timer.")
    }

    @Subcommand("revive")
    @CommandPermission("foxtrot.pvp.revive")
    @Description("Revive a player with the power of staff commands")
    fun revive(player: Player, target: UUID) {
        //todo: remove deathban
        player.sendMessage("${CC.SEC}You have staff-revived ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}.")
    }
}