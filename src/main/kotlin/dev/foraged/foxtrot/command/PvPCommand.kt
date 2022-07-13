package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.Duration
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds

@CommandAlias("pvp|prot|protection")
@AutoRegister
object PvPCommand : GoodCommand()
{
    @Subcommand("enable")
    fun enable(player: Player, @Default("self") target: Player) {
        if (player != target && !player.hasPermission("foxtrot.pvp.management")) throw ConditionFailedException("You cannot disable other players pvp timers.")

        PvPTimerPersistableMap.resetCooldown(target.uniqueId)
        if (player == target) player.sendMessage("${CC.SEC}You have disabled your pvp timer.")
        else player.sendMessage("${CC.SEC}You have diabled the pvp timer of ${target.displayName}${CC.SEC}.")
    }

    @Subcommand("grant")
    @CommandPermission("foxtrot.pvp.management")
    fun grant(player: Player, target: Player, duration: Duration) {
        PvPTimerPersistableMap.startCooldown(target.uniqueId, duration.get().seconds.inWholeSeconds)
        player.sendMessage("${CC.SEC}You have granted ${target.displayName}${CC.PRI} ${TimeUtil.formatIntoDetailedString(duration.get().seconds.inWholeSeconds.toInt())}${CC.SEC} of pvp timer.")
    }
}