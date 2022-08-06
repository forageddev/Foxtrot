package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.acf.annotation.Description
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.team.TeamService
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

@AutoRegister
@CommandAlias("focus|target")
@Description("Focus another player on the server")
object FocusCommand : GoodCommand()
{
    @Default
    fun execute(player: Player, target: UUID) {
        val team = TeamService.findTeamByPlayer(player.uniqueId) ?: throw ConditionFailedException("You cannot focus players whilst you are not in a team.")
        if (team.isMember(target)) throw ConditionFailedException("You cannot focus a team member.")

        team.focused = target
        team.broadcast("${CC.SEC}Your team is now focused on the player ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC}.")

        val targetPlayer = Bukkit.getPlayer(target) ?: return
        NametagHandler.reloadPlayer(targetPlayer)
    }
}